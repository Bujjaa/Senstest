package com.example.android.senstest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.android.senstest.IntelSensingSDK.ActivityListener;
import com.example.android.senstest.IntelSensingSDK.NetworkListener;
import com.example.android.senstest.IntelSensingSDK.SensingListener;
import com.example.android.senstest.IntelSensingSDK.LocationListener;
import com.intel.context.Sensing;
import com.intel.context.error.ContextError;
import com.intel.context.exception.ContextProviderException;
import com.intel.context.item.ContextType;
import com.intel.context.option.ContinuousFlag;
import com.intel.context.option.activity.ActivityOptionBuilder;
import com.intel.context.option.activity.Mode;
import com.intel.context.option.activity.ReportType;
import com.intel.context.sensing.ContextTypeListener;
import com.intel.context.sensing.InitCallback;

/**
 * Created by Anton on 23.06.2017.
 */

public class SensingManager {
    //Intel Sensing SDK
    private Sensing mSensing;
    private ContextTypeListener mLocationListener;
    private ContextTypeListener mActivityListener;
    private ContextTypeListener mNetworkListener;
    private ContextTypeListener mCallListener;
    MainActivity mActivity;
    //

    //




    private boolean isActivityOn = true;
    private boolean isScreenOn;
    private boolean isNetworkOn = true;
    private boolean isRunAppOn;
    private boolean isCallOn;
    //
    private String TAG = SensingManager.class.getName();


    public SensingManager() {
        mActivity = new MainActivity();
    }
    public void startSensing(){
        mSensing = new Sensing(Application.getContext(), new SensingListener());
        //
        mSensing.start(new InitCallback() {
            public void onSuccess() {
                Log.d("APPLICATION", "Context Sensing Daemon Started");
                //
                try {
                    //Location Listener
                    mLocationListener = new LocationListener();
                    mSensing.addContextTypeListener(ContextType.LOCATION, mLocationListener);
                    //Activity Listener
                    mActivityListener = new ActivityListener();
                    mSensing.addContextTypeListener(ContextType.ACTIVITY_RECOGNITION, mActivityListener);

                    //Network Listener
                    mNetworkListener = new NetworkListener();
                    mSensing.addContextTypeListener(ContextType.NETWORK, mNetworkListener);



                    Log.d("APPLICATION", "Sensing started");
                } catch (ContextProviderException e) {
                    e.printStackTrace();
                }
                loadSensingSettings();
            }
            public void onError(ContextError error) {
                loadSensingSettings();

                Log.d("APPLICATION", "Error: " + error.getMessage());
            }
        });
    }
    public void loadSensingSettings()
    {
        SharedPreferences prefs = Application.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        Log.d(TAG, " GPS VALUE");
        try {
            Bundle settings;
            //enable Location Sensing
            if(!isLocationOn)
            {
                mSensing.enableSensing(ContextType.LOCATION, null);
                //isLocationOn=true;
                Log.d(TAG,"GPS-Tracking enabled");
            }else if(isLocationOn){
                mSensing.disableSensing(ContextType.LOCATION);
                //isLocationOn =false;
                Log.d(TAG, "GPS-Tracking disabled");
            }
            //enable Activity Sensing

            if(!mActivity.isActivitySensingon()){
                ActivityOptionBuilder actBui;
                actBui = new ActivityOptionBuilder();
                actBui
                        .setMode(Mode.POWER_SAVING)
                        .setReportType(ReportType.FREQUENCY)
                        .setSensorHubContinuousFlag(ContinuousFlag.NOPAUSE_ON_SLEEP);
                settings = actBui.toBundle();
                mSensing.enableSensing(ContextType.ACTIVITY_RECOGNITION, settings);
                //isActivityOn = true;
                Log.d(TAG, "Activity-Tracking enabled");
            }else if(mActivity.isActivitySensingon()){
                mSensing.disableSensing(ContextType.ACTIVITY_RECOGNITION);
               // isActivityOn=false;
                Log.d(TAG, "Activity-Tracking disabled");
            }

            //enable Network Type
            if(!isNetworkOn){
                settings = new Bundle();
               settings.putLong("TIME_WINDOW", 10*1000);
                mSensing.enableSensing(ContextType.NETWORK, settings);

                //isNetworkOn = true;
                Log.d(TAG, "Network-Tracking enabled");
            }else if (isNetworkOn){
                mSensing.disableSensing(ContextType.NETWORK);
                //isNetworkOn = false;
                Log.d(TAG, "Network-Tracking disabled");
            }


        } catch (ContextProviderException e) {
            Log.e("APPLICATION", "Error enabling context type" + e.getMessage());
        }
    }
    private boolean isLocationOn = true;

    public void setActivityOn(boolean activityOn) {
        isActivityOn = activityOn;
    }

    public boolean isActivityOn() {
        return isActivityOn;
    }

}
