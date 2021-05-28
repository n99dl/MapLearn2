package com.n99dl.maplearn.Model;

public class WaveDetails {
    private String user;
    private String friend;
    private Long time;

    public WaveDetails(String user, String friend, Long time) {
        this.user = user;
        this.friend = friend;
        this.time = time;
    }

    public WaveDetails() {

    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}