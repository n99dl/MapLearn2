package com.n99dl.maplearn;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication singleton;
    public MyApplication getInstance() {
        return singleton;
    }

    public void onCreate() {
        super.onCreate();
    }
}
