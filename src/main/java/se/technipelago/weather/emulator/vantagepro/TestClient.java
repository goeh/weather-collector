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
package se.technipelago.weather.emulator.vantagepro;

import se.technipelago.weather.vantagepro.Constants;
import se.technipelago.weather.vantagepro.CRC16;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.ArchivePage;
import se.technipelago.weather.vantagepro.VantageUtil;
import se.technipelago.weather.emulator.Command;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class TestClient {

    private static final String IN = "< ";
    private static final String OUT = "> ";
    private String hostname;
    private int port;

    public TestClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8888;
        String hostname = args.length > 0 ? args[0] : "localhost";
        TestClient client = new TestClient(hostname, port);
        client.test();
    }

    public void test() throws IOException {
        Socket connection = null;
        DataOutputStream out = null;
        InputStream in = null;
        try {
            connection = new Socket(hostname, port);
            in = connection.getInputStream();
            out = new DataOutputStream(connection.getOutputStream());
            test(in, out);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        // Ignore.
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            // Ignore.
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            // Ignore.
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (IOException e) {
            // Ignore.
            }
        }
    }

    private void test(InputStream in, DataOutputStream out) throws IOException {
        byte[] buf;

        // Send wakeup command.
        writeString("\n", out);
        expectString("\n\r", in);
        log("\t", "The station is awake.");

        // Test command.
        writeString("TEST\n", out);
        expectString("TEST\n\r", in);

        // Determine station type.
        out.write(new byte[]{'W', 'R', 'D', 0x12, 0x4d, '\n'});
        log(OUT, "WRD<0x12><0x4d>\n");
        buf = readBytes(in, 2);
        if (buf[0] != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        String stationType = getStationType(buf[1]);
        if (stationType == null) {
            throw new IOException("Unsupported station type: " + String.valueOf((int) buf[1]));
        }
        log("\t", "The station is a " + stationType);

        // Firmware version.
        writeString("VER\n", out);
        expectString("\n\rOK\n\r", in);
        byte[] line = VantageUtil.readLine(in);
        log(IN, new String(line));

        // Get current station time.
        writeString("GETTIME\n", out);
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        log(IN, "<ACK>");
        buf = readBytes(in, 8);
        if (!CRC16.check(buf, 0, 8)) {
            throw new IOException("CRC error");
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, (int) buf[0]);
        cal.set(Calendar.MINUTE, (int) buf[1]);
        cal.set(Calendar.HOUR_OF_DAY, (int) buf[2]);
        cal.set(Calendar.DAY_OF_MONTH, (int) buf[3]);
        cal.set(Calendar.MONTH, (int) buf[4] - 1);
        cal.set(Calendar.YEAR, (int) buf[5] + 1900);
        log("\t", "Station Time: " + cal.getTime());

        writeString("DMPAFT\n", out);
        if (in.read() != Constants.ACK) {
            throw new IOException("Invalid response");
        }
        log(IN, "<ACK>");
        Date now = new Date();
        CRC16 crc = new CRC16();
        byte[] dateBytes = VantageUtil.getDate(now);
        byte[] timeBytes = VantageUtil.getTime(now);
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
        buf = readBytes(in, 5);
        if (!CRC16.check(buf, 0, 5)) {
            throw new IOException("CRC error");
        }
        int numPages = (int) buf[1];
        log("\tNumber of pages: ", String.valueOf(numPages));

        out.write(Constants.ACK);
        log(OUT, "<ACK>");

        for (int i = 0; i < numPages; i++) {
            byte[] page = readBytes(in, 267);
            if (!CRC16.check(page, 0, page.length)) {
                throw new IOException("CRC error");
            }
            ArchivePage ap = parsePage(page);
            out.write(Constants.ACK);
            log(OUT, "<ACK>");
        }
        
        writeString("SHUTDOWN\n", out);
    }

    private byte[] readBytes(InputStream in, int length) throws IOException {
        byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            int c = in.read();
            if (c != -1) {
                buf[i] = (byte) (c & 0xff);
            } else {
                throw new IOException("Unexpected EOF");
            }
        }
        log(IN, buf, 0, length);

        return buf;
    }

    private void writeString(String string, DataOutputStream out) throws IOException {
        out.writeBytes(string);
        log(OUT, escape(string));
    }

    private void expectString(String string, InputStream in) throws IOException {
        int length = string.length();
        byte[] buf = new byte[length];
        int n;
        if ((n = in.read(buf, 0, length)) != length) {
            if (n == -1) {
                throw new IOException("Unexpected EOF");
            }
            throw new IOException("Invalid response: " + new String(buf, 0, n));
        }
        log(IN, escape(new String(buf, 0, n)));
    }

    private String escape(String string) {
        byte[] bytes = string.getBytes();
        return escape(bytes, 0, bytes.length, true);
    }

    private String escape(byte[] bytes, int offset, int length, boolean printWritable) {
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

    private void log(String prefix, String string) {
        System.out.println(prefix + escape(string));
    }

    private void log(String prefix, byte[] bytes, int offset, int length) {
        System.out.println(prefix + escape(bytes, offset, length, false));
    }

    private String getStationType(int n) {
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

    private Date parseTimestamp(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        int year = ((word >>> 9) & 0x7f) + 1900;
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
        return cal.getTime();
    }

    private int parseWord(byte[] buf, int offset) {
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
    private double parseTemperature(byte[] buf, int offset) {
        int f = ((int) buf[offset + 1] << 8) | (buf[offset] & 0xff) & 0xffff;
        return VantageUtil.fahrenheit2celcius(f / 10.0);
    }

    /**
     * Return millimeter rain.
     * @param buf
     * @param offset
     * @return
     */
    private double parseRain(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        return word / 5.0;
    }

    private int parseBarometer(byte[] buf, int offset) {
        int word = parseWord(buf, offset);
        double inchHg = word / 1000.0;

        return VantageUtil.inchHg2millibar(inchHg);
    }

    private double parseWindSpeed(byte[] buf, int offset) {
        int mph = buf[offset];
        return mph * 0.45;
    }

    private ArchivePage parsePage(byte[] page) {
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
            rec.setSolarRadiation(parseWord(page, offset + 16));
            // Number of wind samples skipped.
            // Inside temperature skipped.
            // Inside humidity skipped.
            rec.setOutsideHumidity((int) page[offset + 23]);
            rec.setWindSpeedAvg(parseWindSpeed(page, offset + 24));
            rec.setWindSpeedHigh(parseWindSpeed(page, offset + 25));
            // Direction of high wind speed skipped.
            rec.setWindDirection((int) page[offset + 27]);
            rec.setUvIndex((int) page[offset + 28]);

            System.out.println(rec.toString());

            offset += 52;
        }

        return ap;
    }
}
