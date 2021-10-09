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

import org.apache.logging.log4j.LogManager;
import se.technipelago.weather.emulator.Command;
import se.technipelago.weather.vantagepro.CRC16;
import se.technipelago.weather.vantagepro.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author goran
 */
public class Loop implements Command, Runnable {

    private static boolean active;
    private OutputStream outputStream;
    private int repetitions;

    public static void stop() {
        active = false;
    }

    public void execute(Socket connection) throws IOException {
        if (Loop.active) {
            return;
        }
        new Thread(new Loop(repetitions, connection.getOutputStream())).start();
    }

    /**
     * This constructor is called by the emulator.
     *
     * @param reps
     */
    public Loop(int reps) {
        this.repetitions = reps;
    }

    /**
     * This constructor is called by the command instance when it creates the runner thread.
     *
     * @param reps
     * @param out
     */
    public Loop(int reps, OutputStream out) {
        this.repetitions = reps;
        this.outputStream = out;
    }

    public void run() {
        Loop.active = true;
        try {
            outputStream.write(Constants.ACK);
            for (int i = 0; i < repetitions; i++) {
                if (!active) {
                    return;
                }
                byte[] bytes = getPacket(i + 1);
                outputStream.write(bytes);
                CRC16 crc = new CRC16();
                crc.add(bytes);
                outputStream.write(crc.getCrc());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        } catch (IOException e) {
            LogManager.getLogger(Loop.class).error(e.getMessage());
        } finally {
            Loop.active = false;
        }
    }

    /**
     * TODO: Write correct data packet.
     *
     * @param num packet number
     * @return bytes for the LOOP data packet
     */
    private byte[] getPacket(int num) {
        byte[] packet = new byte[97];

        // Fake packet begins here.
        byte[] fake = ("LOOP " + String.valueOf(num)).getBytes();
        Arrays.fill(packet, (byte) '.');
        System.arraycopy(fake, 0, packet, 0, fake.length);
        packet[packet.length - 1] = '\r';
        packet[packet.length - 2] = '\n';
        // Fake packet ends here.

        return packet;
    }
}
