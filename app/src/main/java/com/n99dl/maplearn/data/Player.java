package com.n99dl.maplearn.data;

public class Player {
    private MyLocation location;
    private String id;
    private String username;
    private String imageURL;
    private String fullname;
    private String email;
    private int totalQuestDone;
    private int totalQuizDone;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTotalQuestDone() {
        return totalQuestDone;
    }

    public void setTotalQuestDone(int totalQuestDone) {
        this.totalQuestDone = totalQuestDone;
    }

    public int getTotalQuizDone() {
        return totalQuizDone;
    }

    public void setTotalQuizDone(int totalQuizDone) {
        this.totalQuizDone = totalQuizDone;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

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
        this.totalQuestDone = 0;
        this.totalQuizDone = 0;
    }

    public Player(MyLocation location, String id, String username, String imageURL, String fullname, String email) {
        this.location = location;
        this.id = id;
        this.username = username;
        this.imageURL = imageURL;
        this.fullname = fullname;
        this.email = email;
    }

    public Player(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.imageURL = user.getImageURL();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
