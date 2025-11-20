package com.example.noisemonitor.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_API_URL = "api"; // The key for SharedPreferences
    private static final String DEFAULT_API_URL = "http://192.168.0.10:8000"; // Default URL without /api

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
        executorService.execute(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String baseUrl = prefs.getString(KEY_API_URL, DEFAULT_API_URL);
                // Construct the full URL by adding /api here
                URL url = new URL(baseUrl + "/" + KEY_API_URL + "/device-info");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonObject = new JSONObject(content.toString());
                    Device device = new Device(jsonObject);
                    callback.onSuccess(device);
                } else {
                    throw new Exception("HTTP Error: " + responseCode);
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
}
