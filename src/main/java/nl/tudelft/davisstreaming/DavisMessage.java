package nl.tudelft.davisstreaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DavisMessage {
    String sensor_id;
    String sensor_name;
    float latitude;
    float longitude;
    float altitude;
    String ts;
    String date;
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
