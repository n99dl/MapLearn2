package com.n99dl.maplearn.data;

public class Quest {
    private double latitude;
    private double longitude;
    private long id;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public Quest(double latitude, double longitude, long id, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
    }

    public Quest() {
    }

    public String toString() {
        return "{" + this.name + ", " + this.latitude + ", " + this.longitude + "}";
    }
}
