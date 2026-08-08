package com.zybooks.evidencetracker;

/*
 * File: EvidenceItem.java
 * Author: Albert White
 * Course: CS360
 * Description:
 * Represents an evidence item with its properties.
 * Stores case ID, item description, status, location, and date/time.
 */
public class EvidenceItem {
    // Properties
    private final String caseId;
    private final String itemDescription;
    private final String status;
    private final String location;
    private final String dateTime;

    // Constructor
    public EvidenceItem(String caseId, String itemDescription, String status, String location, String dateTime) {
        this.caseId = caseId;
        this.itemDescription = itemDescription;
        this.status = status;
        this.location = location;
        this.dateTime = dateTime;
    }

    // Getters
    public String getCaseId() {
        return caseId;
    }

    // Getters
    public String getItemDescription() {
        return itemDescription;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    // Getters
    public String getLocation() {
        return location;
    }

    // Getters
    public String getDateTime() {
        return dateTime;
    }
}
