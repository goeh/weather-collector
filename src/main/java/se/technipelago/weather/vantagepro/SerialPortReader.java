package se.technipelago.weather.vantagepro;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 * Created by goran on 15-06-12.
 */
public class SerialPortReader implements SerialPortEventListener {
    private SerialPort serialPort;
    private RingBuffer buffer;

    public SerialPortReader(SerialPort serialPort, RingBuffer buffer) {
        this.serialPort = serialPort;
        this.buffer = buffer;
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR()) {
            int len = event.getEventValue();
            try {
                int buf[] = serialPort.readIntArray(len);
                for(int i = 0; i < len; i++) {
                    buffer.write(buf[i]);
                }
            } catch (SerialPortException ex) {
                System.out.println(ex);
            }
        }
    }
}