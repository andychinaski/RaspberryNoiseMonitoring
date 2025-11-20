package com.example.noisemonitor.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Device {
    private final String name;
    private final String formattedUptime;
    private final int measurementFrequency;
    private final int warningThreshold;
    private final int criticalThreshold;

    public Device(JSONObject json) throws JSONException {
        this.name = json.getString("device_name");
        long uptimeSeconds = json.getLong("uptime");
        this.formattedUptime = formatUptime(uptimeSeconds);
        this.measurementFrequency = json.getInt("measurement_frequency");
        this.warningThreshold = json.getInt("warning_threshold");
        this.criticalThreshold = json.getInt("critical_threshold");
    }

    private String formatUptime(long seconds) {
        long hours = TimeUnit.SECONDS.toHours(seconds);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getUptime() { return formattedUptime; } // Return formatted uptime
    public int getMeasurementFrequency() { return measurementFrequency; }
    public int getWarningThreshold() { return warningThreshold; }
    public int getCriticalThreshold() { return criticalThreshold; }
}
