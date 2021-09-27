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
package se.technipelago.weather.archive;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a POJO that hold current (live) values from the weather station.
 * The following MySQL TABLE can be used to persist the data:<br>
 * <pre>
 * CREATE TABLE current (
 *  id integer primary key auto_increment,
 *  bar_trend smallint,
 *  console_battery float,
 *  forecast_icons varchar(28),
 *  forecast_msg varchar(255),
 *  sunrise datetime,
 *  sunset datetime,
 *  ts datetime not null,
 *  transmit_battery smallint
 * );
 * </pre>
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class CurrentRecord {

    public static final byte BAROMETER_TREND_FALLING_RAPIDLY = -60;
    public static final byte BAROMETER_TREND_FALLING_SLOWLY = -20;
    public static final byte BAROMETER_TREND_STEADY = 0;
    public static final byte BAROMETER_TREND_RISING_SLOWLY = 20;
    public static final byte BAROMETER_TREND_RISING_RAPIDLY = 60;
    public static final String FORECAST_ICON_RAIN = "rain";
    public static final String FORECAST_ICON_CLOUD = "cloud";
    public static final String FORECAST_ICON_PARTLY_CLOUD = "partly_cloud";
    public static final String FORECAST_ICON_SUN = "sun";
    public static final String FORECAST_ICON_SNOW = "snow";
    private int barometerTrend;
    private double consoleBatteryVolt;
    private String[] forecastIcons;
    private String forecastMessage;
    private Date sunrise;
    private Date sunset;
    private Date timestamp;
    private int transmitterBatteryStatus;


    private static final Map<String, Integer> icons = new HashMap<String, Integer>();

    static {
        icons.put(FORECAST_ICON_RAIN, 0x01);
        icons.put(FORECAST_ICON_CLOUD, 0x02);
        icons.put(FORECAST_ICON_PARTLY_CLOUD, 0x04);
        icons.put(FORECAST_ICON_SUN, 0x08);
        icons.put(FORECAST_ICON_SNOW, 0x10);
    }

    public CurrentRecord() {
        this.timestamp = new Date();
    }

    public int getBarometerTrend() {
        return barometerTrend;
    }

    public void setBarometerTrend(final int barometerTrend) {
        this.barometerTrend = barometerTrend;
    }

    public double getConsoleBatteryVolt() {
        return consoleBatteryVolt;
    }

    public void setConsoleBatteryVolt(final double consoleBatteryVolt) {
        this.consoleBatteryVolt = consoleBatteryVolt;
    }

    public String[] getForecastIcons() {
        return forecastIcons;
    }

    public void setForecastIcons(final String[] forecastIcons) {
        this.forecastIcons = new String[forecastIcons.length];
        System.arraycopy(forecastIcons, 0, this.forecastIcons, 0, forecastIcons.length);
    }

    public int getForecastIconMask() {
        int mask = 0;
        for(String icon : getForecastIcons()) {
            mask |= icons.get(icon);
        }
        return mask;
    }

    public String getForecastMessage() {
        return forecastMessage;
    }

    public void setForecastMessage(final String forecastMessage) {
        this.forecastMessage = forecastMessage;
    }

    public Date getSunrise() {
        return sunrise;
    }

    public void setSunrise(final Date sunrise) {
        this.sunrise = sunrise;
    }

    public Date getSunset() {
        return sunset;
    }

    public void setSunset(final Date sunset) {
        this.sunset = sunset;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getTransmitterBatteryStatus() {
        return transmitterBatteryStatus;
    }

    public void setTransmitterBatteryStatus(int transmitterBatteryStatus) {
        this.transmitterBatteryStatus = transmitterBatteryStatus;
    }
}
