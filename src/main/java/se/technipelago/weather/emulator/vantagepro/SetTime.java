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
import se.technipelago.weather.emulator.Command;
import se.technipelago.weather.vantagepro.VantageUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author goran
 */
public class SetTime implements Command {

    public void execute(Socket connection) throws IOException {
        OutputStream out = connection.getOutputStream();
        InputStream in = connection.getInputStream();

        out.write(Constants.ACK);

        // Read 6 bytes time and 2 bytes crc.
        byte[] buf = readBytes(in, 8);
        if (buf.length != 8) {
            out.write(0x21);
            return;
        }
        // 8 byte was read, check CRC.
        if (!checkCRC(buf)) {
            out.write(0x18);
            return;
        }
        out.write(Constants.ACK);
        // Parse date and time.
        Date timeStamp = VantageUtil.getTime(buf, 0);
        System.out.println("Setting console time to " + timeStamp);
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

    private boolean checkCRC(byte[] buf) {
        return CRC16.check(buf, 0, buf.length);
    }

}
