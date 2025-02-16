package com.example.theeventhub;

public class Eventlist {

    private int eventID;
    private String eventName;
    private String eventDate;
    private String eventTime;
    private String eventLocation;
    private String eventStatus;
    private String image;
    private String eventType;

    // Constructor without eventStatus
    public Eventlist(int eventID, String eventName, String eventDate, String eventTime, String eventType, String eventLocation, String image) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.eventLocation = eventLocation;
        this.image = image;
        this.eventStatus = null; // Default value
    }

    // Constructor with eventStatus
    public Eventlist(int eventID, String eventName, String eventDate, String eventTime, String eventType, String eventLocation, String image, String eventStatus) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.eventLocation = eventLocation;
        this.image = image;
        this.eventStatus = eventStatus;
    }

    public int getEventID() {
        return eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getImage() {
        return image;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public String getEventType() {
        return eventType;
    }

}
