package nl.tudelft.streaming;

import org.apache.pulsar.client.api.*;
import java.io.IOException;

public class DavisProducer {

    private static final String SERVICE_URL = "someserviceurl";

    public static void main(String[] args) throws IOException
    {

        // Create client object
        PulsarClient client = PulsarClient.builder()
                .serviceUrl(SERVICE_URL)
                .authentication(
                        AuthenticationFactory.token("verylongtokenhere")
                )
                .build();

        // Create producer on a topic
        Producer<byte[]> producer = client.newProducer()
                .topic("sometopichere")
                .create();

        // Send a message to the topic
        producer.send("Hey hey this is a message!\n".getBytes());

        //Close the producer
        producer.close();

        // Close the client
        client.close();

    }

}