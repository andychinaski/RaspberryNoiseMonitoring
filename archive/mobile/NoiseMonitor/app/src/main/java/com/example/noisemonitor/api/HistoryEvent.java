package com.example.noisemonitor.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryEvent {
    public enum Status { NORMAL, WARNING, CRITICAL, UNKNOWN }

    private final String time;
    private final int dbLevel;
    private final Status status;

    public HistoryEvent(JSONObject json) throws JSONException {
        this.dbLevel = json.getInt("noise_level");

        // Parse and reformat timestamp to dd.MM.yyyy HH:mm:ss
        String formattedTimestamp;
        try {
            String apiTimestamp = json.getString("timestamp"); // e.g., "2025-11-20 23:00:09"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(apiTimestamp);
            if (date != null) {
                formattedTimestamp = outputFormat.format(date);
            } else {
                formattedTimestamp = "00.00.0000 00:00:00"; // Fallback for parsing error
            }
        } catch (Exception e) {
            formattedTimestamp = "00.00.0000 00:00:00"; // Fallback value on any exception
        }
        this.time = formattedTimestamp;

        // Safely parse the status enum
        Status parsedStatus;
        try {
            parsedStatus = Status.valueOf(json.getString("type").toUpperCase());
        } catch (IllegalArgumentException e) {
            parsedStatus = Status.UNKNOWN;
        }
        this.status = parsedStatus;
    }

    // --- Getters ---
    public String getTime() {
        return time;
    }

    public int getDbLevel() {
        return dbLevel;
    }

    public Status getStatus() {
        return status;
    }
}
