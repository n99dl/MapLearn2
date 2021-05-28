package com.n99dl.maplearn.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.n99dl.maplearn.Logic.DatabaseKey;
import com.n99dl.maplearn.Logic.GameManager;
import com.n99dl.maplearn.Model.Quest;
import com.n99dl.maplearn.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class QuizPrepareActivity extends AppCompatActivity {

    private TextView tv_quest_name, tv_highscore;
    private CircleImageView iv_quest_image;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_prepare);

        tv_quest_name = findViewById(R.id.tv_quest_name);
        iv_quest_image = findViewById(R.id.iv_quest_image);
        btn_start = findViewById(R.id.btn_start);
        tv_highscore = findViewById(R.id.tv_highscore);

        Quest quest = GameManager.getInstance().getSelectingQuest();
        GameManager.getInstance().loadQuiz();
        if (quest == null)
        finish();
        tv_quest_name.setText(quest.getName());
        if (quest.getImageURL().equals("default")) {
            iv_quest_image.setImageResource(R.mipmap.ic_profile_default);
        } else {
            Glide.with(getApplicationContext()).load(quest.getImageURL()).into(iv_quest_image);
        }
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizPrepareActivity.this, QuestionActivity.class);
                startActivity(intent);
                finish();
            }
        });
        readHighscore();
    }

    private void readHighscore() {
        DatabaseReference highscore_reference = FirebaseDatabase.getInstance().getReference()
                .child(DatabaseKey.KEY_QUIZ_HIGHSCORE)
                .child(GameManager.getInstance().getPlayer().getId())
                .child(String.valueOf(GameManager.getInstance().getSelectingQuest().getId()))
                .child("score");
        highscore_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double percent = (snapshot.getValue(Double.class) - DatabaseKey.FlOAT_OFFSET) * 100;
                    tv_highscore.setText("Your highscore: " + percent + "%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
