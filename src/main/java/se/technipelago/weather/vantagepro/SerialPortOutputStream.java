package se.technipelago.weather.vantagepro;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by goran on 15-06-12.
 */
public class SerialPortOutputStream extends OutputStream {

    private static final byte[] QUIT = "quit".getBytes();//new byte[]{'q', 'u', 'i', 't'};
    private static final byte[] KILL = "kill".getBytes(); //new byte[]{'k', 'i', 'l', 'l'};

    private SerialPort serialPort;

    public SerialPortOutputStream(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            serialPort.writeByte((byte) (b & 0xff));
        } catch (SerialPortException e) {
            throw new IOException("Failed to write to serial port", e);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if(compareBytes(b, QUIT) || compareBytes(b, KILL)) {
            close();
            return;
        }

        try {
            serialPort.writeBytes(b);
        } catch (SerialPortException e) {
            throw new IOException("Failed to write to serial port", e);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        int i = off;
        while (len-- > 0) {
            write(b[i++]);
        }
    }

    @Override
    public void close() throws IOException {
        if(serialPort != null) {
            try {
                serialPort.closePort();
                serialPort = null;
            } catch (SerialPortException e) {
                throw new IOException("Failed to close serial port", e);
            }
        }
        super.close();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    /**
     * Compare two byte arrays.
     *
     * @param input   the bytes to compare.
     * @param compare the reference byte array.
     * @return true if <code>input</code> starts with the same bytes as <code>compare</code>.
     */
    private boolean compareBytes(final byte[] input, final byte[] compare) {
        if (input.length < compare.length) {
            return false;
        }
        for (int i = 0; i < compare.length; i++) {
            if (compare[i] != input[i]) {
                return false;
            }
        }
        return true;
    }
}
