package com.n99dl.maplearn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.n99dl.maplearn.data.GameManager;
import com.n99dl.maplearn.data.Quest;

import de.hdodenhof.circleimageview.CircleImageView;

public class QuizPrepareActivity extends AppCompatActivity {

    private TextView tv_quest_name;
    private CircleImageView iv_quest_image;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_prepare);

        tv_quest_name = findViewById(R.id.tv_quest_name);
        iv_quest_image = findViewById(R.id.iv_quest_image);
        btn_start = findViewById(R.id.btn_start);

        Quest quest = GameManager.getInstance().getSelectingQuest();
        GameManager.getInstance().loadQuiz();
        if (quest == null)
        {
            finish();
            Log.d("quiz test", "onCreate: NO QUEST");
        }
        else {
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
        }
    }
}
