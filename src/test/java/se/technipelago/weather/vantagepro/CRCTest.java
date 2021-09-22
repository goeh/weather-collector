/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.technipelago.weather.vantagepro;

import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author goran
 */
public class CRCTest {

    public CRCTest() {
    }

    /**
     * Test of check method, of class CRC.
     */
    @Test
    public void check() {
        System.out.println("check");
        byte[] data = new byte[]{(byte) 0xc6, (byte) 0xce, (byte) 0xa2, (byte) 0x03};
        byte[] crc = new byte[]{(byte) 0xe2, (byte) 0xb4};
        Assert.assertTrue("CRC check failed", CRC16.check(data, crc));
    }

    /**
     * Test of calculate method, of class CRC.
     */
    @Test
    public void calculate() {
        System.out.println("calculate");
        byte[] data = new byte[]{(byte) 0xc6, (byte) 0xce, (byte) 0xa2, (byte) 0x03};
        byte[] crc = new byte[]{(byte) 0xe2, (byte) 0xb4};
        byte[] test = CRC16.calculate(data);
        Assert.assertTrue("CRC high byte invalid", test[0] == crc[0]);
        Assert.assertTrue("CRC low byte invald", test[1] == crc[1]);
    }

    /**
     * Test of calculate method, of class CRC.
     */
    @Test
    public void calculateZero() {
        System.out.println("calculateZero");
        byte[] data = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte)1};
        byte[] crc = new byte[]{(byte) 0x10, (byte) 0x21};
        byte[] test = CRC16.calculate(data);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < test.length; i++) {
            String s = Integer.toHexString((int) test[i] & 0xff);
            if (s.length() == 1) {
                buf.append("0");
            }
            buf.append(s);
        }
        System.out.println(buf.toString());
        Assert.assertTrue("CRC high byte invalid", test[0] == crc[0]);
        Assert.assertTrue("CRC low byte invald", test[1] == crc[1]);
        Assert.assertTrue("CRC check failed", CRC16.check(data, crc));
    }

    /**
     * Test of calculate method, of class CRC.
     */
    @Test
    public void calculateNegative() {
        System.out.println("calculateNegative");
        byte[] data = new byte[]{(byte) 10, (byte) -10, (byte) 0, (byte) 150, (byte) -200};
        byte[] crc = new byte[]{(byte) 0xfb, (byte) 0x6c};
        byte[] test = CRC16.calculate(data);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < test.length; i++) {
            String s = Integer.toHexString((int) test[i] & 0xff);
            if (s.length() == 1) {
                buf.append("0");
            }
            buf.append(s);
        }
        System.out.println(buf.toString());
        Assert.assertTrue("CRC high byte invalid", test[0] == crc[0]);
        Assert.assertTrue("CRC low byte invald", test[1] == crc[1]);
        Assert.assertTrue("CRC check failed", CRC16.check(data, crc));
    }

    /**
     * Test of calculate method, of class CRC.
     */
    @Test
    public void calculateAndCheck() {
        System.out.println("calculateAndCheck");
        byte[] data = new byte[]{(byte) 0xc6, (byte) 0xce, (byte) 0xa2, (byte) 0x03};
        byte[] crc = CRC16.calculate(data);
        Assert.assertTrue("CRC check failed", CRC16.check(data, crc));
    }

    @Test
    public void testCRCOutputStream() throws Exception {
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        CRCOutputStream out = new CRCOutputStream(ba);
        byte[] data = new byte[]{(byte) 0xc6, (byte) 0xce, (byte) 0xa2, (byte) 0x03};
        byte[] crc = new byte[]{(byte) 0xe2, (byte) 0xb4};
        out.write(data);
        byte[] test = out.getCRC();
        Assert.assertTrue("CRC high byte invalid", test[0] == crc[0]);
        Assert.assertTrue("CRC low byte invald", test[1] == crc[1]);
        byte[] bytes = ba.toByteArray();
        for (int i = 0; i < data.length; i++) {
            Assert.assertTrue("Invalid byte at pos " + i, data[i] == bytes[i]);
        }
    }
}
