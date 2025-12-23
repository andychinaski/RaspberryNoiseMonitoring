package com.example.noisemonitor.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertEvent {

    private final String formattedSentAt;
    private final String message;
    private final String status;

    public AlertEvent(JSONObject json) throws JSONException {
        this.message = json.getString("message");
        this.status = json.getString("status");

        // Parse and reformat sent_at timestamp
        String formattedTimestamp;
        try {
            String apiTimestamp = json.getString("sent_at"); // e.g., "2025-11-21 00:00:12"
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
        this.formattedSentAt = formattedTimestamp;
    }

    // --- Getters ---
    public String getFormattedSentAt() {
        return formattedSentAt;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}
