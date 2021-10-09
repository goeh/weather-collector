package se.technipelago.weather.vantagepro;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.technipelago.weather.Controller;
import se.technipelago.weather.emulator.EmulatorDownloadController;
import se.technipelago.weather.emulator.Server;

public class EmulatorTests {

    private static Server server;

    @BeforeAll
    public static void startEmulator() {
        new Thread(() -> {
            server = new Server();
            server.start(0);
        }).start();
        // Wait for server to accept connections.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void stopEmulator() {
        Server.shutdown();
        // Wait for server to close connections.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void downloadFromEmulator() {
        Controller ctrl = new EmulatorDownloadController();
        ctrl.start(new String[]{"localhost", String.valueOf(server.getPort())});
    }
}
