package com.n99dl.maplearn.data;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private List<Question> questionList;
    private float currentHighScore;

    public Quiz() {
        questionList = new ArrayList<>();
        currentHighScore = 0;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public float getCurrentHighScore() {
        return currentHighScore;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public void setCurrentHighScore(float currentHighScore) {
        this.currentHighScore = currentHighScore;
    }
}
