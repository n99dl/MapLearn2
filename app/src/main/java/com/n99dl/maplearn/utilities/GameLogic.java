package com.n99dl.maplearn.utilities;


import android.location.Location;
import android.util.Log;

import com.n99dl.maplearn.data.MyLocation;
import com.n99dl.maplearn.data.Quest;
import com.n99dl.maplearn.data.Player;

public class GameLogic {

    public static final int MIN_DISTANCE_TO_QUEST = 150; //150m
    public static final double MIN_DISTANCE_TO_QUEST_IN_LATLONG = 0.0014; //150m

    public static boolean isLocationInQuestRadius(Quest quest, MyLocation location) {
        float[] results = new float[1];
        Location.distanceBetween(quest.getLatitude(), quest.getLongitude(), location.getLatitude(), location.getLongitude(), results);
        float distance = results[0];
        Log.d("Distance test: ", "" + distance);
        return distance <= MIN_DISTANCE_TO_QUEST;
    }

    public static boolean isInQuestRadius(Quest quest, Player player) {
        return GameLogic.isLocationInQuestRadius(quest, player.getLocation());
    }


}
