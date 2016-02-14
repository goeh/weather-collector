package se.technipelago.weather.archive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import se.technipelago.opensensor.OpenSensorPayload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Send weather data to opensensor.net.
 */
public class OpenSensorDataStore implements DataStore {

    private static final String PROPERTIES_FILE = "opensensor.properties";

    private static final Logger log = Logger.getLogger(OpenSensorDataStore.class.getName());

    private Properties getProperties() {
        final Properties prop = new Properties();
        InputStream fis = null;
        try {
            File file = new File(PROPERTIES_FILE);
            if (file.exists()) {
                fis = new FileInputStream(file);
                prop.load(fis);
            } else {
                log.log(Level.WARNING, PROPERTIES_FILE + " not found, data will not be uploaded");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
        return prop;
    }

    @Override
    public void init() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public Date getLastRecordTime() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -6);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    @Override
    public boolean insertData(ArchiveRecord rec) throws IOException {

        final Properties prop = getProperties();
        String url = prop.getProperty("opensensor.url");
        if (url == null || url.trim().length() == 0) {
            log.fine("No REST service configured");
            return false;
        }

        String username = prop.getProperty("opensensor.username");
        String password = prop.getProperty("opensensor.password");
        String clientId = prop.getProperty("opensensor.clientId");
        String clientSecret = prop.getProperty("opensensor.clientSecret");
        String tokenUrl = prop.getProperty("opensensor.accessTokenUri");

        String accessToken = getAccessToken(URI.create(tokenUrl), username, password, clientId, clientSecret);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        try {
            List<String> probes = getCollectorProbes(prop);
            List<OpenSensorPayload> payloads = createPayload(rec, probes, prop);
            ObjectMapper objectMapper = objectMapper();

            httpPost.setHeader("Authorization", "Bearer " + accessToken);

            for (OpenSensorPayload payload : payloads) {

                JsonNode jsonNode = objectMapper.valueToTree(payload);
                String jsonString = objectMapper.writeValueAsString(jsonNode);

                httpPost.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

                CloseableHttpResponse response = httpclient.execute(httpPost);

                try {
                    HttpEntity entity = response.getEntity();
                    System.out.println(response.getStatusLine() + " " + EntityUtils.toString(entity));
                    //EntityUtils.consume(entity);
                } finally {
                    response.close();
                }

                log.fine("Weather data for " + payload.getSid() + " sent to " + url);
            }
        } finally {
            httpPost.releaseConnection();
            httpclient.close();
        }

        return false;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }

    private List<String> getCollectorProbes(Properties prop) {
        List<String> result = new ArrayList<>();
        String line = prop.getProperty("collector.values");
        if (line != null && line.trim().length() > 0) {
            String[] values = line.split(",");
            for (int i = 0; i < values.length; i++) {
                result.add(values[i].trim());
            }
        }
        return result;
    }

    private List<OpenSensorPayload> createPayload(ArchiveRecord rec, List<String> probes, Properties prop) {
        List<OpenSensorPayload> result = new ArrayList<>();

        for (String probe : probes) {
            OpenSensorPayload payload = new OpenSensorPayload();
            String sid = prop.getProperty("collector." + probe + ".sid");
            if (sid == null) {
                throw new IllegalArgumentException("Property collector." + probe + ".sid must be set");
            }
            payload.setSid(sid);
            payload.addValue(rec.getTimestamp(), (Number) getFieldValue(rec, probe));
            result.add(payload);
        }

        return result;
    }

    private Object getFieldValue(ArchiveRecord rec, String fieldName) {
        try {
            Field field = rec.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(rec);
        } catch (NoSuchFieldException e) {
            log.severe(e.getMessage());
        } catch (IllegalAccessException e) {
            log.severe(e.getMessage());
        }
        return null;
    }

    @Override
    public Date updateStatus(Date lastDownload, Date lastRecord) throws IOException {
        return lastRecord;
    }

    @Override
    public void updateCurrent(CurrentRecord current) throws IOException {

    }

    private String getAccessToken(URI url, String username, String password, String clientId, String clientSecret) throws IOException {
        String accessToken = null;
        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(clientId, clientSecret));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        try {
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(target, basicAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("username", username));
            urlParameters.add(new BasicNameValuePair("password", password));
            urlParameters.add(new BasicNameValuePair("grant_type", "password"));
            urlParameters.add(new BasicNameValuePair("scope", "sensor data"));
            urlParameters.add(new BasicNameValuePair("client_id", clientId));
            urlParameters.add(new BasicNameValuePair("client_secret", clientSecret));

            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

            CloseableHttpResponse response = httpclient.execute(target, httpPost, localContext);

            try {
                JsonNode json = objectMapper().readTree(response.getEntity().getContent());
                accessToken = json.get("access_token").asText();
            } finally {
                response.close();
                httpPost.releaseConnection();
            }

        } catch (ClientProtocolException e) {
            log.severe(e.getMessage());
        } catch (IOException e) {
            log.severe(e.getMessage());
        } finally {
            httpclient.close();
        }
        return accessToken;
    }
}
