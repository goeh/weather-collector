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
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author goran
 */
public class GetTime implements Command {

    public void execute(Socket connection) throws IOException {
        byte[] data = getTimeAsBytes(new Date());
        byte[] crc = CRC16.calculate(data);

        OutputStream out = connection.getOutputStream();
        out.write(Constants.ACK);
        out.write(data);
        out.write(crc);
        out.flush();
    }

    /**
     * Return time as byte array.
     * @param time
     * @return
     */
    public static byte[] getTimeAsBytes(Date time) {
        Calendar now = Calendar.getInstance();
        now.setTime(time);
        int year = now.get(Calendar.YEAR) - 1900;
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);

        return new byte[]{(byte) second, (byte) minute, (byte) hour,
            (byte) day, (byte) month, (byte) year};
    }
}
