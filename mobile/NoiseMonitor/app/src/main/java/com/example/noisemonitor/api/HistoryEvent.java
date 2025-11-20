package com.example.noisemonitor.api;

public class HistoryEvent {
    public enum Status { NORMAL, WARNING, CRITICAL }

    private final String time;
    private final int dbLevel;
    private final Status status;

    public HistoryEvent(String time, int dbLevel, Status status) {
        this.time = time;
        this.dbLevel = dbLevel;
        this.status = status;
    }

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
