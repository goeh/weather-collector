package nl.tudelft.davisstreaming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import se.technipelago.weather.WeatherUtils;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


public class PulsarDataStore implements DataStore {

    private Connection conn;
    private PulsarClient client;
    private Producer<DavisMessage> producer;
    private PreparedStatement selectStatus;
    private PreparedStatement updateStatus;
    private PreparedStatement updateCurrent;


    private static final String PROPERTIES_FILE = "pulsar.properties";

    private static final Logger log = LogManager.getLogger(PulsarDataStore.class);

    final Properties prop = WeatherUtils.loadProperties(PROPERTIES_FILE);

    final String service_url = prop.getProperty("pulsar.service_url");
    final String topic = prop.getProperty("pulsar.topic");
    final String token = prop.getProperty("pulsar.token");
    final String uuid = prop.getProperty("sensor.uuid");

    final float lon = Float.parseFloat((prop.getProperty("sensor.longitude")));
    final float lat = Float.parseFloat((prop.getProperty("sensor.latitude")));
    final float alt = Float.parseFloat((prop.getProperty("sensor.altitude")));

    @Override
    public void init(Properties prop) {
        // TODO use properties provided here instead of our own property file?
        if (conn == null) {
            try {
                conn = DriverManager.getConnection("jdbc:h2:file:./statusDb");
                createTables(); // TODO this is called every time.
            } catch (SQLException e) {
                log.error("Cannot connect to database", e);
                throw new RuntimeException(e);
            }
            log.debug("SQL data store initialized");
        }
        try {
            selectStatus = conn.prepareStatement("SELECT last_rec FROM status");
            updateStatus = conn.prepareStatement("UPDATE status SET last_dl = ?, last_rec = ?");
        } catch (SQLException e) {
            log.error("Failed to prepare SQL statements", e);
            throw new RuntimeException(e);
        }
        if (client == null) {
            try {
                client = PulsarClient.builder()
                        .serviceUrl(service_url)
                        .tlsTrustCertsFilePath("/etc/ssl/certs/ca-certificates.crt")
                        .authentication(
                                AuthenticationFactory.token(token)
                        )
                        .build();
            } catch (PulsarClientException ex) {
                ex.printStackTrace();
            }
        }
        if (producer == null) {
            try {
                producer = client.newProducer(Schema.AVRO(DavisMessage.class))
                        .producerName(uuid)
                        .topic(topic)
                        .sendTimeout(10, TimeUnit.SECONDS)
                        .create();
            } catch (PulsarClientException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void cleanup() {
        if (updateStatus != null) {
            try {
                updateStatus.close();
            } catch (SQLException ex) {
                log.warn("Exception while closing INSERT statement");
            }
            updateStatus = null;
        }
        if (selectStatus != null) {
            try {
                selectStatus.close();
            } catch (SQLException ex) {
                log.warn("Exception while closing SELECT statement");
            }
            selectStatus = null;
        }
        if (updateCurrent != null) {
            try {
                updateCurrent.close();
            } catch (SQLException ex) {
                log.warn("Exception while closing UPDATE statement");
            }
            updateCurrent = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                log.error("Cannot close database connection", ex);
            }
            conn = null;
        }
        if (producer != null) {
            try {
                producer.close();
            } catch (PulsarClientException ex) {
                log.warn("Exception while closing Pulsar producer");
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (PulsarClientException ex) {
                log.warn("Exception while closing Pulsar client");
            }
        }
    }

    @Override
    public Date getLastRecordTime() {
        Date d = null;
        try {
            ResultSet rs = selectStatus.executeQuery();
            if (rs.next()) {
                d = rs.getTimestamp(1);
            } else {
                final Date EPOCH = new Date(0L);
                d = insertStatus(EPOCH, EPOCH);
            }
        } catch (SQLException ex) {
            log.error("Failed to get archive status", ex);
        }
        return d;
    }

    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {

        log.debug("Sending message to Pulsar.");
        Timestamp timestamp = new Timestamp(rec.getTimestamp().getTime());

        final String formattedtimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(timestamp);

        producer.newMessage().value(DavisMessage.builder()
                .uuid(uuid)
                .latitude(lat)
                .longitude(lon)
                .altitude(alt)
                .ts(formattedtimestamp)
                .temp_out((float) rec.getOutsideTemperature())
                .temp_in((float) rec.getInsideTemperature())
                .hum_out((short) rec.getOutsideHumidity())
                .hum_in((short) rec.getInsideHumidity())
                .barometer(rec.getBarometer())
                .rain((float) rec.getRainFall())
                .rain_rate((float) rec.getRainRateHigh())
                .wind_avg((float) rec.getWindSpeedAvg())
                .wind_dir((short) rec.getWindDirection())
                .wind_high((float) rec.getWindSpeedHigh())
                .solar((short) rec.getSolarRadiation())
                .uv((float) rec.getUvIndex())
                .build()).send();

//        producer.close();
//        client.close();

        return true;
    }

    public Date insertStatus(Date lastDownload, Date lastRecord) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO status (last_dl, last_rec) VALUES (?, ?)");
        stmt.setTimestamp(1, new java.sql.Timestamp(lastDownload.getTime()));
        stmt.setTimestamp(2, new java.sql.Timestamp(lastRecord.getTime()));
        stmt.execute();
        return lastRecord;
    }

    @Override
    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        try {
            updateStatus.setTimestamp(1, new java.sql.Timestamp(lastDownload.getTime()));
            updateStatus.setTimestamp(2, new java.sql.Timestamp(lastRecord.getTime()));
            updateStatus.executeUpdate();
        } catch (SQLException e) {
            throw new IOException(e);
        }
        return lastRecord;
    }

    @Override
    public void updateCurrent(CurrentRecord current) {
    }

    private void createTables() throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, "%", null, new String[]{"TABLE"});
        List<String> tablesFound = new ArrayList<>();
        while (tables.next()) {
            tablesFound.add(tables.getString("TABLE_NAME").toLowerCase());
        }
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            if (!tablesFound.contains("status")) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS status ("
                        + "id int AUTO_INCREMENT,"
                        + "last_dl datetime NOT NULL,"
                        + "last_rec datetime NOT NULL,"
                        + "PRIMARY KEY (id));");
            }
        } finally {
            assert stmt != null;
            stmt.close();
        }
        log.debug("Database tables created successfully");
    }

}

