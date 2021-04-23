package com.n99dl.maplearn.data;

public class Player {
    private MyLocation location;
    private String id;
    private String username;
    private String imageURL;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Player() {
    }

    public Player(MyLocation location, String id, String username, String imageURL) {
        this.location = location;
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
    }

    public Player(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.imageURL = user.getImageURL();
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
