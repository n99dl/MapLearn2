package com.n99dl.maplearn.data;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
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
    private List<Quest> allQuest;
    private MapsActivity mapsActivity;
    private boolean questListready = false;
    private List<Long> questDoneIds;
    private Quiz quiz;

    private Player player;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(final Player _player) {
        this.player = _player;
        playerDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(player.getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("doneQuests").child(player.getId());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    player.setTotalQuestDone((int)snapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference = FirebaseDatabase.getInstance().getReference().child("quiz_highscore").child(player.getId());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    player.setTotalQuizDone((int)snapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        addQuestDoneListener();
    }

    public List<Long> getQuestDoneIds() {
        return questDoneIds;
    }

    public void removeQuest(Long id) {
        availableQuest.remove(GameManager.getInstance().getQuest(id));
    }
    public void addQuestDoneListener(){
        questDoneIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("doneQuests").child(player.getId());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                questDoneIds.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    HashMap hashMap = (HashMap) dataSnapshot.getValue();
                    Long id = (Long)hashMap.get("id");
                    questDoneIds.add(id);
                    removeQuest(id);
                    Log.d("marker remove", "gamemanager");
                    //not already removed
                    if (mapsActivity.getQuestMarker(id) != null)
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

    public boolean isQuestListready() {
        return questListready;
    }

    public void markQuestAsDone() {
        if (selectingQuest == null)
            return;
        if (questDoneIds.contains(selectingQuest.getId()))
            return;
        //update quest
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", GameManager.getInstance().getSelectingQuest().getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("doneQuests").child(player.getId());
        reference.push().setValue(hashMap);
        changMode(GameManager.Mode.MAP_VIEW);
    }
    private GameManager() {
        availableQuest = new ArrayList<>();
        allQuest = new ArrayList<>();
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
                    allQuest.add(quest);
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
        for (Quest quest: allQuest) {
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
        return selectingQuest;
    }
    public void clearSelectingQuest() {
        selectingQuest = null;
    }

    public void setSelectingQuest(Quest selectingQuest) {
        this.selectingQuest = selectingQuest;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void loadQuiz() {
        quiz = new Quiz();
        reference = FirebaseDatabase.getInstance().getReference().child("quest_quizs").child(String.valueOf(selectingQuest.getId()));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Question question = dataSnapshot.getValue(Question.class);
                    quiz.getQuestionList().add(question);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void setQuestScore(int correctAnsCount) {
        float score = ((float)correctAnsCount)/((float)quiz.getQuestionList().size());
        quiz.setCurrentHighScore(Math.max(score, quiz.getCurrentHighScore()));
        reference = FirebaseDatabase.getInstance().getReference()
                .child("quiz_highscore")
                .child(GameManager.getInstance().getPlayer().getId());
        HashMap<String, Float> hashMap = new HashMap<>();
        hashMap.put("score", score);
        reference.push().setValue(hashMap);
        if (score >= 0.8) {
            markQuestAsDone();
        }
    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        GameManager.INSTANCE = null;
    }

    //Singleton
    public static synchronized GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }



}
