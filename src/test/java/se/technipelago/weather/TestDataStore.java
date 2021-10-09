package se.technipelago.weather;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;

/**
 * A data store used in tests.
 */
public class TestDataStore implements DataStore {

    protected final Logger log = LogManager.getLogger(getClass().getName());

    public static String param;

    private Date lastRecordTime = Date.from(Instant.now().minus(2, ChronoUnit.HOURS));

    @Override
    public void init(Properties prop) {
        this.param = prop.getProperty("param");
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
