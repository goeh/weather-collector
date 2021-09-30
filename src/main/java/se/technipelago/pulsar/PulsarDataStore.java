package se.technipelago.pulsar;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.schema.SchemaDefinition;
import se.technipelago.opensensor.OpenSensorDataStore;
import se.technipelago.opensensor.OpenSensorPayload;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.archive.DataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PulsarDataStore implements DataStore {

    private static final String PROPERTIES_FILE = "pulsar.properties";

    private static final Logger log = Logger.getLogger(OpenSensorDataStore.class.getName());

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.WARNING, PROPERTIES_FILE + " not found, data will not be sent.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        return prop;
    }

    @Override
    public void init() {
        log.fine("Pulsar datastore initialized");
    }

    @Override
    public void cleanup() {}

    @Override
    public Date getLastRecordTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DavisMessage {
        String uuid;
        float latitude;
        float longitude;
        float altitude;
        String ts;
        float temp_out;
        float temp_in;
        int hum_out;
        int hum_in;
        int barometer;
        float rain;
        float rain_rate;
        float wind_avg;
        int wind_dir;
        float wind_high;
        int solar;
        float uv;
    }

    // TODO add return statement (boolean)
    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {

        final Properties prop = getProperties();

        String service_url = prop.getProperty("pulsar.service_url");
        String topic = prop.getProperty("pulsar.topic");
        String token = prop.getProperty("pulsar.token");

        PulsarClient client = PulsarClient.builder()
                .serviceUrl(service_url)
                .authentication(
                        AuthenticationFactory.token(token)
                )
                .build();

        SchemaDefinition<DavisMessage> schemaDefinition = SchemaDefinition.<DavisMessage>builder()
                .withPojo(DavisMessage.class)
                .build();

        Producer<DavisMessage> producer = client.newProducer(Schema.AVRO(schemaDefinition))
                .topic(topic)
                .create();

        float lon = Float.parseFloat((prop.getProperty("sensor.longitude")));
        float lat = Float.parseFloat((prop.getProperty("sensor.latitude")));
        float alt = Float.parseFloat((prop.getProperty("sensor.altitude")));


        log.fine("Sending message to Pulsar.");

        producer.newMessage().value(DavisMessage.builder()
                .uuid(prop.getProperty("sensor.uuid"))
                .latitude(lat)
                .longitude(lon)
                .altitude(alt)
                .ts("2021-09-29T12:00:22")
                .temp_out((float) 18.5556)
                .temp_in((float) 1.1)
                .hum_out(80)
                .hum_in(64)
                .barometer(1001)
                .rain((float) 1.1)
                .rain_rate((float) 1.0)
                .wind_avg((float) 2.2)
                .wind_dir(90)
                .wind_high((float) 3.0)
                .solar(1)
                .uv((float) 4.0)
                .build()).send();

        producer.close();
        client.close();

    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }

    private List<String> getCollectorProbes(Properties prop) {
        List<String> result = new ArrayList<>();
        String line = prop.getProperty("collector.values");
        if (line != null && line.trim().length() > 0) {
            String[] values = line.split(",");
            for (int i = 0; i < values.length; i++) {
                result.add(values[i].trim());
            }
        }
        return result;
    }

    private List<OpenSensorPayload> createPayload(ArchiveRecord rec, List<String> probes, Properties prop) {
        List<OpenSensorPayload> result = new ArrayList<>();

        for (String probe : probes) {
            OpenSensorPayload payload = new OpenSensorPayload();
            String sid = prop.getProperty("collector." + probe + ".sid");
            if (sid == null) {
                throw new IllegalArgumentException("Property collector." + probe + ".sid must be set");
            }
            payload.setSid(sid);
            payload.addValue(rec.getTimestamp(), (Number) getFieldValue(rec, probe));
            result.add(payload);
        }

        return result;
    }

    private Object getFieldValue(ArchiveRecord rec, String fieldName) {
        try {
            Field field = rec.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(rec);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.severe(e.getMessage());
        }
        return null;
    }

    @Override
    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        return lastRecord;
    }

    @Override
    public void updateCurrent(CurrentRecord current) throws IOException {
    }

}
