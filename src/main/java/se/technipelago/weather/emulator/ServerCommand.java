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
package se.technipelago.weather.emulator;

import java.io.IOException;
import java.net.Socket;

/**
 * This command controls the emulator server.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class ServerCommand implements Command {

    private final Emulator emulator;
    private boolean shutdown;

    public ServerCommand(Emulator arg) {
        this.emulator = arg;
    }

    /**
     * Execute the command. If the {@link #shutdown} method has been called
     * prior to execute, the emulator server will be shutdown.
     * 
     * @param out &quot;Shutting down...&quot; will be written to this output stream.
     * @throws java.io.IOException if the message cannot be written.
     */
    public void execute(Socket connection) throws IOException {
        if (shutdown) {
            emulator.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            // Ignore.
            }
            Server.shutdown();
        }
    }

    /**
     * This method will configure the command to shutdown the emulator server
     * once the command is executed.
     */
    public void shutdown() {
        this.shutdown = true;
    }
}
