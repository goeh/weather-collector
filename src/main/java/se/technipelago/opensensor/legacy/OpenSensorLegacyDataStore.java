package se.technipelago.opensensor.legacy;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author Goran Ehrsson
 * @since 1.0
 */
public class OpenSensorLegacyDataStore implements DataStore {

    protected final Logger log = Logger.getLogger(getClass().getName());

    private String url;
    private String clientKey;
    private String clientSecret;

    @Override
    public void init(Properties prop) {
        url = prop.getProperty("url");
        if (StringUtils.isEmpty(url)) {
            log.severe("Property 'url' must be set");
            return;
        }
        clientKey = prop.getProperty("client.key");
        if (StringUtils.isEmpty(clientKey)) {
            log.severe("Property 'client.key' must be set");
            return;
        }
        clientSecret = prop.getProperty("client.secret");
        if (StringUtils.isEmpty(clientSecret)) {
            log.severe("Property 'client.secret' must be set");
            return;
        }
    }

    @Override
    public void cleanup() {
    }

    @Override
    public Date getLastRecordTime() {
        return null;
    }

    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {
        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(url);
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String timestamp = dateFormat.format(rec.getTimestamp());
        final StringBuilder buf = new StringBuilder();

        buf.append("{\n");
        buf.append("  \"clientKey\": \"" + clientKey + "\",\n");
        buf.append("  \"clientSecret\": \"" + clientSecret + "\",\n");
        buf.append("  \"data\": [\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"outsideTemperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getOutsideTemperature() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"outsideHumidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getOutsideHumidity() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"greenhouseTemperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraTemperature1() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"greenhouseHumidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraHumidity1() + "\n");
        buf.append("    }\n");

        buf.append("  ]\n");
        buf.append("}\n");

        httpPost.setEntity(new StringEntity(buf.toString(), ContentType.create("application/json")));

        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
        }

        log.fine("Weather data for " + timestamp + " sent to " + url);

        return false;
    }

    @Override
    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        return null;
    }

    @Override
    public void updateCurrent(CurrentRecord current) throws IOException {

    }
}
