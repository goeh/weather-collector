package nl.tudelft.streaming;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PulsarGetPropertyValues {
    HashMap<String, String> prop = new HashMap<String, String>();
    InputStream inputStream;

    public HashMap<String, String> getPropValues() throws IOException {

        try {
            Properties prop = new Properties();
            String propFileName = "pulsar.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            String service_url = prop.getProperty("service_url");
            String token = prop.getProperty("token");
            String topic = prop.getProperty("topic");

            prop.put("SERVICE_URL", service_url);
            prop.put("TOKEN", token);
            prop.put("TOPIC", topic);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            assert inputStream != null;
            inputStream.close();
        }
        return prop;
    }
}
