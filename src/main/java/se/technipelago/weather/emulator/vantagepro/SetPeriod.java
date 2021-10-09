package se.technipelago.weather.emulator.vantagepro;

import se.technipelago.weather.emulator.Command;
import se.technipelago.weather.vantagepro.Constants;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * @author Goran Ehrsson
 * @since 1.5.0
 */
public class SetPeriod implements Command {

    private static final List<Integer> validPeriods = Arrays.asList(1, 5, 10, 15, 30, 60, 120);

    private final int minutes;

    public SetPeriod(int minutes) {
        this.minutes = minutes;
    }

    @Override
    public void execute(Socket connection) throws IOException {
        if (validPeriods.contains(minutes)) {
            System.out.println("Archive interval set to " + minutes + " minutes");
            connection.getOutputStream().write(Constants.ACK);
        } else {
            connection.getOutputStream().write(Constants.NAK);
        }
    }
}
