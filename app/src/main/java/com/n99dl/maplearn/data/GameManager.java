package com.n99dl.maplearn.data;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.MapsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameManager {
    //Singleton
    public enum Mode {
        MAP_VIEW,
        NEARBY_PLAYER_QUEST
    }
    private static GameManager INSTANCE = null;
    private Mode mode;
    private Quest selectingQuest;

    private DatabaseReference reference, playerDataReference;

    public List<Quest> getAvailableQuest() {
        return availableQuest;
    }

    private List<Quest> availableQuest;
    private MapsActivity mapsActivity;
    private boolean questListready = false;


    public User getPlayer() {
        return player;
    }

    public void setPlayer(final User player) {
        this.player = player;
        playerDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(player.getId());
        addQuestDoneListener();
    }

    public void addQuestDoneListener(){
        final List<Long> questDoneIds = new ArrayList<>();
        playerDataReference.child("doneQuests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questDoneIds.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    Long id = (Long)hashMap.get("id");
                    questDoneIds.add(id);
                    mapsActivity.getQuestMarker(id).selfRemoveFormMap();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        for (Quest quest: availableQuest) {
            if (questDoneIds.contains(quest.getId())) {
                availableQuest.remove(quest);
            }
        }
    }
    public DatabaseReference getPlayerDataReference() {
        return playerDataReference;
    }

    private User player;

    public boolean isQuestListready() {
        return questListready;
    }

    private GameManager() {
        availableQuest = new ArrayList<>();
        this.mode = Mode.MAP_VIEW;
        //LoadQuest();
    }

    public void setMapsActivity(MapsActivity activity) {
        this.mapsActivity = activity;
        LoadQuest();
    }

    public void LoadQuest() {
        reference = FirebaseDatabase.getInstance().getReference().child("QuestSets").child("Set1");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Quest","Firebase quest list count: " + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Quest quest = postSnapshot.getValue(Quest.class);
                    availableQuest.add(quest);
                }
                Log.d("Quest","Client read quests list count: " + availableQuest.size());
                Log.d("Quest","Quest 0: " + availableQuest.get(1).toString());
                if (mapsActivity != null) {
                    mapsActivity.loadQuest();
                    questListready = true;
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public Quest getQuest(long id) {
        for (Quest quest: availableQuest) {
            if (quest.getId() == id)
                return quest;
        }
        return null;
    }

    public void changMode(GameManager.Mode mode) {
        this.mode = mode;
    }

    public GameManager.Mode getGameMode() {
        return this.mode;
    }
    public Quest getSelectingQuest() {
        if (mode == Mode.MAP_VIEW) return null;
        return selectingQuest;
    }

    public void setSelectingQuest(Quest selectingQuest) {
        if (mode == Mode.MAP_VIEW) return;
        this.selectingQuest = selectingQuest;
    }

    //Singleton
    public static synchronized GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }



}
