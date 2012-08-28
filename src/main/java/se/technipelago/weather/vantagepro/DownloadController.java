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

import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.archive.ArchivePage;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.DataStore;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class DownloadController extends AbstractController {

    private static final Logger log = Logger.getLogger(DownloadController.class.getName());
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
        if (!store.insertData(rec)) {
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
}
