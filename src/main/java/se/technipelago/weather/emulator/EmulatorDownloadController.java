package se.technipelago.weather.emulator;

import se.technipelago.weather.vantagepro.DownloadController;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author Goran Ehrsson
 * @since 1.0
 */
public class EmulatorDownloadController extends DownloadController {
    public void cleanup() {
        try {
            writeString("QUIT\n");
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Cannot stop the receiver thread", ex);
        }
        super.cleanup();
    }
}
