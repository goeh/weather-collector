/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.technipelago.weather.vantagepro;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class VantageUtilTest {

    public VantageUtilTest() {
    }

    /**
     * Test of celcius2fahrenheit method, of class VantageUtil.
     */
    @Test
    public void celcius2fahrenheit() {
        System.out.println("celcius2fahrenheit");
        double celcius = 0.0;
        double expResult = 32.0;
        double result = VantageUtil.celcius2fahrenheit(celcius);
        assertEquals(expResult, result);
    }

    /**
     * Test of fahrenheit2celcius method, of class VantageUtil.
     */
    @Test
    public void fahrenheit2celcius() {
        System.out.println("fahrenheit2celcius");
        double f = 32.0;
        double expResult = 0.0;
        double result = VantageUtil.fahrenheit2celcius(f);
        assertEquals(expResult, result);
    }

    /**
     * Test of millibar2inchHg method, of class VantageUtil.
     */
    @Test
    public void millibar2inchHg() {
        System.out.println("millibar2inchHg");
        int mb = 1016;
        double expResult = 30.0;
        double result = VantageUtil.millibar2inchHg(mb);
        assertEquals(expResult, result);
    }

    /**
     * Test of inchHg2millibar method, of class VantageUtil.
     */
    @Test
    public void inchHg2millibar() {
        System.out.println("inchHg2millibar");
        double inchHg = 30.0;
        int expResult = 1016;
        int result = VantageUtil.inchHg2millibar(inchHg);
        assertEquals(expResult, result);
    }

    /**
     * Test of ms2mph method, of class VantageUtil.
     */
    @Test
    public void ms2mph() {
        System.out.println("ms2mph");
        double ms = 100.0;
        int expResult = 224;
        int result = VantageUtil.ms2mph(ms);
        assertEquals(expResult, result);
    }

    /**
     * Test of mph2ms method, of class VantageUtil.
     */
    @Test
    public void mph2ms() {
        System.out.println("mph2ms");
        int mph = 224;
        double expResult = 100.8;
        double result = VantageUtil.mph2ms(mph);
        assertEquals(expResult, result);
    }

    /**
     * Test of mm2inch method, of class VantageUtil.
     */
    @Test
    public void mm2inch() {
        System.out.println("mm2inch");
        int mm = 254;
        double expResult = 10.0;
        double result = VantageUtil.mm2inch(mm);
        assertEquals(expResult, result);
    }

    /**
     * Test of getTemperature method, of class VantageUtil.
     */
    @Test
    public void getTemperature() {
        System.out.println("getTemperature");
        double temp = 15.0;
        byte[] expResult = new byte[]{78, 2};
        byte[] result = VantageUtil.getTemperature(temp);
        assertEquals(expResult[0], result[0]);
        assertEquals(expResult[1], result[1]);
    }

    /**
     * Test of getRain method, of class VantageUtil.
     */
    @Test
    public void getRain() {
        System.out.println("getRain");
        double mm = 10.5;
        byte[] expResult = new byte[]{52, 0};
        byte[] result = VantageUtil.getRain(mm);
        assertEquals(expResult[0], result[0]);
        assertEquals(expResult[1], result[1]);
    }

    /**
     * Test of calculateWindChill method, of class VantageUtil.
     */
    @Test
    public void calculateWindChill() {
        System.out.println("calculateWindChill");
        double tempC = -6;
        double windSpeed = 10.0;
        long expResult = -15;
        double result = VantageUtil.calculateWindChill(tempC, windSpeed);
        assertEquals(expResult, Math.round(result));
    }

    @Test
    public void testWindChill() {
        System.out.println("testWindChill");
        double tempC = 0;
        for (int i = 0; i < 20; i++) {
            double result = VantageUtil.calculateWindChill(tempC, (double) i);
            System.out.println("" + tempC + "\u00b0C @ " + i + " m/s = " + result);
        }
    }
}
