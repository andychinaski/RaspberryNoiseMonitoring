package com.example.noisemonitor.api;

public class AlertEvent {
    private final String time;
    private final String message;

    public AlertEvent(String time, String message) {
        this.time = time;
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
