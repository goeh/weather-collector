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
import se.technipelago.weather.vantagepro.CRCOutputStream;
import se.technipelago.weather.vantagepro.CRC16;
import se.technipelago.weather.vantagepro.VantageUtil;
import se.technipelago.weather.emulator.Command;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

/**
 * Download data from the weather station.
 * Currently it does not support incremental downloads.
 * It always download 5 demo packets, regardless of the argument
 * specified in the constructor.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class Download implements Command {

    private boolean downloadAll;

    public Download(boolean all) {
        this.downloadAll = all;
    }

    public void execute(Socket connection) throws IOException {
        CRCOutputStream out = new CRCOutputStream(connection.getOutputStream());
        InputStream in = connection.getInputStream();
        out.write(Constants.ACK);
        // Read 2 bytes date, 2 bytes time and 2 bytes crc.
        byte[] buf = readBytes(in, 6);
        if (buf.length != 6) {
            out.write(0x21);
            return;
        }
        // 6 byte was read, check CRC.
        if (!checkCRC(buf)) {
            out.write(0x18);
            return;
        }
        // Parse date and time.
        Date timeStamp = VantageUtil.getTime(buf, 0);
        System.out.println("Download requested from " + timeStamp);
        
        out.write(Constants.ACK);
        out.resetCRC();
        out.write(new byte[] {2, 0}); // Number of pages to send.
        out.write(new byte[] {0, 0}); // Location within the first page of the first record.
        out.writeCRC();

        // Wait for ACK.
        if (in.read() != Constants.ACK) {
            System.out.println("Download cancelled by client");
            return; // Download cancelled by client.
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);

        
        // Send pages.
        int numPages = 2;
        for (int page = 0; page < numPages; page++) {
            out.resetCRC();
            out.write((byte) 0); // Page number.
            for (int seq = 0; seq < 5; seq++) {
                // There are 5 data records in each page.
                Date ts = cal.getTime();
                out.write(VantageUtil.getDate(ts));
                out.write(VantageUtil.getTime(ts));
                out.write(VantageUtil.getTemperature(17 + seq * 2 + Math.random())); // Outside temp
                out.write(VantageUtil.getTemperature(20 + seq * 3 + Math.random())); // Hight out temp
                out.write(VantageUtil.getTemperature(15 + seq + Math.random())); // Low out temp
                out.write(VantageUtil.getRain(1.6)); // Rain fall
                out.write(VantageUtil.getRain(1.2)); // High Rain rate
                out.write(getBytes((int)(VantageUtil.millibar2inchHg(1012) * 1000.0))); // Barometer
                out.write(getBytes(742)); // Solar Radiation
                out.write(getBytes(123)); // Number of Wind Packets
                out.write(VantageUtil.getTemperature(22.5)); // Inside Temperature
                out.write((byte) 56); // Inside Humidity
                out.write((byte) 72); // Outside Humidity
                out.write((byte) VantageUtil.ms2mph(4.5)); // Average Wind Speed (MPH)
                out.write((byte) VantageUtil.ms2mph(12)); // High Wind Speed
                out.write((byte) Constants.WIND_DIR_SE); // Direction of High Wind Speed
                out.write((byte) Constants.WIND_DIR_S); // Prevailing/Dominant Wind Direction
                out.write((byte) (3.1 * 10)); // Average UV
                out.write((byte) 0); // ET
                out.write(getBytes(960)); // High Solar Radiation
                out.write((byte) (3.9 * 10)); // High UV Index
                out.write((byte) 193); // Forecast Rule
                out.write(255); // Leaf Temperature 1 (unit is degreeF + 90)
                out.write(255); // Leaf Temperature 2
                out.write(255); // Leaf Wetness 1
                out.write(255); // Leaf Wetness 2

                out.write(255); // Soil Temperature 1
                out.write(255); // Soil Temperature 2
                out.write(255); // Soil Temperature 3
                out.write(255); // Soil Temperature 4

                out.write((byte) 0); // Download Record Type 0xff = Rev A, 0x00 = Rev B

                out.write(255); // Extra Humidity 1
                out.write(255); // Extra Humidity 2
                out.write(255); // Extra Temp 1
                out.write(255); // Extra Temp 2
                out.write(255); // Extra Temp 3

                out.write(255); // Soil Moisture 1 (unit is cb)
                out.write(255); // Soil Moisture 2
                out.write(255); // Soil Moisture 3
                out.write(255); // Soil Moisture 4
                
                cal.add(Calendar.MINUTE, -15);
            }

            // 4 unused bytes.
            out.write((byte) 0);
            out.write((byte) 0);
            out.write((byte) 0);
            out.write((byte) 0);
            out.writeCRC();

            // Wait for ACK before we send the next page.
            if (in.read() != Constants.ACK) {
                break; // Download cancelled by client.
            }
        }

        out.flush();
    }

    private byte[] readBytes(InputStream in, int length) throws IOException {
        byte[] buf = new byte[length];
        for(int i = 0; i < length; i++) {
            int c = in.read();
            if(c != -1) {
                buf[i] = (byte)(c & 0xff);
            } else {
                return new byte[0];
            }
        }
        
        return buf;
    }
    
    private byte[] getBytes(int value) {
        return VantageUtil.getBytes(value);
    }

    private boolean checkCRC(byte[] buf) {
        return CRC16.check(buf, 0, buf.length);
    }

}
