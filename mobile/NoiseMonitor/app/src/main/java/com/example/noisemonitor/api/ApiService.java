package com.example.noisemonitor.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiService {

    private static final String PREFS_NAME = "NoiseMonitorPrefs";
    private static final String KEY_API = "api";
    private static final String DEFAULT_API_URL = "http://192.168.0.10:8000";

    private static volatile ApiService INSTANCE;
    private final OkHttpClient client;
    private final Context context;

    private volatile boolean isDeviceAvailable = false;

    private interface JsonParser<T> {
        T parse(String jsonString) throws JSONException;
    }

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    private ApiService(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
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

    private <T> void executeRequest(String urlString, JsonParser<T> parser, ApiCallback<T> callback) {
        Request request = new Request.Builder().url(urlString).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        throw new IOException("Request failed with code: " + response.code());
                    }
                    String jsonString = responseBody.string();
                    T result = parser.parse(jsonString);
                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    private String getBaseUrl() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API, DEFAULT_API_URL) + "/api";
    }
}
