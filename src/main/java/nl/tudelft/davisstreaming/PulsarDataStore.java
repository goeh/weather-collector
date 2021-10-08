package nl.tudelft.davisstreaming;

import org.apache.pulsar.client.api.*;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.archive.DataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PulsarDataStore implements DataStore {

    private Connection conn;
    private PreparedStatement selectStatus;
    private PreparedStatement updateStatus;
    private PreparedStatement updateCurrent;
//    private PreparedStatement selectData;

    private static final String PROPERTIES_FILE = "pulsar.properties";

    private static final Logger log = Logger.getLogger(PulsarDataStore.class.getName());

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.SEVERE, PROPERTIES_FILE + " not found, data will not be sent.");
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
        if (conn == null) {
            try {
                conn = DriverManager.getConnection("jdbc:h2:file:./statusDb");
                createTables(); // TODO this is called every time.
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Cannot connect to database", e);
                throw new RuntimeException(e);
            }
            log.fine("SQL datastore initialized");
        }

//        if (selectData == null) {
            try {
                selectStatus = conn.prepareStatement("SELECT last_rec FROM status");
//                selectData = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM archive WHERE ts = ?");
                updateStatus = conn.prepareStatement("UPDATE status SET last_dl = ?, last_rec = ?");
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to prepare SQL statements", e);
                throw new RuntimeException(e);
            }
//        }
    }

    @Override
    public void cleanup() {
//        if (selectData != null) {
//            try {
//                selectData.close();
//            } catch (SQLException ex) {
//                log.log(Level.WARNING, "Exception while closing SELECT statement", ex);
//            }
//            selectData = null;
//        }
        if (updateStatus != null) {
            try {
                updateStatus.close();
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Exception while closing INSERT statement", ex);
            }
            updateStatus = null;
        }
        if (selectStatus != null) {
            try {
                selectStatus.close();
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Exception while closing SELECT statement", ex);
            }
            selectStatus = null;
        }
        if (updateCurrent != null) {
            try {
                updateCurrent.close();
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Exception while closing UPDATE statement", ex);
            }
            updateCurrent = null;
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Cannot close database connection", ex);
            }
            conn = null;
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
            log.log(Level.SEVERE, "Failed to get archive status", ex);
        }
        return d;
    }

    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {
        final Properties prop = getProperties();

        String service_url = prop.getProperty("pulsar.service_url");
        String topic = prop.getProperty("pulsar.topic");
        String token = prop.getProperty("pulsar.token");

        Timestamp timestamp = new Timestamp(rec.getTimestamp().getTime());
        try {
//            selectData.setTimestamp(1, timestamp);
//            ResultSet duplicate = selectData.executeQuery();
//            if (duplicate.next()) {
//                int count = duplicate.getInt("cnt");
//                if (count > 0) {
//                    return false;
//                }
//            }

            PulsarClient client = PulsarClient.builder()
                    .serviceUrl(service_url)
                    .tlsTrustCertsFilePath("/etc/ssl/certs/ca-certificates.crt")
                    .authentication(
                            AuthenticationFactory.token(token)
                    )
                    .build();

            Producer<DavisMessage> producer = client.newProducer(Schema.AVRO(DavisMessage.class))
                    .topic(topic)
                    .sendTimeout(10, TimeUnit.SECONDS)
                    .create();

            final String formattedtimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .format(timestamp);

            float lon = Float.parseFloat((prop.getProperty("sensor.longitude")));
            float lat = Float.parseFloat((prop.getProperty("sensor.latitude")));
            float alt = Float.parseFloat((prop.getProperty("sensor.altitude")));

            log.fine("Sending message to Pulsar.");
            producer.newMessage().value(DavisMessage.builder()
                    .uuid(prop.getProperty("sensor.uuid"))
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

            producer.close();
            client.close();

        } catch (PulsarClientException e) {
            throw new IOException(e);
        }
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
        } catch(SQLException e) {
            throw new IOException(e);
        }
        return lastRecord;
    }

    @Override
    public void updateCurrent(CurrentRecord current) {}

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
        log.fine("Database tables created successfully");
    }

}

