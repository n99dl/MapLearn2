package com.n99dl.maplearn.data;

public class MarkerType {
    public enum Type {
        PLAYER,
        QUEST,
        OTHER_PLAYER
    }
    private Quest quest;
    private User player;
    private Type type;

    public MarkerType(Type type) {
        this.type = type;
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
