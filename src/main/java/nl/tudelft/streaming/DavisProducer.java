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
import org.apache.pulsar.client.impl.schema.JSONSchema;

import java.io.IOException;

/**
 *
 * @author Saverio Guzzo <saverio.g.guzzo@gmail.com>
 */
public class DavisProducer {

    public static void main(String[] args) throws IOException {
        PulsarGetPropertyValues properties = new PulsarGetPropertyValues();
        properties.getPropValues();

        PulsarClient client = PulsarClient.builder()
                .serviceUrl(properties.getProperty("pulsar.service_url"))
                .authentication(
                        AuthenticationFactory.token(properties.getProperty("pulsar.token"))
                )
                .build();

        Producer<DavisSchema> producer = client.newProducer(JSONSchema.of(DavisSchema.class))
                .topic(properties.getProperty("pulsar.topic"))
                .create();

        // Send a message to the topic
        producer.newMessage().value(DavisSchema.builder()
                .ts("timestamp")
                .temp_out(18.5556)
                .build()).send();

        producer.close();

        client.close();

    }
}