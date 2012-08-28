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

import se.technipelago.weather.emulator.vantagepro.VantagePro2;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Weather station emulator server.
 * 
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class Server {

    private static boolean keepRunning = true;

    /**
     * This is the entry point of the emulator server.
     * The server listens for connections and creates a new emulator instance
     * for each connection. Each emulator instance runs in it's own thread.
     * 
     * @param args command line arguments args[0] is the TCP port number to listen to
     */
    public static void main(String[] args) {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8888;
        ServerSocket socket;
        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(10000);
        } catch (IOException e) {
            throw new RuntimeException("Error initializing simulator", e);
        }

        System.out.println("Emulator Server listening on port " + port);

        while (keepRunning) {
            try {
                Socket connection = socket.accept();
                if (keepRunning) {
                    new Thread(getEmulator(connection)).start();
                } else {
                    connection.getOutputStream().write("Service unavailable".getBytes());
                    connection.close();
                }
            } catch (SocketTimeoutException e) {
            // Ignore.
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Return a new weather station emulator instance.
     * TODO move hard coded class to configuration.
     */
    private static Emulator getEmulator(Socket connection) {
        return new VantagePro2(connection);
    }
    
    /**
     * Shutdown the emulator server.
     */
    public static void shutdown() {
        keepRunning = false;
    }
}
