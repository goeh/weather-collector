package nl.tudelft.streaming;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Davis {
    String ts;
    double temp_out;
    double temp_in;
}