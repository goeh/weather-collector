package nl.tudelft.streaming;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Davis {
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