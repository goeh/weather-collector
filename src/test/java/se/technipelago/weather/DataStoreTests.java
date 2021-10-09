package se.technipelago.weather;

import org.junit.jupiter.api.Test;
import se.technipelago.weather.vantagepro.DownloadController;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by goran on 2016-02-14.
 */
public class DataStoreTests {

    @Test
    public void testInitDataStores() {
        final Properties props = new Properties();
        props.setProperty("datastore.type", "ds1,ds2");
        props.setProperty("datastore.status", "ds1");
        props.setProperty("datastore.ds1.class", TestDataStore.class.getName());
        props.setProperty("datastore.ds2.class", TestDataStore.class.getName());
        props.setProperty("datastore.ds1.param", "Hello World");
        props.setProperty("datastore.ds2.param", "42");

        final DownloadController controller = new DownloadController();
        controller.initDataStores(props);
        assertEquals(2, controller.getDataStores().size());
        assertNotNull(controller.getDataStore("ds1"));
        assertNotNull(controller.getDataStore("ds2"));
        assertNull(controller.getDataStore("ds3"));
    }

}
