package com.example.android.senstest;

import android.content.Context;

/**
 * Created by Anton on 23.06.2017.
 */

public class Application extends android.app.Application {
    private static Context context;
    private static SensingManager sensMang;
    @Override
    public void onCreate()  {
        super.onCreate();
        context = getApplicationContext();
        sensMang = new SensingManager();
        sensMang.startSensing();

    }
    public static Context getContext() {return context;}
    public static SensingManager getSensingManager() {return sensMang;}
}
