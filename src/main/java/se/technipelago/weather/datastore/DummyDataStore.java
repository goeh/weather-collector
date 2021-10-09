package se.technipelago.weather.datastore;

import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A data store that prints to stdout.
 */
public class DummyDataStore implements DataStore {

    protected final Logger log = Logger.getLogger(getClass().getName());

    private Date lastRecordTime = Date.from(Instant.now().minus(1, ChronoUnit.HOURS));

    @Override
    public void init(Properties prop) {
        log.info(getClass().getName() + " initialized");
    }

    @Override
    public void cleanup() {
        log.info(getClass().getName() + " closed");
    }

    @Override
    public Date getLastRecordTime() {
        return lastRecordTime;
    }

    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {
        log.info("Insert data " + rec);
        return true;
    }

    @Override
    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        log.info("Update status " + lastDownload + " / " + lastRecord);
        this.lastRecordTime = lastRecord;
        return this.lastRecordTime;
    }

    @Override
    public void updateCurrent(CurrentRecord current) throws IOException {
        log.info("Upadate current data " + current);
    }
}
