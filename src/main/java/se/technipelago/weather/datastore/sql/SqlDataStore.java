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
package se.technipelago.weather.datastore.sql;

import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A data persistence implementation that sore data in a SQL database.
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class SqlDataStore implements DataStore {

    private static final Logger log = Logger.getLogger(SqlDataStore.class.getName());
    private Connection conn;
    private PreparedStatement selectStatus;
    private PreparedStatement updateStatus;
    private PreparedStatement selectData;
    private PreparedStatement insertData;
    private PreparedStatement updateCurrent;

    public void init(Properties prop) {
        if (conn == null) {
            try {
                Class.forName(prop.getProperty("driver", "org.h2.Driver"));
                conn = DriverManager.getConnection(prop.getProperty("url", "jdbc:h2:file:./weatherDb"));
                createTables();
            } catch (ClassNotFoundException e) {
                log.log(Level.SEVERE, "Cannot find JDBC driver", e);
                throw new RuntimeException(e);
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Cannot connect to database", e);
                throw new RuntimeException(e);
            }
            log.fine("SQL datastore initialized");
        }

        if (selectData == null) {
            try {
                selectData = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM archive WHERE ts = ?");
                insertData = conn.prepareStatement("INSERT INTO archive (ts,temp_out,temp_in,hum_out,hum_in,barometer,rain,rain_rate,wind_avg,wind_dir,wind_high,solar,uv) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
                selectStatus = conn.prepareStatement("SELECT last_rec FROM status");
                updateStatus = conn.prepareStatement("UPDATE status SET last_dl = ?, last_rec = ?");
                updateCurrent = conn.prepareStatement("UPDATE current SET bar_trend = ?, console_battery = ?, forecast_icons = ?, forecast_msg = ?, sunrise = ?, sunset = ?, ts = ?, transmit_battery = ?");
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Failed to prepare SQL statements", e);
                throw new RuntimeException(e);
            }
        }
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

    private void createTables() throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tables = metaData.getTables(null, "%", null, new String[]{"TABLE"});
        List<String> tablesFound = new ArrayList<String>();
        while (tables.next()) {
            tablesFound.add(tables.getString("TABLE_NAME").toLowerCase());
        }
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            if (!tablesFound.contains("archive")) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS archive ("
                        + "id int NOT NULL AUTO_INCREMENT,"
                        + "ts datetime NOT NULL,"
                        + "temp_out float NULL,"
                        + "temp_in float NULL,"
                        + "hum_out smallint NULL,"
                        + "hum_in smallint NULL,"
                        + "barometer int NULL,"
                        + "rain float NULL,"
                        + "rain_rate float NULL,"
                        + "wind_avg float NULL,"
                        + "wind_dir smallint NULL,"
                        + "wind_high float NULL,"
                        + "solar smallint NULL,"
                        + "uv float NULL,"
                        + "PRIMARY KEY (id));");
            }
            if (!tablesFound.contains("current")) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS current ("
                        + "id int AUTO_INCREMENT,"
                        + "bar_trend smallint NULL,"
                        + "console_battery float NULL,"
                        + "forecast_icons varchar(28) NULL,"
                        + "forecast_msg varchar(255) NULL,"
                        + "sunrise datetime NULL,"
                        + "sunset datetime NULL,"
                        + "ts datetime NOT NULL,"
                        + "transmit_battery smallint NULL,"
                        + "PRIMARY KEY (id));");
            }
            if (!tablesFound.contains("status")) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS status ("
                        + "id int AUTO_INCREMENT,"
                        + "last_dl datetime NOT NULL,"
                        + "last_rec datetime NOT NULL,"
                        + "PRIMARY KEY (id));");
            }
        } finally {
            stmt.close();
        }
        log.fine("Database tables created successfully");
    }
}
