package com.n99dl.maplearn.Logic;

import com.n99dl.maplearn.Model.Question;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private List<Question> questionList;
    private double currentHighScore;

    public Quiz() {
        questionList = new ArrayList<>();
        currentHighScore = 0;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public double getCurrentHighScore() {
        return currentHighScore;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public void setCurrentHighScore(double currentHighScore) {
        this.currentHighScore = currentHighScore;
    }
}
