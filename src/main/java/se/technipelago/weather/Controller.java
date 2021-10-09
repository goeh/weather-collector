package se.technipelago.weather;

import se.technipelago.weather.datastore.DataStore;

public interface Controller {

    void start(String[] args);

    DataStore getDataStore(String name);
}
