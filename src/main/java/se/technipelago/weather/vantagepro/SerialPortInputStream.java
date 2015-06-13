package se.technipelago.weather.vantagepro;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by goran on 15-06-12.
 */
public class SerialPortInputStream extends InputStream {

    private RingBuffer buffer;

    public SerialPortInputStream(RingBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        return buffer.read();
    }

    @Override
    public void close() throws IOException {
        buffer.clear();
        super.close();
    }
}
