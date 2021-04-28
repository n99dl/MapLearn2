package com.n99dl.maplearn;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.n99dl.maplearn.data.GameManager;
import com.n99dl.maplearn.data.Question;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_question, tv_question_count;
    private Button btn_option_1, btn_option_2, btn_option_3, btn_option_4;
    private List<Question> questionList;
    private int score;
    private long currentQuestion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        tv_question = findViewById(R.id.tv_question);
        tv_question_count =findViewById(R.id.tv_question_count);
        btn_option_1 = findViewById(R.id.btn_option_1);
        btn_option_2 = findViewById(R.id.btn_option_2);
        btn_option_3 = findViewById(R.id.btn_option_3);
        btn_option_4 = findViewById(R.id.btn_option_4);

        btn_option_1.setOnClickListener(this);
        btn_option_2.setOnClickListener(this);
        btn_option_3.setOnClickListener(this);
        btn_option_4.setOnClickListener(this);
        
        getQuestionList();
    }

    private void getQuestionList() {
//        questionList = new ArrayList<>();
//        questionList.add(new Question("Question 1","A","B","C","D", 1));
//        questionList.add(new Question("Question 2","oA","oB","oC","oD", 2));
//        questionList.add(new Question("Question 3","bA","bB","bC","bD", 3));
//        questionList.add(new Question("Question 4","4A","4B","4C","4D", 4));
        if (GameManager.getInstance().getQuiz() != null) {
            questionList = GameManager.getInstance().getQuiz().getQuestionList();
        } else {
            finish();
        }

        setQuestion();
    }

    private void setQuestion() {
        tv_question.setText(questionList.get(0).getQuestion());
        btn_option_1.setText(questionList.get(0).getOption1());
        btn_option_2.setText(questionList.get(0).getOption2());
        btn_option_3.setText(questionList.get(0).getOption3());
        btn_option_4.setText(questionList.get(0).getOption4());

        currentQuestion = 0;
        score = 0;

        tv_question_count.setText("" + (currentQuestion+1) + "/" + questionList.size());
    }

    @Override
    public void onClick(View v) {
        int selectedOption = 0;
        switch (v.getId()) {
            case R.id.btn_option_1:
                selectedOption = 1;
                break;
            case R.id.btn_option_2:
                selectedOption = 2;
                break;
            case R.id.btn_option_3:
                selectedOption = 3;
                break;
            case R.id.btn_option_4:
                selectedOption = 4;
                break;
            default:
        }
        checkAnswer(selectedOption, v);
    }

    @SuppressLint("NewApi")
    private void checkAnswer(long selectedOption, View view) {
        if (selectedOption == questionList.get((int)currentQuestion).getCorrectAns()) {
            //Right answer
            score++;
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }
        else {
            //Wrong answer
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            switch ((int)questionList.get((int)currentQuestion).getCorrectAns()) {
                case 1:
                    btn_option_1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    btn_option_2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    btn_option_3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    btn_option_4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextQuestion();
            }
        }, 2000);
    }

    private void nextQuestion() {
        if (currentQuestion == questionList.size()-1) {
            FinishQuiz();
        } else {
            currentQuestion++;
            playAnimation(tv_question, 0, 0);
            playAnimation(btn_option_1, 0, 1);
            playAnimation(btn_option_2, 0, 2);
            playAnimation(btn_option_3, 0, 3);
            playAnimation(btn_option_4, 0, 4);
            tv_question_count.setText("" + (currentQuestion+1) + "/" + questionList.size());
    }
    }

    private void FinishQuiz() {
        GameManager.getInstance().setQuestScore(score);
        Intent intent = new Intent(QuestionActivity.this, QuizScoreActivity.class);
        intent.putExtra("correctCount", score);
        Log.d("quiz test", "FinishQuiz: " + score);
        startActivity(intent);
        finish();
    }

    private void playAnimation(final View view, final int value, final int viewNum) {
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500)
                .setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @SuppressLint("NewApi")
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (value == 0) {
                            switch (viewNum) {
                                case 0:
                                    ((TextView)view).setText(questionList.get((int)currentQuestion).getQuestion());
                                    break;
                                case 1:
                                    ((Button)view).setText(questionList.get((int)currentQuestion).getOption1());
                                    break;
                                case 2:
                                    ((Button)view).setText(questionList.get((int)currentQuestion).getOption2());
                                    break;
                                case 3:
                                    ((Button)view).setText(questionList.get((int)currentQuestion).getOption2());
                                    break;
                                case 4:
                                    ((Button)view).setText(questionList.get((int)currentQuestion).getOption2());
                                    break;
                            }
                            if (viewNum != 0) {
                                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AA03A9F4")));
                            }
                            playAnimation(view, 1 ,viewNum);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }
}
