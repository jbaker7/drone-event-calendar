package com.jbaker7.dronecalendar;

public class Event {
    private int id;
    private String name;
    private String start;
    private String end;
    private String description;
    private int attendees;
    private String type;
    private int createdBy;

    public Event (int id, String name, String start, String end, String description, int attendees, String type, int createdBy) {
        this.setId(id);
        this.setName(name);
        this.setStart(start);
        this.setEnd(end);
        this.setDescription(description);
        this.setAttendees(attendees);
        this.setType(type);
        this.setCreatedBy(createdBy);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public int getId () {
        return id;
    }
    public String getName () {
        return name;
    }
    public String getStart () {
        return start;
    }
    public String getEnd () {
        return end;
    }
    public String getDescription () {
        return description;
    }
    public int getAttendees () {
        return attendees;
    }
    public String getType () {
        return type;
    }
    public int getCreatedBy () {
        return createdBy;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setStart(String start) {
        this.start = start;
    }
    public void setEnd(String end) {
        this.end = end;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setAttendees(int attendees) {
        this.attendees = attendees;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
