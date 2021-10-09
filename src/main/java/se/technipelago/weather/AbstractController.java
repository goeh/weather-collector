package se.technipelago.weather;

import org.apache.commons.lang3.StringUtils;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractController implements Controller {

    protected final Logger log = Logger.getLogger(getClass().getName());

    private final Map<String, DataStore> stores = new HashMap<>();
    private String statusDataStore;

    protected void init(Properties prop) {
        initDataStores(prop);
    }

    protected void initDataStores(Properties prop) {
        final List<String> types = getDataStoreTypes(prop);
        for (String type : types) {
            addDataStore(type, initDataStore(type, prop));
        }

        final String statusType = prop.getProperty("datastore.status");
        setStatusDataStore(StringUtils.isEmpty(statusType) ? types.get(0) : statusType);
        log.fine("Data store " + statusDataStore + " will be used to store download status");
    }

    protected DataStore initDataStore(String type, Properties prop) {
        final String propertyName = "datastore." + type + ".class";
        final String className = prop.getProperty(propertyName);
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException(propertyName + " must be set");
        }

        final DataStore dataStore = createDataStore(className);
        dataStore.init(getDataStoreProperties(type, prop));
        log.info("Data store " + type + " (" + className + ") initialized");
        return dataStore;
    }

    protected DataStore createDataStore(String className) {
        try {
            final Class<?> clazz = Class.forName(className);
            if (!DataStore.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() + " must implement " + DataStore.class.getName());
            }
            return (DataStore) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create data store " + className, e);
        }
    }

    protected List<String> getDataStoreTypes(Properties prop) {
        final String types = prop.getProperty("datastore.type");
        if (StringUtils.isEmpty(types)) {
            throw new IllegalArgumentException("datastore.type must be set");
        }
        return Arrays.asList(types.split("\\s*,\\s*"));
    }

    protected Map<String, DataStore> getDataStores() {
        return this.stores;
    }

    protected void addDataStore(String type, DataStore store) {
        getDataStores().put(type, store);
    }

    protected DataStore getDataStore(String type) {
        return getDataStores().get(type);
    }

    protected void forEachDataStore(Consumer<? super DataStore> action) {
        getDataStores().values().forEach(action);
    }

    public void setStatusDataStore(String statusDataStore) {
        this.statusDataStore = statusDataStore;
    }

    protected DataStore getStatusDataStore() {
        return getDataStores().get(statusDataStore);
    }

    protected Properties getDataStoreProperties(String type, Properties root) {
        final String prefix = "datastore." + type + ".";
        final Properties prop = new Properties();
        root.forEach((key, value) -> {
            if (((String) key).startsWith(prefix)) {
                prop.setProperty(((String) key).substring(prefix.length()), (String) value);
            }
        });
        return prop;
    }

    protected void saveRecord(ArchiveRecord rec) throws IOException {
        forEachDataStore(store -> {
            try {
                if (!store.insertData(rec)) {
                    log.fine("Record already saved: " + rec);
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to save weather data", e);
            }
        });
    }

    public void cleanup() {
        forEachDataStore(store -> {
            try {
                store.cleanup();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to cleanup data store " + store.getClass().getName(), e);
            }
        });
    }
}
