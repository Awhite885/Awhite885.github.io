package com.zybooks.evidencetracker;

public class AuditLogEntry {

    private final String dateTime;
    private final String action;
    private final String changes;
    private final String performedBy;

    public AuditLogEntry(
            String dateTime,
            String action,
            String changes,
            String performedBy) {

        this.dateTime = dateTime;
        this.action = action;
        this.changes = changes;
        this.performedBy = performedBy;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getAction() {
        return action;
    }

    public String getChanges() {
        return changes;
    }

    public String getPerformedBy() {
        return performedBy;
    }
}