package se.technipelago.weather;

import java.io.*;
import java.util.Properties;

/**
 * Misc utilities.
 */
public class WeatherUtils {

    public static Properties loadProperties(String filename) {
        final Properties prop = new Properties();
        final File file = new File(filename);

        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                prop.load(is);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties from filesystem" + file.getAbsolutePath(), e);
            }
        } else {
            try (InputStream is = WeatherUtils.class.getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    prop.load(is);
                } else {
                    throw new FileNotFoundException(filename);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties from classpath: " + filename, e);
            }
        }
        return prop;
    }
}
