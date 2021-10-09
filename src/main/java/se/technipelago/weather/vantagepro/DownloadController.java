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

import se.technipelago.weather.WeatherUtils;
import se.technipelago.weather.archive.ArchivePage;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class DownloadController extends AbstractDavisController {

    private static final String COLLECTOR_PROPERTIES = "collector.properties";

    @Override
    public void start(String[] args) {
        try {
            String firstArg = args.length > 0 ? args[0] : "localhost";
            DownloadController controller = new DownloadController();
            if (firstArg.indexOf('/') != -1) {
                controller.startLocal(args); // Local serial device
            } else {
                controller.startRemote(args); // Remote virtual device
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void run() {
        init();
        try {
            //configure();
            //test();
            //clear();
            execute();
            sleep(3000);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            cleanup();
        }
    }

    public void init() {
        initDataStores(WeatherUtils.loadProperties(COLLECTOR_PROPERTIES));
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

        log.fine("Connected to weather station");

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

    private void downloadAndSave() throws IOException {
        Date lastTime = getStatusDataStore().getLastRecordTime();
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
                if (validate(rec)) {
                    saveRecord(rec);
                    Date d = rec.getTimestamp();
                    long l = d.getTime();
                    if (l > highTime) {
                        highTime = l;
                    }
                } else {
                    log.warning("Invalid record: " + rec);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "Failed to save record: " + rec, e);
            }
        }
        return new Date(highTime);
    }

    private boolean validate(ArchiveRecord rec) {
        if (rec.getBarometer() < 500) {
            return false;
        }
        return new Date().after(rec.getTimestamp());
    }

    private void saveStatus(Date lastRecord) {
        try {
            getStatusDataStore().updateStatus(new Date(), lastRecord);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to update archive status", ex);
        }
    }

    private void saveCurrentValues() throws IOException {
        final CurrentRecord current = loop();

        forEachDataStore(store -> {
            try {
                store.updateCurrent(current);
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Failed to update data store", ex);
            }
        });
    }


}
