package nl.tudelft.davisstreaming;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DavisMessage {
    private String uuid;
    private float latitude;
    private float longitude;
    private float altitude;
    private String ts;
    private float tempOut;
    private float tempIn;
    private int humOut;
    private int humIn;
    private int barometer;
    private float rain;
    private float rainRate;
    private float windAvg;
    private int windDir;
    private float windHigh;
    private int solar;
    private float uv;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public float getTempOut() {
        return tempOut;
    }

    public void setTempOut(float tempOut) {
        this.tempOut = tempOut;
    }

    public float getTempIn() {
        return tempIn;
    }

    public void setTempIn(float tempIn) {
        this.tempIn = tempIn;
    }

    public int getHumOut() {
        return humOut;
    }

    public void setHumOut(int humOut) {
        this.humOut = humOut;
    }

    public int getHumIn() {
        return humIn;
    }

    public void setHumIn(int humIn) {
        this.humIn = humIn;
    }

    public int getBarometer() {
        return barometer;
    }

    public void setBarometer(int barometer) {
        this.barometer = barometer;
    }

    public float getRain() {
        return rain;
    }

    public void setRain(float rain) {
        this.rain = rain;
    }

    public float getRainRate() {
        return rainRate;
    }

    public void setRainRate(float rainRate) {
        this.rainRate = rainRate;
    }

    public float getWindAvg() {
        return windAvg;
    }

    public void setWindAvg(float windAvg) {
        this.windAvg = windAvg;
    }

    public int getWindDir() {
        return windDir;
    }

    public void setWindDir(int windDir) {
        this.windDir = windDir;
    }

    public float getWindHigh() {
        return windHigh;
    }

    public void setWindHigh(float windHigh) {
        this.windHigh = windHigh;
    }

    public int getSolar() {
        return solar;
    }

    public void setSolar(int solar) {
        this.solar = solar;
    }

    public float getUv() {
        return uv;
    }

    public void setUv(float uv) {
        this.uv = uv;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uuid;
        private float latitude;
        private float longitude;
        private float altitude;
        private String ts;
        private float tempOut;
        private float tempIn;
        private int humOut;
        private int humIn;
        private int barometer;
        private float rain;
        private float rainRate;
        private float windAvg;
        private int windDir;
        private float windHigh;
        private int solar;
        private float uv;

        public Builder uuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder latitude(float latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(float longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder altitude(float altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder ts(String ts) {
            this.ts = ts;
            return this;
        }

        public Builder tempOut(float tempOut) {
            this.tempOut = tempOut;
            return this;
        }

        public Builder tempIn(float tempIn) {
            this.tempIn = tempIn;
            return this;
        }

        public Builder humOut(int humOut) {
            this.humOut = humOut;
            return this;
        }

        public Builder humIn(int humIn) {
            this.humIn = humIn;
            return this;
        }

        public Builder barometer(int barometer) {
            this.barometer = barometer;
            return this;
        }

        public Builder rain(float rain) {
            this.rain = rain;
            return this;
        }

        public Builder rainRate(float rainRate) {
            this.rainRate = rainRate;
            return this;
        }

        public Builder windAvg(float windAvg) {
            this.windAvg = windAvg;
            return this;
        }

        public Builder windDir(int windDir) {
            this.windDir = windDir;
            return this;
        }

        public Builder windHigh(float windHigh) {
            this.windHigh = windHigh;
            return this;
        }

        public Builder solar(int solar) {
            this.solar = solar;
            return this;
        }

        public Builder uv(float uv) {
            this.uv = uv;
            return this;
        }

        public DavisMessage build() {
            final DavisMessage msg = new DavisMessage();

            msg.setUuid(uuid);
            msg.setLatitude(latitude);
            msg.setLongitude(longitude);
            msg.setAltitude(altitude);
            msg.setTs(ts);
            msg.setTempOut(tempOut);
            msg.setTempIn(tempIn);
            msg.setHumOut(humOut);
            msg.setHumIn(humIn);
            msg.setBarometer(barometer);
            msg.setRain(rain);
            msg.setRainRate(rainRate);
            msg.setWindAvg(windAvg);
            msg.setWindDir(windDir);
            msg.setWindHigh(windHigh);
            msg.setSolar(solar);
            msg.setUv(uv);

            return msg;
        }
    }
}
