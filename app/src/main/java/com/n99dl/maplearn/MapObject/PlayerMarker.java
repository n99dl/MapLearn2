package com.n99dl.maplearn.MapObject;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Marker;

public class PlayerMarker {
    private Marker marker;
    private String id;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PlayerMarker() {
    }

    public PlayerMarker(Marker marker, String id) {
        this.marker = marker;
        this.id = id;
    }
}
