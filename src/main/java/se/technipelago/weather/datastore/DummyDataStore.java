package se.technipelago.weather.datastore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;

/**
 * A data store that prints to stdout.
 */
public class DummyDataStore implements DataStore {

    protected final Logger log = LogManager.getLogger(getClass().getName());

    private Date lastRecordTime = Date.from(Instant.now().minus(3, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS));

    @Override
    public void init(Properties prop) {
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
        log.info("Update current data " + current);
    }
}
