package com.n99dl.maplearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.n99dl.maplearn.data.GameManager;
import com.n99dl.maplearn.data.Quest;

import java.util.HashMap;

public class QuestActivity extends AppCompatActivity {

    private Button btn_socialize;

    Intent intent;

    Quest quest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);
        intent = getIntent();
        final Long id = intent.getLongExtra("questId", -1);
        quest = GameManager.getInstance().getQuest(id);
        if (quest == null)
            finish();


        btn_socialize = findViewById(R.id.btn_socialize);
        btn_socialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                HashMap<String, Object> hashMap = new HashMap<>();
//                hashMap.put("id", quest.getId());
//                GameManager.getInstance().getPlayerDataReference().child("doneQuests").push().setValue(hashMap);
//                finish();
                GameManager.getInstance().changMode(GameManager.Mode.NEARBY_PLAYER_QUEST);
                GameManager.getInstance().setSelectingQuest(quest);
                Intent intent = new Intent(QuestActivity.this, MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }
}
