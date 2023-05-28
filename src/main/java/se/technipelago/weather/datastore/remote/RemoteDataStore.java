package se.technipelago.weather.datastore.remote;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.technipelago.weather.archive.ArchiveRecord;
import se.technipelago.weather.archive.CurrentRecord;
import se.technipelago.weather.datastore.DataStore;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by goran on 15-06-13.
 */
public class RemoteDataStore implements DataStore {

    private static final String PROPERTIES_FILE = "collector.properties";

    protected final Logger log = LogManager.getLogger(getClass().getName());

    private String url;
    private String clientKey;
    private String clientSecret;

    private String name;

    private String getPrefix() {
        return name != null ? name + "_" : "";
    }

    @Override
    public void init(Properties prop) {
        name = Optional.ofNullable(prop.getProperty("name"))
                .map(s -> StringUtils.isBlank(s) ? null : s.trim())
                .orElse(null);
        url = prop.getProperty("url");
        if (StringUtils.isEmpty(url)) {
            log.error("Property 'url' must be set");
            return;
        }
        clientKey = prop.getProperty("client.key");
        if (StringUtils.isEmpty(clientKey)) {
            log.error("Property 'client.key' must be set");
            return;
        }
        clientSecret = prop.getProperty("client.secret");
        if (StringUtils.isEmpty(clientSecret)) {
            log.error("Property 'client.secret' must be set");
            return;
        }
    }

    public void cleanup() {

    }

    public Date getLastRecordTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public boolean insertData(ArchiveRecord rec) throws IOException {

        if (url == null || url.trim().length() == 0) {
            log.debug("No REST service configured");
            return false;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = dateFormat.format(rec.getTimestamp());
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        buf.append("  \"clientKey\": \"" + clientKey + "\",\n");
        buf.append("  \"clientSecret\": \"" + clientSecret + "\",\n");
        buf.append("  \"data\": [\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "outsideTemperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getOutsideTemperature() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "outsideHumidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getOutsideHumidity() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "windSpeed\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getWindSpeedAvg() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "windGusts\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getWindSpeedHigh() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "windDirection\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getWindDirection() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "barometer\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getBarometer() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "rain\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getRainFall() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "sun\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getSolarRadiation() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "uv\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getUvIndex() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "extra1Temperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraTemperature1() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "extra2Temperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraTemperature2() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "extra3Temperature\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraTemperature3() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "extra1Humidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraHumidity1() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "extra2Humidity\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getExtraHumidity2() + "\n");
        buf.append("    }\n");

        buf.append("  ]\n");
        buf.append("}\n");

        httpPost.setEntity(new StringEntity(buf.toString(), ContentType.create("application/json")));

        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        log.debug("Weather data for " + timestamp + " sent to " + url);

        return false;
    }

    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        return null;
    }

    public void updateCurrent(CurrentRecord rec) throws IOException {

        if (url == null || url.trim().length() == 0) {
            log.debug("No REST service configured");
            return;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = dateFormat.format(rec.getTimestamp());
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        buf.append("  \"clientKey\": \"" + clientKey + "\",\n");
        buf.append("  \"clientSecret\": \"" + clientSecret + "\",\n");
        buf.append("  \"data\": [\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "barometerTrend\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getBarometerTrend() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "icons\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getForcastIconMask() + "\n");
        buf.append("    }\n");
/*
        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "sunrise\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getSunrise().getTime() + "\n");
        buf.append("    },\n");

        buf.append("    {\n");
        buf.append("      \"sid\": \"" + getPrefix() + "sunset\",\n");
        buf.append("      \"timestamp\": \"" + timestamp + "\",\n");
        buf.append("      \"value\": " + rec.getSunset().getTime() + "\n");
        buf.append("    }\n");
*/
        buf.append("  ]\n");
        buf.append("}\n");

        httpPost.setEntity(new StringEntity(buf.toString(), ContentType.create("application/json")));

        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            HttpEntity entity = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }

        log.debug("Current data for " + timestamp + " sent to " + url);
    }
}
