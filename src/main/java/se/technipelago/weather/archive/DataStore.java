package se.technipelago.weather.archive;

import java.io.IOException;
import java.util.Date;

/**
 * Created by goran on 15-06-13.
 */
public interface DataStore {

    void init();

    void cleanup();

    Date getLastRecordTime();

    boolean insertData(final ArchiveRecord rec) throws IOException;

    Date updateStatus(final Date lastDownload, final Date lastRecord) throws IOException;

    void updateCurrent(final CurrentRecord current) throws IOException;
}
