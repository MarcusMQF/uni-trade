package com.example.unitrade;

public class Device {
    private String name;
    private String location;
    private String lastActive;
    private boolean isCurrent;
    private String type;

    public Device(String name, String location, String lastActive, boolean isCurrent, String type) {
        this.name = name;
        this.location = location;
        this.lastActive = lastActive;
        this.isCurrent = isCurrent;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getLastActive() {
        return lastActive;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public String getType() {
        return type;
    }
}
