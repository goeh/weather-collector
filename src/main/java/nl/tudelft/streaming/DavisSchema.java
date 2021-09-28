package nl.tudelft.streaming;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.all;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public static class DavisSchema {
    String ts;
    float temp_out;
}