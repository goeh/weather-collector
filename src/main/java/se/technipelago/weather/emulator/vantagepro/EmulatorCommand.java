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

import se.technipelago.weather.emulator.Command;
import se.technipelago.weather.emulator.Emulator;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This command controls the emulator. It can close the emulator.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class EmulatorCommand implements Command {

    private Emulator emulator;
    private boolean quit;

    public EmulatorCommand(Emulator arg) {
        this.emulator = arg;
    }
    /**
     * Execute the command. If the {@link #quit} method has been called
     * prior to execute, the emulator will quit/exit.
     * 
     * @param connection &quot;Closing...&quot; will be written to this socket's output stream.
     * @throws java.io.IOException if the message cannot be written.
     */
    public void execute(Socket connection) throws IOException {
        if (quit) {
            OutputStream out = connection.getOutputStream();
            out.write("Closing...".getBytes());
            try {
                Thread.sleep(1000);
            } catch(InterruptedException e) {
                // Ignore.
            }
            this.emulator.close();
        }
    }
    /**
     * This method will configure the command to exit the emulator
     * once the command is executed.
     */
    public void quit() {
        this.quit = true;
    }

}
