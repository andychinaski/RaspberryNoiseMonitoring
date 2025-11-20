package com.example.noisemonitor.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void getDeviceData(ApiCallback<Device> callback) {
        // ... (implementation is correct)
    }

    public void getHistoryEvents(@NonNull String date, boolean onlyCritical, ApiCallback<List<HistoryEvent>> callback) {
        // ... (implementation is correct)
    }

    public void getAlerts(@NonNull String date, boolean successfullySent, ApiCallback<List<AlertEvent>> callback) {
        executorService.execute(() -> {
            try {
                String baseUrl = getBaseUrl();
                // The API doesn't seem to use the 'successfullySent' flag, so we ignore it for now.
                URL url = new URL(baseUrl + "/notifications?date=" + date);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(content.toString());
                    List<AlertEvent> alerts = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        alerts.add(new AlertEvent(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(alerts);
                } else {
                    throw new Exception("HTTP Error: " + connection.getResponseCode());
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private String getBaseUrl() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API, DEFAULT_API_URL) + "/api";
    }
}
