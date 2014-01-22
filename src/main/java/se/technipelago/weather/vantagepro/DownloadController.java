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
package se.technipelago.weather.vantagepro;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.archive.ArchivePage;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.DataStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class DownloadController extends AbstractController {

    private static final Logger log = Logger.getLogger(DownloadController.class.getName());
    private static final String PROPERTIES_FILE = "remote.properties";
    private final DataStore store = new DataStore();

    public static void main(String[] args) {
        try {
            new DownloadController().start(args);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    protected void run() {
        init();
        try {
            execute();
            sleep(3000);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            cleanup();
        }
    }

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.WARNING, PROPERTIES_FILE + " not found, data will not be pushed to remote service.");
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

    public void init() {
        store.init();
    }

    public void cleanup() {
        try {
            out.write("quit\n".getBytes());
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Cannot stop the receiver thread", ex);
        }
        store.cleanup();
    }

    public void execute() throws IOException {
        byte[] buf;

        // Send wakeup command.
        if (wakeup()) {
            log("\t", "The station is awake.");
        } else {
            log.warning("No response from station");
            return;
        }

        // Get current station time.
        writeString("GETTIME\n");
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        log(IN, "<ACK>");
        buf = readBytes(8);
        if (!CRC16.check(buf, 0, 8)) {
            throw new IOException("CRC error");
        }

        // Get station time.
        Date serverTime = new Date();
        Date consoleTime = VantageUtil.getTime(buf, 0);
        log("\t", "Console Time: " + consoleTime);

        downloadAndSave();
        saveCurrentValues();

        // Synchronize console time with server.
        long diff = serverTime.getTime() - consoleTime.getTime();
        if (Math.abs(diff) > 5000) {
            log.fine("Console clock is out of sync: " + diff);
            wakeup();
            setConsoleTime(new Date());
        }
    }

    public void test() throws IOException {
        byte[] buf;

        // Test command.
        writeString("TEST\n");
        expectString("\n\rTEST\n\r");


        // Determine station type.
        out.write(new byte[]{'W', 'R', 'D', 0x12, 0x4d, '\n'});
        log(OUT, "WRD<0x12><0x4d>\n");
        buf = readBytes(2);
        if (buf[0] != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        String stationType = getStationType(buf[1]);
        if (stationType == null) {
            throw new IOException("Unsupported station type: " + String.valueOf((int) buf[1]));
        }
        log("\t", "The station is a " + stationType);

        // Firmware version.
        writeString("VER\n");
        expectString("\n\rOK\n\r");
        byte[] line = VantageUtil.readLine(in);
        log(IN, new String(line));
    }

    private void downloadAndSave() throws IOException {
        Date lastTime = store.getLastRecordTime();
        long highTime = 0L;
        ArchivePage[] pages = download(lastTime);
        if (pages.length == 0) {
            log.warning("No data downloaded");
            return;
        }
        for (ArchivePage p : pages) {
            Date d = savePage(p);
            long l = d.getTime();
            if (l > highTime) {
                highTime = l;
            }
        }
        Date last = new Date(highTime);
        log.fine("Last recorded time was " + last);
        saveStatus(last);
    }

    private Date savePage(ArchivePage p) {
        log.fine("Saving page " + p.getPageNumber());
        long highTime = 0;
        for (int i = 0; i < 5; i++) {
            ArchiveRecord rec = p.getRecord(i);
            try {
                saveRecord(rec);
                Date d = rec.getTimestamp();
                long l = d.getTime();
                if (l > highTime) {
                    highTime = l;
                }
            } catch (SQLException e) {
                log.log(Level.WARNING, "Failed to save record: " + rec, e);
            }
        }
        return new Date(highTime);
    }

    private void saveRecord(ArchiveRecord rec) throws SQLException {
        if (store.insertData(rec)) {
            try {
                postToExternal(rec);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Failed to post to external service", e);
            }
        } else {
            log.fine("Record already saved: " + rec);
        }
    }

    private void saveStatus(Date lastRecord) {
        try {
            store.updateStatus(new Date(), lastRecord);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Failed to update archive status", ex);
        }
    }

    private void saveCurrentValues() throws IOException {
        final CurrentRecord current = loop();
        try {
            store.updateCurrent(current);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "Failed to update current values", ex);
        }
    }

    private void postToExternal(ArchiveRecord rec) throws IOException {
        final Properties prop = getProperties();
        String url = prop.getProperty("remote.url");
        if(url == null || url.trim().length() == 0) {
            return;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = dateFormat.format(rec.getTimestamp());
        StringBuilder buf = new StringBuilder();
        String clientKey = prop.getProperty("remote.client.key");
        String clientSecret = prop.getProperty("remote.client.secret");
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
        buf.append("      \"sid\": \"cellarTemperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraTemperature1() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"cellarHumidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraHumidity1() + "\n");
        buf.append("    }\n");

        buf.append("  ]\n");
        buf.append("}\n");


        httpPost.setEntity(new StringEntity(buf.toString(), ContentType.create("application/json")));

        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }
    }
}
