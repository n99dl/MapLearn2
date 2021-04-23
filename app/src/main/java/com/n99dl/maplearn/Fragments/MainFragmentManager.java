package com.n99dl.maplearn.Fragments;

import android.util.Log;

public class MainFragmentManager {
    //Singleton
    private static MainFragmentManager INSTANCE = null;
    public static synchronized MainFragmentManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MainFragmentManager();
        }
        return INSTANCE;
    }

    private ProfileFragment profileFragment;
    private QuestFragment questFragment;
    private SocialFragment socialFragment;
    private EmptyFragment emptyFragment;

    public SocialFragment getSocialFragment() {
        if (socialFragment == null)
            socialFragment = new SocialFragment();
        return socialFragment;
    }

    public ProfileFragment getProfileFragment() {
        if (profileFragment == null)
            profileFragment = new ProfileFragment();
        return profileFragment;
    }

    public QuestFragment getQuestFragment() {
        if (questFragment == null) {
            questFragment = new QuestFragment();
        }
        return questFragment;
    }

    public EmptyFragment getEmptyFragment() {
        if (emptyFragment == null)
            emptyFragment = new EmptyFragment();
        return emptyFragment;
    }

}
