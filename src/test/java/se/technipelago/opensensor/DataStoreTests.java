package se.technipelago.opensensor;

import org.junit.Test;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.OpenSensorDataStore;

import java.util.Date;

/**
 * Created by goran on 2016-02-14.
 */
public class DataStoreTests {

    @Test
    public void testPayload() throws Exception {
        ArchiveRecord rec = new ArchiveRecord();

        rec.setRecordNumber(1);
        rec.setTimestamp(new Date());
        rec.setBarometer(1000);
        rec.setInsideHumidity(29);
        rec.setInsideTemperature(19.8);
        rec.setOutsideHumidity(93);
        rec.setOutsideTemperature(-2.0);
        rec.setRainFall(0.2);
        rec.setSolarRadiation(0);
        rec.setUvIndex(0);
        rec.setWindDirection(4);
        rec.setWindSpeedAvg(1.5);

        OpenSensorDataStore ds = new OpenSensorDataStore();

        ds.init();
        ds.insertData(rec);
        ds.cleanup();
    }
}
