package nl.tudelft.streaming;

//import java.lang.String;

public class DavisMessage {

    String uuid;
    String ts;
    float temp_out;
    float temp_in;
    int hum_out;
    int hum_in;
    int barometer;
    float rain;
    float rain_rate;
    float wind_avg;
    int wind_dir;
    float wind_high;
    int solar;
    float uv;

    public String getTimestamp() {
        return ts;
    }

    public void setTimestamp(String ts) {
        this.ts = ts;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getTempOut() {
        return temp_out;
    }

    public void setTempOut(float temp_out) {
        this.temp_out = temp_out;
    }

    public float getTempIn() {
        return temp_in;
    }

    public void setTempIn(float temp_in) {
        this.temp_in = temp_in;
    }

    public float getHumOut() {
        return hum_out;
    }

    public void setHumOut(int hum_out) {
        this.hum_out = hum_out;
    }

    public int setHumIn() {
        return hum_out;
    }

    public void getHumIn(int hum_in) {
        this.hum_in = hum_in;
    }

    public int setBarometer() {
        return hum_out;
    }

    public void getBarometer(int barometer) {
        this.barometer = barometer;
    }

    public float setRain() {
        return rain;
    }

    public void getRain(float rain) {
        this.rain = rain;
    }

    public float setRainRate() {
        return rain;
    }

    public void getRainRate(float rain_rate) {
        this.rain_rate = rain_rate;
    }

    public float setWindAvg() {
        return wind_avg;
    }

    public void getWindAvg(float wind_avg) {
        this.wind_avg = wind_avg;
    }

    public int setWindDir() {
        return wind_dir;
    }

    public void getWindDir(int wind_dir) {
        this.wind_dir = wind_dir;
    }

    public float setWindHigh() {
        return wind_high;
    }

    public void getWindHigh(float wind_high) {
        this.wind_high = wind_high;
    }

    public int setSolar() {
        return solar;
    }

    public void getSolar(int solar) {
        this.solar = solar;
    }

    public float setUv() {
        return uv;
    }

    public void getUv(float uv) {
        this.uv = uv;
    }

    public DavisMessage() {
        super();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DavisMessage{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append(" ts='").append(ts).append('\'');
        // all the rest
        sb.append('}');
        return sb.toString();
    }

    public DavisMessage(String uuid, String ts, ...) {
        super();
        this.uuid = uuid;
        this.ts = ts;
        //all the rest
    }
}