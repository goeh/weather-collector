package se.technipelago.weather.vantagepro;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import se.technipelago.weather.emulator.Server;

public class EmulatorTests {

    private static Server server;

    @BeforeAll
    public static void startEmulator() {
        new Thread(() -> {
            server = new Server();
            server.start(0);
        }).start();
    }

    @AfterAll
    public static void stopEmulator() {
        Server.shutdown();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void downloadFromEmulator() {
        DownloadController ctrl = new DownloadController();
        ctrl.start(new String[]{"localhost", String.valueOf(server.getPort())});
    }
}
