package com.n99dl.maplearn.Logic;

public class DatabaseKey {
    public static String KEY_USER = "Users";
    public static String KEY_QUEST_SET = "quest_sets";
    public static String KEY_LOCATION = "all_users_locations";
    public static String KEY_QUEST_QUIZ = "quest_quizs";
    public static String KEY_QUIZ_HIGHSCORE = "quiz_highscore";
    public static String KEY_WAVE_LIST = "Wave_list";
    public static String KEY_DONE_QUEST = "done_quest";
    //this value only cause long to happen if the quiz have 1e7 question
    public static Double FlOAT_OFFSET = 0.000000001;
}
