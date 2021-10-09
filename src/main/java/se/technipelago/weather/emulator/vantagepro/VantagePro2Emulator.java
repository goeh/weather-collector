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
import org.apache.logging.log4j.Logger;
import se.technipelago.weather.emulator.Command;
import se.technipelago.weather.emulator.Emulator;
import se.technipelago.weather.emulator.ServerCommand;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Davis Vantage Pro 2 Emulator.
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class VantagePro2Emulator implements Emulator {

    private static final Logger log = LogManager.getLogger(VantagePro2Emulator.class);

    private static final int BUF_LENGTH = 256;
    private byte[] inputBuffer = new byte[BUF_LENGTH];
    private boolean keepReading = true;
    private Socket connection;

    public VantagePro2Emulator(Socket connection) {
        this.connection = connection;
    }

    /**
     * Close down the emulator.
     */
    public void close() {
        keepReading = false;
    }

    /**
     * This is the main loop that listen for and process commands from a client.
     */
    public void run() {
        try {
            InputStream is = connection.getInputStream();
            while (keepReading) {
                byte[] input = readLine(is);
                if(input != null) {
                    Command cmd = getCommand(input);
                    if (cmd != null) {
                        log.debug("Executing " + cmd.getClass().getName());
                        cmd.execute(connection);
                    } else {
                        connection.getOutputStream().write("ERROR\n\r".getBytes());
                    }
                } else {
                    keepReading = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Read bytes until NEWLINE.
     * <br>NOTE This method is not the same as {@link se.technipelago.weather.vantagepro.VantageUtil#readLine(InputStream)}
     * which reads until CR.
     *
     * @param is the input stream to read bytes from.
     * @return return a byte array up to but not including the NL or CR.
     * @throws java.io.IOException if the read operation fails.
     */
    private byte[] readLine(InputStream is) throws IOException {
        int idx = 0;
        int character;
        while ((character = is.read()) != -1) {
            if (character == '\n') {
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


        if (character == -1) { // EOF
            return null;
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
        if (idx > 0) {
            System.arraycopy(inputBuffer, 0, input, 0, idx);
        }

        return input;
    }

    /**
     * Returns a command instance ready to perform the specified action.
     *
     * @param input the client command line and optional arguments.
     * @return a command instance representing the client command given.
     */
    private Command getCommand(final byte[] input) {
        if (input.length == 0) {
            return new Wakeup();
        }
        if (compareBytes(input, "TEST")) {
            return new Test();
        }
        if (compareBytes(input, new byte[]{'W', 'R', 'D', 0x12, 0x4d})) {
            return new StationType();
        }
        if (compareBytes(input, "RXCHECK")) {
            return new RxCheck();
        }
        if (compareBytes(input, "VER")) {
            return new Version();
        }
        if (compareBytes(input, "LOOP")) {
            return new Loop(Integer.parseInt(new String(input, 5, input.length - 5)));
        }
        if (compareBytes(input, "DMP")) {
            return new Download(true);
        }
        if (compareBytes(input, "DMPAFT")) {
            return new Download(false);
        }
        if (compareBytes(input, "GETTIME")) {
            return new GetTime();
        }
        if (compareBytes(input, "SETTIME")) {
            return new SetTime();
        }

        /*
         * Commands that controls the server and emulator.
         */
        if (compareBytes(input, "QUIT")) {
            EmulatorCommand cmd = new EmulatorCommand(this);
            cmd.quit();
            return cmd;
        }
        if (compareBytes(input, "SHUTDOWN") || compareBytes(input, "KILL")) {
            ServerCommand cmd = new ServerCommand(this);
            cmd.shutdown();
            return cmd;
        }

        return null;
    }

    /**
     * Compare a String and a byte array. If the byte array starts with the
     * same bytes as the string, true is returned.
     *
     * @param input the bytes to compare.
     * @param compare the bytes must match this reference string.
     * @return true if the byte array starts with the same bytes as the string
     */
    private boolean compareBytes(final byte[] input, final String compare) {
        return compareBytes(input, compare.getBytes());
    }

    /**
     * Compare two byte arrays.
     *
     * @param input the bytes to compare.
     * @param compare the reference byte array.
     * @return true if <code>input</code> starts with the same bytes as <code>compare</code>.
     */
    private boolean compareBytes(final byte[] input, final byte[] compare) {
        if(input.length < compare.length) {
            return false;
        }
        for (int i = 0; i < compare.length; i++) {
            if (compare[i] != input[i]) {
                return false;
            }
        }
        return true;
    }
}
