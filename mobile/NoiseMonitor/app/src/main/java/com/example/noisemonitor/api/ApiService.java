package com.example.noisemonitor.api;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import org.json.JSONArray;
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

    private String getBaseUrl() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API, DEFAULT_API_URL) + "/api";
    }

    // --- API Methods using OkHttp ---

    public void refreshDeviceConnection(ApiCallback<Device> callback) {
        Request request = new Request.Builder().url(getBaseUrl() + "/device-info").build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isDeviceAvailable = false;
                callback.onError(e);
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) throw new IOException("Unexpected code " + response);
                    Device device = new Device(new JSONObject(responseBody.string()));
                    isDeviceAvailable = true;
                    callback.onSuccess(device);
                } catch (Exception e) {
                    isDeviceAvailable = false;
                    callback.onError(e);
                }
            }
        });
    }

    public void getHistoryEvents(@NonNull String date, boolean onlyCritical, ApiCallback<List<HistoryEvent>> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/events?date=" + date + "&only_critical=" + (onlyCritical ? 1 : 0);
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) throw new IOException("Unexpected code " + response);
                    JSONArray jsonArray = new JSONArray(responseBody.string());
                    List<HistoryEvent> events = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        events.add(new HistoryEvent(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(events);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getAlerts(@NonNull String date, boolean successfullySent, ApiCallback<List<AlertEvent>> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/notifications?date=" + date;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) throw new IOException("Unexpected code " + response);
                    JSONArray jsonArray = new JSONArray(responseBody.string());
                    List<AlertEvent> alerts = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        alerts.add(new AlertEvent(jsonArray.getJSONObject(i)));
                    }
                    callback.onSuccess(alerts);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getNoiseStats(@NonNull String date, ApiCallback<NoiseStats> callback) {
        if (!isDeviceAvailable) {
            callback.onError(new IllegalStateException("Device is not available."));
            return;
        }
        String url = getBaseUrl() + "/noise-stats?date=" + date;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) throw new IOException("Unexpected code " + response);
                    NoiseStats stats = new NoiseStats(new JSONObject(responseBody.string()));
                    callback.onSuccess(stats);
                } catch (Exception e) {
                    callback.onError(e);
                }
            }
        });
    }
}
