package com.n99dl.maplearn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Model.Quest;
import com.n99dl.maplearn.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class QuestActivity extends AppCompatActivity {

    private Button btn_socialize, btn_quiz;
    private TextView tv_quest_name, tv_quest_guide;
    private CircleImageView iv_quest_image;

    Intent intent;

    Quest quest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest);
        btn_socialize = findViewById(R.id.btn_socialize);
        btn_quiz = findViewById(R.id.btn_quiz);
        tv_quest_name = findViewById(R.id.tv_quest_name);
        tv_quest_guide = findViewById(R.id.tv_quest_guide);
        iv_quest_image = findViewById(R.id.iv_quest_image);
        intent = getIntent();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        quest = GameManager.getInstance().getSelectingQuest();
        if (quest == null)
            finish();
        tv_quest_name.setText(quest.getName());
        getSupportActionBar().setTitle(quest.getName());
        if (quest.getImageURL().equals("default")) {
            iv_quest_image.setImageResource(R.mipmap.ic_profile_default);
        } else {
            Glide.with(getApplicationContext()).load(quest.getImageURL()).into(iv_quest_image);
        }

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
                finish();
            }
        });

        btn_quiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuestActivity.this, QuizPrepareActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case (android.R.id.home):
//                GameManager.getInstance().clearSelectingQuest();
                Intent intent = new Intent(QuestActivity.this, MapsActivity.class);

                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
