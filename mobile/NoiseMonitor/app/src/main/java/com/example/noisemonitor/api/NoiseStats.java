package com.example.noisemonitor.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoiseStats {

    private final int minNoise;
    private final int maxNoise;
    private final int currentNoise;
    private final String currentTimestamp;
    private final String eventType;
    private final int notificationsSent;
    private final List<NoisePoint> last10Minutes;

    public static class NoisePoint {
        public final String timestamp;
        public final int noiseLevel;

        public NoisePoint(JSONObject json) throws JSONException {
            this.timestamp = json.getString("timestamp");
            this.noiseLevel = json.getInt("noise_level");
        }
    }

    public NoiseStats(JSONObject json) throws JSONException {
        minNoise = json.getInt("min_noise");
        maxNoise = json.getInt("max_noise");
        currentNoise = json.getInt("current_noise");
        currentTimestamp = json.getString("current_timestamp");
        eventType = json.getString("event_type");
        notificationsSent = json.getInt("notifications_sent");

        last10Minutes = new ArrayList<>();
        JSONArray pointsArray = json.getJSONArray("last_10_minutes");
        for (int i = 0; i < pointsArray.length(); i++) {
            last10Minutes.add(new NoisePoint(pointsArray.getJSONObject(i)));
        }
    }

    // --- Getters ---
    public int getMinNoise() { return minNoise; }
    public int getMaxNoise() { return maxNoise; }
    public int getCurrentNoise() { return currentNoise; }
    public String getCurrentTimestamp() { return currentTimestamp; }
    public String getEventType() { return eventType; }
    public int getNotificationsSent() { return notificationsSent; }
    public List<NoisePoint> getLast10Minutes() { return last10Minutes; }
}
