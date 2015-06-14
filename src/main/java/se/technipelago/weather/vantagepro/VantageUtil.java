/*
 *  Copyright 2006 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package se.technipelago.weather.vantagepro;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * Misc helper functions.
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class VantageUtil {

    private static final int BUF_LENGTH = 256;

    private VantageUtil() {
    }

    /**
     * Read bytes until CR.
     * @param is the input stream to read bytes from.
     * @return return a byte array up to but not including the NL or CR.
     * @throws java.io.IOException if the read operation fails.
     */
    public static byte[] readLine(InputStream is) throws IOException {
        byte[] inputBuffer = new byte[BUF_LENGTH];
        int idx = 0;
        int character;
        while ((character = is.read()) != -1) {
            if (character == '\r') {
                break;
            }
            if (idx >= BUF_LENGTH) {
                // Invalid input, throw away this line.
                System.err.println("Invalid input");
                while (is.read() != -1) {
                // Throw away.
                }
                return new byte[0];
            }
            inputBuffer[idx++] = (byte) character;
        }

        /*
         * Remove trailing CR/NL
         */
        if (idx > 0) {
            while (--idx != -1) {
                if (inputBuffer[idx] != 10 && inputBuffer[idx] != 13) {
                    break;
                }
            }
            ++idx;
        }
        byte[] input = new byte[idx];
        System.arraycopy(inputBuffer, 0, input, 0, idx);

        return input;
    }

    /**
     * Return the two least significant bytes of a value.
     * The least signifcant byte is returned first (at index 0)
     * @param value the value to convert to bytes
     * @return a byte array of size 2 with LSB first.
     */
    public static byte[] getBytes(int value) {
        byte[] rval = new byte[2];
        // Least significant byte first.
        rval[0] = (byte) (value & 0xff);
        rval[1] = (byte) ((value >>> 8) & 0xff);
        return rval;
    }

    /**
     * Compare two byte arrays.
     *
     * @param input the bytes to compare.
     * @param compare the reference byte array.
     * @return true if <code>input</code> starts with the same bytes as <code>compare</code>.
     */
    public static boolean compareBytes(final byte[] input, final byte[] compare) {
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

    /**
     * Return a date in Davis Vantage Pro format (two bytes).
     * @param timestamp the date value
     * @return a byte array of size 2
     */
    public static byte[] getDate(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int value = day + month * 32 + (year - 2000) * 512;
        return getBytes(value);
    }

    /**
     * Return a time in Davis Vantage Pro format (two bytes).
     * @param timestamp the time value
     * @return a byte array of size 2
     */
    public static byte[] getTime(Date timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(timestamp);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int value = hour * 100 + minute;
        return getBytes(value);
    }

    /**
     * Parse Vantage Pro date into <code>java.util.Date</code>.
     *
     * @param buf station date format
     * @param offset byte offset where station time begins
     * @return the date in Java format.
     */
    public static Date getTime(byte[] buf, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.SECOND, (int) buf[offset + 0]);
        cal.set(Calendar.MINUTE, (int) buf[offset + 1]);
        cal.set(Calendar.HOUR_OF_DAY, (int) buf[offset + 2]);
        cal.set(Calendar.DAY_OF_MONTH, (int) buf[offset + 3]);
        cal.set(Calendar.MONTH, (int) buf[offset + 4] - 1);
        cal.set(Calendar.YEAR, (int) buf[offset + 5] + 1900);
        return cal.getTime();
    }

    /**
     * Convert degrees Celcius to degrees Fahrenheit.
     * @param celcius the temperature in Celcius.
     * @return the temperature in Fahrenheit.
     */
    public static double celcius2fahrenheit(double celcius) {
        return celcius * 9 / 5 + 32;
    }

    /**
     * Convert degrees Fahrenheit to degrees Celcius.
     * @param f the temperature in Fahrenheit.
     * @return the temperature in Celcius.
     */
    public static double fahrenheit2celcius(double f) {
        return (f - 32) * 5 / 9.0;
    }

    /**
     * Convert pressure as millibar to inch Hg.
     * @param mb the millibar value to convert.
     * @return the pressure as inch Hg.
     */
    public static double millibar2inchHg(int mb) {
        return Math.round(mb * 0.02953007 * 100) / 100.0;
    }

    /**
     * Convert pressure as inch Hg to millibar.
     * @param inchHg the inch Hg value to convert.
     * @return the pressure as millibar.
     */
    public static int inchHg2millibar(double inchHg) {
        return (int) Math.round(inchHg / 0.02953007);
    }

    /**
     * Convert velocity meter/second to miles/hour.
     * @param ms meter per second
     * @return miles per hour
     */
    public static int ms2mph(double ms) {
        return (int) Math.round(ms * 2.24);
    }

    /**
     * Convert velocity miles/hour to meter/second.
     * @param mph miles per hour
     * @return meter per second
     */
    public static double mph2ms(int mph) {
        return mph * 0.45;
    }

    /**
     * Calculate wind chill.
     * The "Chilled" air temperature can also be expressed as a function of
     * wind velocity and ambient air temperature.
     *
     * @param tempC temperature in degrees Celcius
     * @param windSpeed wind speed in meters per second (m/s).
     * @return chilled air temperature
     */
    public static double calculateWindChill(final double tempC, final double windSpeed) {
        double tempF = celcius2fahrenheit(tempC);
        double mph = ms2mph(windSpeed);
        if (tempF < 50.0 && mph > 3.0) {
            // Wind chill is only defined for temperatures below 50F and
            // wind speed above 3 MPH.
            double chillF = 35.74 + (0.6215 * tempF) - (35.75 * Math.pow(mph, 0.16)) + (0.4275 * tempF * Math.pow(mph, 0.16));
            double chillC = (Math.round(fahrenheit2celcius(chillF) * 10)) / 10.0;
            return chillC;
        }
        return tempC;
    }

    /**
     * Convert millimeter to inch.
     * @param mm millimeter value
     * @return the value in inch
     */
    public static double mm2inch(int mm) {
        return mm / 25.4;
    }

    /**
     * Convert inch to millimeter.
     * @param inch inch value
     * @return the value in millimeter
     */
    public static double inch2mm(double inch) {
        return inch * 25.4;
    }

    /**
     * return a Vantage Pro two-byte representation of temperature.
     * @param temp the temperature in degrees Celcius.
     * @return a byte array with size=2
     */
    public static byte[] getTemperature(double temp) {
        double f = celcius2fahrenheit(temp);
        return getBytes((int) (f * 10));
    }

    /**
     * Return a Vantage Pro two-byte representation of rain fall.
     * @param mm millimeter rain.
     * @return a byte array with size=2
     */
    public static byte[] getRain(double mm) {
        return getBytes((int) (mm * 5.0));
    }
}
