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

import se.technipelago.weather.vantagepro.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Goran Ehrsson <goran@technipelago.se>
 */
public class ArchiveRecord {

    public static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final NumberFormat tempFormat = DecimalFormat.getNumberInstance();

    static {
        tempFormat.setMaximumFractionDigits(1);
        tempFormat.setMinimumFractionDigits(1);
        tempFormat.setGroupingUsed(false);
    }
    private int recordNumber;
    private Date timestamp;
    private double outsideTemperature;
    private double outsideTemperatureHigh;
    private double outsideTemperatureLow;
    private double insideTemperature;
    private double extraTemperature1;
    private double extraTemperature2;
    private double extraTemperature3;
    private double rainFall;
    private double rainRateHigh;
    private int barometer;
    private int outsideHumidity;
    private int insideHumidity;
    private int extraHumidity1;
    private int extraHumidity2;
    private int solarRadiation;
    private double uvIndex;
    private double windSpeedAvg;
    private double windSpeedHigh;
    private int windDirection;

    public int getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getOutsideTemperature() {
        return outsideTemperature;
    }

    public void setOutsideTemperature(double outsideTemperature) {
        this.outsideTemperature = outsideTemperature;
    }

    public int getBarometer() {
        return barometer;
    }

    public void setBarometer(int barobeter) {
        this.barometer = barobeter;
    }

    public double getOutsideTemperatureHigh() {
        return outsideTemperatureHigh;
    }

    public void setOutsideTemperatureHigh(double outsideTemperatureHigh) {
        this.outsideTemperatureHigh = outsideTemperatureHigh;
    }

    public double getOutsideTemperatureLow() {
        return outsideTemperatureLow;
    }

    public void setOutsideTemperatureLow(double outsideTemperatureLow) {
        this.outsideTemperatureLow = outsideTemperatureLow;
    }

    public int getInsideHumidity() {
        return insideHumidity;
    }

    public void setInsideHumidity(int insideHumidity) { this.insideHumidity = insideHumidity; }

    public int getExtraHumidity1() { return extraHumidity1; }

    public void setExtraHumidity1(int extraHumidity1) { this.extraHumidity1 = extraHumidity1; }

    public int getExtraHumidity2() { return extraHumidity2; }

    public void setExtraHumidity2(int extraHumidity2) { this.extraHumidity2 = extraHumidity2; }

    public double getInsideTemperature() { return insideTemperature; }

    public void setInsideTemperature(double insideTemperature) {
        this.insideTemperature = insideTemperature;
    }

    public double getExtraTemperature1() { return extraTemperature1; }

    public void setExtraTemperature1(double extraTemperature1) { this.extraTemperature1 = extraTemperature1; }

    public double getExtraTemperature2() { return extraTemperature2; }

    public void setExtraTemperature2(double extraTemperature2) { this.extraTemperature2 = extraTemperature2; }

    public double getExtraTemperature3() { return extraTemperature3; }

    public void setExtraTemperature3(double extraTemperature3) { this.extraTemperature3 = extraTemperature3; }

    public double getRainFall() {
        return rainFall;
    }

    public void setRainFall(double rainFall) {
        this.rainFall = rainFall;
    }

    public double getRainRateHigh() {
        return rainRateHigh;
    }

    public void setRainRateHigh(double rainRateHigh) {
        this.rainRateHigh = rainRateHigh;
    }

    public int getOutsideHumidity() {
        return outsideHumidity;
    }

    public void setOutsideHumidity(int outsideHumidity) {
        this.outsideHumidity = outsideHumidity;
    }

    public int getSolarRadiation() {
        return solarRadiation;
    }

    public void setSolarRadiation(int solarRadiation) {
        this.solarRadiation = solarRadiation;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public double getWindSpeedAvg() {
        return windSpeedAvg;
    }

    public void setWindSpeedAvg(double windSpeedAvg) {
        this.windSpeedAvg = windSpeedAvg;
    }

    public double getWindSpeedHigh() {
        return windSpeedHigh;
    }

    public void setWindSpeedHigh(double windSpeedHigh) {
        this.windSpeedHigh = windSpeedHigh;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append('#');
        buf.append(String.valueOf(recordNumber));
        buf.append(' ');
        buf.append(timestamp != null ? dateTimeFormat.format(timestamp) : "null");
        if (outsideTemperature != 32767) {
            buf.append(" <");
            buf.append(tempFormat.format(outsideTemperature));
            buf.append(" \u00B0C>");
        }
        buf.append(" <");
        buf.append(String.valueOf(barometer));
        buf.append(" mb>");
        buf.append(" <rain=");
        buf.append(String.valueOf(rainFall));
        buf.append(" mm>");
        if (outsideHumidity != 255) {
            buf.append(" <hum=");
            buf.append(String.valueOf(outsideHumidity));
            buf.append(" %>");
        }
        if (windSpeedAvg != 255) {
            buf.append(" <wind=");
            buf.append(String.valueOf(windSpeedAvg));
            buf.append(" m/s>");
        }
        if (windDirection != -1) {
            buf.append(" <wind dir=");
            buf.append(Constants.WIND_DIR_NAMES[windDirection]);
            buf.append(">");
        }
        return buf.toString();
    }
}
