package com.n99dl.maplearn.data;

public class User {
    MyLocation location;
    private String id;
    private String name;

    public MyLocation getLocation() {
        return location;
    }

    public void setLocation(MyLocation location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User() {
    }

    public User(MyLocation location, String id, String name) {
        this.location = location;
        this.id = id;
        this.name = name;
    }
}
