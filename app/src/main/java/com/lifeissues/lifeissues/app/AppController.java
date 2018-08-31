package com.lifeissues.lifeissues.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by Emo on 11/28/2017.
 */

public class AppController extends Application {
    private static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
}
