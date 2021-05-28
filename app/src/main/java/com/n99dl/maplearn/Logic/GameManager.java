package com.n99dl.maplearn.Logic;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.n99dl.maplearn.Activities.MapsActivity;
import com.n99dl.maplearn.Model.Quest;
import com.n99dl.maplearn.Model.Question;
import com.n99dl.maplearn.Notification.Token;

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


    private List<Quest> availableQuest;
    private List<Quest> allQuest;
    private MapsActivity mapsActivity;
    private boolean questListready = false;
    private List<Long> questDoneIds;
    private Quiz quiz;
    private Player player;
    //Inintialize
    private GameManager() {
        availableQuest = new ArrayList<>();
        allQuest = new ArrayList<>();
        this.mode = Mode.MAP_VIEW;
        //LoadQuest();
    }
    //Get main player
    public Player getPlayer() {
        return player;
    }
    //Set main player
    public void setPlayer(final Player _player) {
        this.player = _player;
        playerDataReference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_USER).child(player.getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_DONE_QUEST).child(player.getId());
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
        reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_QUIZ_HIGHSCORE).child(player.getId());
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
        updateToken(FirebaseInstanceId.getInstance().getToken());
        addQuestDoneListener();
    }
    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(GameManager.getInstance().getPlayer().getId()).setValue(token1);
    }
    public void removeQuest(Long id) {
        availableQuest.remove(GameManager.getInstance().getQuest(id));
    }
    public void addQuestDoneListener(){
        questDoneIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_DONE_QUEST).child(player.getId());
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
    public void markQuestAsDone() {
        if (selectingQuest == null)
            return;
        if (questDoneIds.contains(selectingQuest.getId()))
            return;
        //update quest
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", GameManager.getInstance().getSelectingQuest().getId());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_DONE_QUEST).child(player.getId());
        reference.push().setValue(hashMap);
        changMode(GameManager.Mode.MAP_VIEW);
    }
    public void setMapsActivity(MapsActivity activity) {
        this.mapsActivity = activity;
        LoadQuest();
    }
    public void LoadQuest() {
        reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_QUEST_SET).child("set_1");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Quest","Firebase quest list count: " + snapshot.getChildrenCount());
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    Quest quest = postSnapshot.getValue(Quest.class);
                    availableQuest.add(quest);
                    allQuest.add(quest);
                }
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
        reference = FirebaseDatabase.getInstance().getReference().child(DatabaseKey.KEY_QUEST_QUIZ).child(String.valueOf(selectingQuest.getId()));
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
        DatabaseReference highscore_reference = FirebaseDatabase.getInstance().getReference()
                .child(DatabaseKey.KEY_QUIZ_HIGHSCORE)
                .child(GameManager.getInstance().getPlayer().getId())
                .child(String.valueOf(getSelectingQuest().getId()))
                .child("score");
        highscore_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double highscore = snapshot.getValue(Double.class);
                    quiz.setCurrentHighScore(highscore - DatabaseKey.FlOAT_OFFSET);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public List<Quest> getAvailableQuest() {
        return availableQuest;
    }
    public void setQuestScore(int correctAnsCount) {
        double score = ((double)correctAnsCount)/((double)quiz.getQuestionList().size());
        quiz.setCurrentHighScore(Math.max(score, quiz.getCurrentHighScore()));
        reference = FirebaseDatabase.getInstance().getReference()
                .child(DatabaseKey.KEY_QUIZ_HIGHSCORE)
                .child(GameManager.getInstance().getPlayer().getId())
                .child(String.valueOf(getSelectingQuest().getId()));
        HashMap<String, Double> hashMap = new HashMap<>();
        score += DatabaseKey.FlOAT_OFFSET;
        hashMap.put("score", score);
        reference.setValue(hashMap);
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
