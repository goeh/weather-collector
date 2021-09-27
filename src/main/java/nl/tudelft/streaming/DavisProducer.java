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
package nl.tudelft.streaming;

import org.apache.pulsar.client.api.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Saverio Guzzo <saverio.g.guzzo@gmail.com>
 */
public class DavisProducer {

    public static void main(String[] args) throws IOException {
        PulsarGetPropertyValues properties = new PulsarGetPropertyValues();
        properties.getPropValues();

        PulsarClient client = PulsarClient.builder()
                // TODO fix how to access this
                .serviceUrl(properties.get("SERVICE_URL"))
                .authentication(
                        AuthenticationFactory.token("verylongtokenhere")
                )
                .build();

        Producer<byte[]> producer = client.newProducer()
                .topic("sometopichere")
                .create();

        // Send a message to the topic
        producer.send("Hey hey this is a message!\n".getBytes());

        producer.close();

        client.close();

    }
}