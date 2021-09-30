package se.technipelago.pulsar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

//TODO understand why it can't be static

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DavisMessage {
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
