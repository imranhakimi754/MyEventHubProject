package com.example.theeventhub;

public class EventDetails {
    private String eventName;
    private String organizerName;
    private String eventDate;
    private String eventTime;
    private String eventEnd;
    private String eventDescription;
    private String eventType;
    private String eventVenue;
    private String image;

    public EventDetails(String eventName, String organizerName, String eventDate, String eventTime, String eventEnd, String eventDescription, String eventType, String eventVenue, String image) {
        this.eventName = eventName;
        this.organizerName = organizerName;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventEnd = eventEnd;
        this.eventDescription = eventDescription;
        this.eventType = eventType;
        this.eventVenue = eventVenue;
        this.image = image;
    }


    public String getEventName() {
        return eventName;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventTime(){
        return eventTime;
    }

    public String getEventEnd() { return eventEnd;}

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public String getImage(){ return image; }
}

