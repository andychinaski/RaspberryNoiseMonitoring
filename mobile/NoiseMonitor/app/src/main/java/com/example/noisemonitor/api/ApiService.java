package com.example.noisemonitor.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_API = "api";
    private static final String DEFAULT_API_URL = "http://192.168.0.10:8000";

    private static volatile ApiService INSTANCE;
    private final ExecutorService executorService;
    private final Context context;

    // --- Device State ---
    private volatile boolean isDeviceAvailable = false;

    // Parser interface to decouple network logic from parsing logic
    private interface JsonParser<T> {
        T parse(String jsonString) throws JSONException;
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private ApiService(Context context) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.context = context.getApplicationContext();
    }

    public static ApiService getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ApiService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ApiService(context);
                }
            }
        }
        return INSTANCE;
    }

    // --- Public API Methods ---

    public boolean isDeviceAvailable() {
        return isDeviceAvailable;
    }

    public void refreshDeviceConnection(ApiCallback<Device> callback) {
        String url = getBaseUrl() + "/device-info";
        executeRequest(url, jsonString -> new Device(new JSONObject(jsonString)), new ApiCallback<Device>() {
            @Override
            public void onSuccess(Device result) {
                isDeviceAvailable = true;
                callback.onSuccess(result);
            }

            @Override
            public void onError(Exception e) {
                isDeviceAvailable = false;
                callback.onError(e);
            }
        });
    }

    public void getHistoryEvents(@NonNull String date, boolean onlyCritical, ApiCallback<List<HistoryEvent>> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/events?date=" + date + "&only_critical=" + (onlyCritical ? 1 : 0);
        executeRequest(url, jsonString -> {
            JSONArray jsonArray = new JSONArray(jsonString);
            List<HistoryEvent> events = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                events.add(new HistoryEvent(jsonArray.getJSONObject(i)));
            }
            return events;
        }, callback);
    }

    public void getAlerts(@NonNull String date, boolean successfullySent, ApiCallback<List<AlertEvent>> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/notifications?date=" + date;
        executeRequest(url, jsonString -> {
            JSONArray jsonArray = new JSONArray(jsonString);
            List<AlertEvent> alerts = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                alerts.add(new AlertEvent(jsonArray.getJSONObject(i)));
            }
            return alerts;
        }, callback);
    }

    public void getNoiseStats(@NonNull String date, ApiCallback<NoiseStats> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/noise-stats?date=" + date;
        executeRequest(url, jsonString -> new NoiseStats(new JSONObject(jsonString)), callback);
    }

    // --- Private Helper Methods ---

    private <T> void executeRequest(String urlString, JsonParser<T> parser, ApiCallback<T> callback) {
        executorService.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // Disable GZIP compression. This can prevent "unexpected end of stream" errors.
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String jsonString = readStream(connection.getInputStream());
                    T result = parser.parse(jsonString);
                    callback.onSuccess(result);
                } else {
                    String errorString = readStream(connection.getErrorStream());
                    throw new Exception("HTTP Error: " + responseCode + " - " + errorString);
                }
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
        }
        return content.toString();
    }

    private String getBaseUrl() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API, DEFAULT_API_URL) + "/api";
    }
}
