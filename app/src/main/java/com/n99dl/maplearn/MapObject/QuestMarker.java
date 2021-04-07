package com.n99dl.maplearn.MapObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

public class QuestMarker {
    private Marker marker;
    private Circle circle;
    private long id;

    public long getId() {
        return id;
    }

    public QuestMarker(Marker marker, Circle circle, long id) {
        this.marker = marker;
        this.circle = circle;
        this.id = id;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void selfRemoveFormMap() {
        marker.remove();
        circle.remove();
    }
}
