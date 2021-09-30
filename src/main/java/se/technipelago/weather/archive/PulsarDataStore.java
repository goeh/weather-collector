/*
 *  Copyright 2006 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package se.technipelago.weather.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;


public class PulsarDataStore implements DataStore {

    private static final Logger log = Logger.getLogger(SqlDataStore.class.getName());
    private static final String PROPERTIES_FILE = "pulsar.properties";
    private Connection conn;
    private PreparedStatement selectStatus;
    private PreparedStatement updateStatus;
    private PreparedStatement selectData;
    private PreparedStatement insertData;
    private PreparedStatement updateCurrent;

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.WARNING, PROPERTIES_FILE + " not found.");
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

    public PulsarClient init() {
        PulsarClient client = null;
        if (conn == null) {
            try {
                final Properties prop = getProperties();
                Class.forName(prop.getProperty("pulsar.service_url"));
                client = PulsarClient.builder()
                        .serviceUrl(prop.getProperty("pulsar.service_url"))
                        .authentication(
                                AuthenticationFactory.token(prop.getProperty("pulsar.token"))
                        )
                        .build();
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "Cannot find Pulsar Service URL", e);
                throw new RuntimeException(e);
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
            log.fine("Client initialized");
        }
        return client;
    }

    public void cleanup() {

        if (insertData != null) {
            try {
                insertData.close();
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Exception while closing INSERT statement", ex);
            }
            insertData = null;
        }
        if (selectData != null) {
            try {
                selectData.close();
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Exception while closing SELECT statement", ex);
            }
            selectData = null;
        }
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

    public boolean insertData(final ArchiveRecord rec) throws IOException {
        final java.sql.Timestamp timestamp = new java.sql.Timestamp(rec.getTimestamp().getTime());
        try {
            selectData.setTimestamp(1, timestamp);
            ResultSet duplicate = selectData.executeQuery();
            if (duplicate.next()) {
                int count = duplicate.getInt("cnt");
                if (count > 0) {
                    return false;
                }
            }
            insertData.setTimestamp(1, timestamp);
            insertData.setFloat(2, (float) rec.getOutsideTemperature());
            insertData.setFloat(3, (float) rec.getInsideTemperature());
            insertData.setShort(4, (short) rec.getOutsideHumidity());
            insertData.setShort(5, (short) rec.getInsideHumidity());
            insertData.setInt(6, rec.getBarometer());
            insertData.setFloat(7, (float) rec.getRainFall());
            insertData.setFloat(8, (float) rec.getRainRateHigh());
            insertData.setFloat(9, (float) rec.getWindSpeedAvg());
            insertData.setShort(10, (short) rec.getWindDirection());
            insertData.setFloat(11, (float) rec.getWindSpeedHigh());
            insertData.setShort(12, (short) rec.getSolarRadiation());
            insertData.setFloat(13, (float) rec.getUvIndex());
            insertData.execute();
        } catch(SQLException e) {
            throw new IOException(e);
        }
        return true;
    }

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

    /**
     * Insert status record.
     * @param lastDownload
     * @param lastRecord
     * @return <code>lastRecord</code>
     * @throws java.sql.SQLException
     */
    public Date insertStatus(Date lastDownload, Date lastRecord) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO status (last_dl, last_rec) VALUES (?, ?)");
        stmt.setTimestamp(1, new java.sql.Timestamp(lastDownload.getTime()));
        stmt.setTimestamp(2, new java.sql.Timestamp(lastRecord.getTime()));
        stmt.execute();
        return lastRecord;
    }

    /**
     * Update status record.
     * @param lastDownload
     * @param lastRecord
     * @return <code>lastRecord</code>
     * @throws java.sql.SQLException
     */
    public Date updateStatus(final Date lastDownload, final Date lastRecord) throws IOException {
        try {
            updateStatus.setTimestamp(1, new java.sql.Timestamp(lastDownload.getTime()));
            updateStatus.setTimestamp(2, new java.sql.Timestamp(lastRecord.getTime()));
            updateStatus.executeUpdate();
        } catch(SQLException e) {
            throw new IOException(e);
        }
        return lastRecord;
    }

    public void updateCurrent(final CurrentRecord current) throws IOException {
        try {
            setCurrentValues(updateCurrent, current);
            if (updateCurrent.executeUpdate() < 1) {
                insertCurrent(current);
            }
        } catch(SQLException e) {
            throw new IOException(e);
        }
    }

    public void insertCurrent(final CurrentRecord current) throws SQLException {

        final PreparedStatement stmt = conn.prepareStatement("INSERT INTO current (bar_trend, console_battery, forecast_icons, forecast_msg, sunrise, sunset, ts, transmit_battery) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        setCurrentValues(stmt, current);
        stmt.executeUpdate();
    }

    public void setCurrentValues(final PreparedStatement stmt, final CurrentRecord current) throws SQLException {
        final StringBuilder buf = new StringBuilder();
        final String[] icons = current.getForecastIcons();
        for (int i = 0; i < icons.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(icons[i]);
        }
        stmt.setInt(1, current.getBarometerTrend());
        stmt.setFloat(2, (float) current.getConsoleBatteryVolt());
        stmt.setString(3, buf.toString());
        stmt.setString(4, current.getForecastMessage());
        stmt.setTimestamp(5, new java.sql.Timestamp(current.getSunrise().getTime()));
        stmt.setTimestamp(6, new java.sql.Timestamp(current.getSunset().getTime()));
        stmt.setTimestamp(7, new java.sql.Timestamp(current.getTimestamp().getTime()));
        stmt.setInt(8, current.getTransmitterBatteryStatus());
    }

}
