package se.technipelago.weather.vantagepro;

/**
 * Created by goran on 15-06-12.
 */
public class RingBuffer {

    private static final int BUFFER_SIZE = 2048;

    private int[] buffer = new int[BUFFER_SIZE];
    private int readIndex = 0;
    private int writeIndex = 0;

    public void write(int b) {
        buffer[writeIndex++ % BUFFER_SIZE] = b;
    }

    public void write(int[] b) {
        for (int i = 0; i < b.length; i++) {
            write(b[i]);
        }
    }

    public int read() {
        for (int i = 0; i < 3; i++) {
            if (readIndex < writeIndex) {
                return buffer[readIndex++ % BUFFER_SIZE];
            } else {
                // Data not ready yet, wait a while.
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
        return -1;
    }

    public int[] read(int len) {
        int[] b = new int[len];
        for (int i = 0; i < len; i++) {
            b[i] = read();
        }
        return b;
    }

    public void clear() {
        readIndex = 0;
        writeIndex = 0;
    }
}
