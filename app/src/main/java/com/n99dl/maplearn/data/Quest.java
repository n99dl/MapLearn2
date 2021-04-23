package com.n99dl.maplearn.data;

public class Quest {
    private double latitude;
    private double longitude;
    private long id;
    private String imageURL;

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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    private String name;

    public Quest(double latitude, double longitude, long id, String name, String imageURL) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
    }

    public Quest() {
    }

    public String toString() {
        return "{" + this.name + ", " + this.latitude + ", " + this.longitude + "}";
    }
}
