package se.technipelago.weather;

import se.technipelago.weather.vantagepro.DownloadController;

public class Downloader {

    public static void main(String[] args) {
        final Controller controller = new DownloadController();
        controller.start(args);
    }
}
