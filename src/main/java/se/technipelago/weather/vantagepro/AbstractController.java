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

import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.archive.ArchivePage;
import se.technipelago.weather.archive.ArchiveRecord;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author goran
 */
public abstract class AbstractController {

    protected static final String IN = "< ";
    protected static final String OUT = "> ";
    protected InputStream in;
    protected OutputStream out;

    protected abstract void run();

    protected void start(String[] args) throws IOException {
        Socket connection = null;
        try {
            String host = args.length > 0 ? args[0] : "localhost";
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;

            connection = new Socket(host, port);
            connection.setSoTimeout(5000);
            in = connection.getInputStream();
            out = connection.getOutputStream();
            run();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    protected void writeString(String string) throws IOException {
        out.write(string.getBytes());
        out.flush();
        log(OUT, escape(string));
    }

    protected void expectString(String string) throws IOException {
        sleep(250);
        int length = string.length();
        byte[] buf = readBytes(length);
        String s = new String(buf);
        if (!string.equals(s)) {
            throw new IOException("Invalid response: " + escape(s));
        }
    }

    protected byte[] readBytes(int length) throws IOException {
        byte[] buf = new byte[length];
        //log(IN);
        for (int i = 0; i < length; i++) {
            int c = in.read();
            if (c != -1) {
                buf[i] = (byte) (c & 0xff);
            //logByte(buf[i]);
            } else {
                throw new IOException("Unexpected EOF");
            }
        }
        log(IN, buf, 0, length);
        //logNL();

        return buf;
    }

    protected boolean wakeup() throws IOException {
        // Send wakeup command.
        boolean awake = false;
        int i = 0;
        while (awake == false && i++ < 3) {
            writeString("\n");
            try {
                expectString("\n\r");
                awake = true;
            } catch (IOException e) {
            // Ignore.
            }
            sleep(2000);
        }
        return awake;
    }


    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            // Ignore.
        }
    }

    protected String getStationType(int n) {
        String stationType = null;
        switch (n) {
            case 0:
                stationType = "Wizard III";
                break;
            case 1:
                stationType = "Wizard II";
                break;
            case 2:
                stationType = "Monitor";
                break;
            case 3:
                stationType = "Perception";
                break;
            case 16:
                stationType = "Vantage Pro or Vantage Pro 2";
                break;
        }
        return stationType;
    }

    /**
     * Set console time.
     * @param time the time to set
     * @throws java.io.IOException if a communication error occurs.
     */
    protected void setConsoleTime(final Date time) throws IOException {
        writeString("SETTIME\n");
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        byte[] bytes = new byte[6];
        bytes[0] = (byte) cal.get(Calendar.SECOND);
        bytes[1] = (byte) cal.get(Calendar.MINUTE);
        bytes[2] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        bytes[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        bytes[4] = (byte) (cal.get(Calendar.MONTH) + 1);
        bytes[5] = (byte) (cal.get(Calendar.YEAR) - 1900);

        out.write(bytes);
        CRC16 crc = new CRC16();
        crc.add(bytes);
        out.write(crc.getCrc());
        if (in.read() != Constants.ACK) {
            throw new IOException("Cannot set station time");
        }
        // Wait a while because the station can get a little dizzy after SETTIME.
        sleep(3000);
    }

    protected Date parseTimestamp(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        int year = ((word >>> 9) & 0x7f) + 2000;
        int month = ((word >>> 5) & 0x0f) - 1;
        int day = word & 0x1f;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        word = parseWord(buf, offset + 2);
        int hour = (int) (word / 100);
        int minute = word % 100;
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected int parseWord(byte[] buf, int offset) {
        int firstByte = (0x000000ff & ((int) buf[offset + 1]));
        int secondByte = (0x000000ff & ((int) buf[offset]));
        return (firstByte << 8 | secondByte);
    }

    /**
     * Return degrees Celcius.
     * @param buf
     * @param offset
     * @return
     */
    protected double parseTemperature(byte[] buf, int offset) {
        int f = ((int) buf[offset + 1] << 8) | (buf[offset] & 0xff) & 0xffff;
        if(f == 32767) {
            return 0.0;
        }
        return VantageUtil.fahrenheit2celcius(f / 10.0);
    }

    /**
     * Return degrees Celcius.
     * @param buf
     * @param offset
     * @return
     */
    protected double parseExtraTemperature(byte[] buf, int offset) {
        int f = (int) (buf[offset] & 0xff);
        if(f == 255) {
            return 0.0;
        }
        return VantageUtil.fahrenheit2celcius(f - 90.0);
    }

    /**
     * Return millimeter rain.
     * @param buf
     * @param offset
     * @return
     */
    protected double parseRain(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        return word / 5.0;
    }

    protected int parseBarometer(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        double inchHg = word / 1000.0;

        return VantageUtil.inchHg2millibar(inchHg);
    }

    protected double parseWindSpeed(byte[] buf, int offset) {
        int mph = buf[offset];
        return mph != -1 ? mph * 0.45 : 0;
    }

    protected ArchivePage[] download(Date startRecord) throws IOException {
        if (startRecord == null) {
            throw new IllegalArgumentException("startRecord must be != null");
        }
        writeString("DMPAFT\n");
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        log(IN, "<ACK>");
        CRC16 crc = new CRC16();
        byte[] dateBytes = VantageUtil.getDate(startRecord);
        byte[] timeBytes = VantageUtil.getTime(startRecord);
        crc.add(dateBytes);
        crc.add(timeBytes);

        out.write(dateBytes);
        out.write(timeBytes);
        out.write(crc.getCrc());
        int n = in.read();
        if (n != Constants.ACK) {
            throw new IOException("Invalid response: " + Integer.toHexString(n));
        }
        log(IN, "<ACK>");
        byte[] buf = readBytes(6);
        if (!CRC16.check(buf, 0, 6)) {
            throw new IOException("CRC error");
        }
        int numPages = parseWord(buf, 0);
        int startPage = parseWord(buf, 2);
        log("\tNumber of pages: ", String.valueOf(numPages));
        log("\tStart page: ", String.valueOf(startPage));

        out.write(Constants.ACK);
        log(OUT, "<ACK>");

        List<ArchivePage> pages = new ArrayList<ArchivePage>();
        for (int i = 0; i < numPages; i++) {
            byte[] page = readBytes(267);
            if (!CRC16.check(page, 0, page.length)) {
                throw new IOException("CRC error");
            }
            ArchivePage ap = parseArchivePage(page);
            pages.add(ap);
            out.write(Constants.ACK);
            log(OUT, "<ACK>");
        }
        return pages.toArray(new ArchivePage[pages.size()]);
    }

    protected ArchivePage parseArchivePage(byte[] page) {
        ArchivePage ap = new ArchivePage();
        int offset = 0;

        ap.setPageNumber((int) page[offset++] & 0xff);
        for (int i = 0; i < 5; i++) {
            ArchiveRecord rec = ap.getRecord(i);
            rec.setTimestamp(parseTimestamp(page, offset));
            rec.setOutsideTemperature(parseTemperature(page, offset + 4));
            rec.setOutsideTemperatureHigh(parseTemperature(page, offset + 6));
            rec.setOutsideTemperatureLow(parseTemperature(page, offset + 8));
            rec.setRainFall(parseRain(page, offset + 10));
            rec.setRainRateHigh(parseRain(page, offset + 12));
            rec.setBarometer(parseBarometer(page, offset + 14));
            int solar = parseWord(page, offset + 16);
            if(solar == 32767) {
                solar = 0;
            }
            rec.setSolarRadiation(solar);
            // Number of wind samples skipped.
            rec.setInsideTemperature(parseTemperature(page, offset + 20));
            int humidity = (int) page[offset + 22];
            rec.setInsideHumidity(humidity == -1 ? 0 : humidity);
            humidity = (int) page[offset + 23];
            rec.setOutsideHumidity(humidity == -1 ? 0 : humidity);
            rec.setWindSpeedAvg(parseWindSpeed(page, offset + 24));
            rec.setWindSpeedHigh(parseWindSpeed(page, offset + 25));
            // Direction of high wind speed skipped.
            rec.setWindDirection((int) page[offset + 27]);
            int uv = (int) page[offset + 28];
            rec.setUvIndex(uv == -1 ? 0 : uv / 10.0);

            rec.setExtraTemperature1(parseExtraTemperature(page, offset + 45));
            rec.setExtraTemperature2(parseExtraTemperature(page, offset + 46));
            rec.setExtraTemperature3(parseExtraTemperature(page, offset + 47));

            humidity = (int) page[offset + 43]; rec.setExtraHumidity1(humidity == -1 ? 0 : humidity);
            humidity = (int) page[offset + 44]; rec.setExtraHumidity2(humidity == -1 ? 0 : humidity);

            offset += 52;
        }

        return ap;
    }

    protected CurrentRecord loop() throws IOException {

        writeString("LOOP 1\n");
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        log(IN, "<ACK>");

        byte[] buf = readBytes(99);
        if (!CRC16.check(buf, 0, buf.length)) {
            throw new IOException("CRC error");
        }

        return parseLoopRecord(buf);
    }
    protected static final byte FORECAST_ICON_RAIN_BIT = 0x01;
    protected static final byte FORECAST_ICON_CLOUD_BIT = 0x02;
    protected static final byte FORECAST_ICON_PARTLY_CLOUD_BIT = 0x04;
    protected static final byte FORECAST_ICON_SUN_BIT = 0x08;
    protected static final byte FORECAST_ICON_SNOW_BIT = 0x10;

    protected CurrentRecord parseLoopRecord(byte[] buf) throws IOException {
        if (buf[0] != 'L' || buf[1] != 'O' || buf[2] != 'O') {
            throw new IOException("Invalid response: " + escape(buf, 0, 3, true));
        }
        byte barTrend = buf[3];
        byte xmitBatteryStatus = buf[86];
        double consoleBatteryVoltage = ((parseWord(buf, 87) * 300) / 512) / 100.0;
        String[] icons = parseForecastIcons(buf[89]);
        String forecast = ForecastRules.getText((int) buf[90] & 0xff);
        Date sunrise = parseHourMinute(buf, 91);
        Date sunset = parseHourMinute(buf, 93);
        // Initialize a CurrentRecord instance with the collected values.
        CurrentRecord rec = new CurrentRecord();
        rec.setBarometerTrend(barTrend);
        rec.setConsoleBatteryVolt(consoleBatteryVoltage);
        rec.setForecastIcons(icons);
        rec.setForecastMessage(forecast);
        rec.setSunrise(sunrise);
        rec.setSunset(sunset);
        // My guess is that the battery status will switch from zero to one
        // when there is a low-battery situation in the transmitter.
        rec.setTransmitterBatteryStatus((int) xmitBatteryStatus & 0xff);

        return rec;
    }

    private String[] parseForecastIcons(byte b) {
        String[] tmp = new String[5];
        String[] icons;
        int size = 0;
        if ((b & FORECAST_ICON_RAIN_BIT) != 0) {
            tmp[size++] = CurrentRecord.FORECAST_ICON_RAIN;
        }
        if ((b & FORECAST_ICON_CLOUD_BIT) != 0) {
            tmp[size++] = CurrentRecord.FORECAST_ICON_CLOUD;
        }
        if ((b & FORECAST_ICON_PARTLY_CLOUD_BIT) != 0) {
            tmp[size++] = CurrentRecord.FORECAST_ICON_PARTLY_CLOUD;
        }
        if ((b & FORECAST_ICON_SUN_BIT) != 0) {
            tmp[size++] = CurrentRecord.FORECAST_ICON_SUN;
        }
        if ((b & FORECAST_ICON_SNOW_BIT) != 0) {
            tmp[size++] = CurrentRecord.FORECAST_ICON_SNOW;
        }
        if (size == 5) {
            icons = tmp;
        } else {
            icons = new String[size];
            System.arraycopy(tmp, 0, icons, 0, size);
        }
        return icons;
    }

    protected Date parseHourMinute(byte[] buf, int offset) {
        int w = parseWord(buf, offset);
        int h = w / 100;
        int m = w % 100;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    protected String escape(String string) {
        byte[] bytes = string.getBytes();
        return escape(bytes, 0, bytes.length, true);
    }

    protected String escape(byte[] bytes, int offset, int length, boolean printWritable) {
        if (offset > length) {
            throw new IllegalArgumentException("offset " + offset + " is greater than length " + length);
        }
        StringBuilder buf = new StringBuilder();
        for (int i = offset; i < (offset + length); i++) {
            switch (bytes[i]) {
                case '\n':
                    buf.append(printWritable ? "\\n" : "<0x0a>");
                    break;
                case '\r':
                    buf.append(printWritable ? "\\r" : "<0x0d>");
                    break;
                case '\t':
                    buf.append(printWritable ? "\\t" : "<0x09>");
                    break;
                case 0x06:
                    buf.append(printWritable ? "<ACK>" : "<0x06>");
                    break;
                case 0x18:
                    buf.append(printWritable ? "<CAN>" : "<0x18>");
                    break;
                case 0x21:
                    buf.append(printWritable ? "<NAK>" : "<0x21>");
                    break;
                default:
                    if (bytes[i] < 0x20 || bytes[i] > 0x7e || !printWritable) {
                        String s = Integer.toHexString((int) bytes[i] & 0x000000ff);
                        buf.append("<0x");
                        if (s.length() == 1) {
                            buf.append('0');
                        }
                        buf.append(s);
                        buf.append('>');
                    } else {
                        buf.append((char) bytes[i]);
                    }
                    break;
            }
        }
        return buf.toString();
    }

    protected void log(String prefix, String string) {
    //System.out.println(prefix + escape(string));
    }

    protected void log(String prefix, byte[] bytes, int offset, int length) {
    //System.out.println(prefix + escape(bytes, offset, length, false));
    }
}
