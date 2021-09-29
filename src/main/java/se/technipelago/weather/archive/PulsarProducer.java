/*
 *  Copyright 2021 Saverio Guzzo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package se.technipelago.weather.archive;

import nl.tudelft.streaming.DavisMessage;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pulsar.client.api.*;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 * @author Saverio Guzzo <s.guzzo@tudelft.nl>
 */
public class PulsarProducer implements DataStore {

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class DavisMessage {
        String uuid;
        float latitude;
        float longitude;
        float altitude;
        String ts;
        float temp_out;
        float temp_in;
        int hum_out;
        int hum_in;
        int barometer;
        float rain;
        float rain_rate;
        float wind_avg;
        int wind_dir;
        float wind_high;
        int solar;
        float uv;
    }

    private static final Logger log = Logger.getLogger(PulsarProducer.class.getName());
    private static final String PROPERTIES_FILE = "pulsar.properties";

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.WARNING, PROPERTIES_FILE + " not found! Please place it into classpath.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        return prop;
    }

    public void init() {
        try {
            final Properties properties = getProperties();
            PulsarClient client = PulsarClient.builder()
                    .serviceUrl(properties.getProperty("pulsar.service_url"))
                    .authentication(
                            AuthenticationFactory.token(properties.getProperty("pulsar.token"))
                    )
                    .build();

            Producer<DavisMessage> producer = client.newProducer(Schema.AVRO(DavisMessage.class))
                    .topic(properties.getProperty("pulsar.topic"))
                    .create();

        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }


}
