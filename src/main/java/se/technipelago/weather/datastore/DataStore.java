package se.technipelago.weather.datastore;

import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

/**
 * Interface implemented by classes that store weather data.
 * <p>
 * Created by goran on 15-06-13.
 */
public interface DataStore {

    void init(Properties prop);

    void cleanup();

    Date getLastRecordTime();

    boolean insertData(final ArchiveRecord rec) throws IOException;

    Date updateStatus(final Date lastDownload, final Date lastRecord) throws IOException;

    void updateCurrent(final CurrentRecord current) throws IOException;
}
