package com.example.android.senstest;

import android.content.Context;
import android.content.SharedPreferences;

import de.dennis.mobilesensing_module.mobilesensing.Module;
import de.dennis.mobilesensing_module.mobilesensing.SensingManager.SensingManager;
import de.dennis.mobilesensing_module.mobilesensing.SensingManager.SensorNames;
import de.dennis.mobilesensing_module.mobilesensing.Upload.UploadManager;

/**
 * Created by Anton on 23.06.2017.
 */

public class Application extends android.app.Application {
    private static Context context;
    private static SensingManager sensMang;
    private static UploadManager uplMang;
    @Override
    public void onCreate()  {
        super.onCreate();
        context = getApplicationContext();
        Module.init(context, "USERNAME");
       sensMang = Module.getSensingManager();


        sensMang.setSensingSetting(SensorNames.Activity,false);
        sensMang.setSensingSetting(SensorNames.GPS,false);
        sensMang.setSensingSetting(SensorNames.WLANUpload,false);
        sensMang.setSensingSetting(SensorNames.ScreenOn,false);
        sensMang.setSensingSetting(SensorNames.Apps,false);
        sensMang.setSensingSetting(SensorNames.Call,false);
        sensMang.setSensingSetting(SensorNames.Network,true);

        sensMang.startSensing();
        uplMang = Module.getUploadManager();

    }

    public static Context getContext() {return context;}
    public static SensingManager getSensingManager() {return sensMang;}
}
