package com.n99dl.maplearn;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.n99dl.maplearn.data.GameManager;
import com.n99dl.maplearn.data.Quiz;

public class QuizScoreActivity extends AppCompatActivity {

    private TextView tv_correctCount, tv_score, tv_announce;
    private Button btn_retry, btn_done;
    private RelativeLayout bac_dim_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_score);

        tv_correctCount = findViewById(R.id.tv_correctCount);
        tv_score = findViewById(R.id.tv_score);
        tv_announce = findViewById(R.id.tv_announce);
        btn_retry = findViewById(R.id.btn_retry);
        btn_done = findViewById(R.id.btn_done);
        bac_dim_layout = findViewById(R.id.bac_dim_layout);

        Intent intent = getIntent();
        long correctAnsCount = intent.getIntExtra("correctCount", 0);
        Quiz quiz = GameManager.getInstance().getQuiz();
        if (quiz == null)
        {
            Log.d("quiz test", "onCreate: NO QUIZ FOUND");
            finish();
        }

        float score = ((float)correctAnsCount)/((float)quiz.getQuestionList().size()) * 100;
        tv_correctCount.setText("" + correctAnsCount + "/" + GameManager.getInstance().getQuiz().getQuestionList().size());
        String scoreText = String.format("%.0f", score) + "%";
        tv_score.setText(scoreText);
        if (score < 80) {
            tv_announce.setText("Your score is almost good enough. Press Retry to retake quiz");
            tv_score.setTextColor(Color.RED);
        } else {
            findViewById(R.id.quiz_score_root_activity).post(new Runnable() {
                @SuppressLint("NewApi")
                public void run() {
                    showQuestDonePopupWindowClick(findViewById(R.id.quiz_score_root_activity));
                }
            });
        }

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizScoreActivity.this, QuestionActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showQuestDonePopupWindowClick(View view) {

        bac_dim_layout.setVisibility(View.VISIBLE);
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.quest_done_popup_window, null);
        TextView tv_pop_up = popupView.findViewById(R.id.tv_pop_up);
        tv_pop_up.setText("Congratulation! Your quest in " + GameManager.getInstance().getSelectingQuest().getName() + " is done!");

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = false; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                bac_dim_layout.setVisibility(View.INVISIBLE);
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
