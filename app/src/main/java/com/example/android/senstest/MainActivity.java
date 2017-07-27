package com.example.android.senstest;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.senstest.EventBus.ActivityEvent;
import com.example.android.senstest.EventBus.MessageEvent;
import com.example.android.senstest.IntelSensingSDK.LocationListener;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity {
    Button sensBtn, permBtn;
    TextView vActivity, vLocation, vNetwork;
    SharedPreferences sharedPreferences, dataPref, locPref;
    Handler mHandler;
    String sLocation, sActivity;
    SensingManager sensMang;

    public boolean isActivitySensingon() {
        return activitySensingon;
    }


    boolean activitySensingon =true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.checkPermissions();
        this.mHandler = new Handler();
        EventBus.getDefault().register(this);
        sensMang = new SensingManager();




        vActivity = (TextView) findViewById(R.id.dataActivity);
        vLocation = (TextView) findViewById(R.id.dataLocation);
        vNetwork = (TextView) findViewById(R.id.dataNetwork);
        permBtn = (Button) findViewById(R.id.permBtn);

        dataPref = Application.getContext().getSharedPreferences("Data",Context.MODE_PRIVATE);
        sharedPreferences = Application.getContext().getSharedPreferences("Settings",Context.MODE_PRIVATE);
        locPref = Application.getContext().getSharedPreferences("Data",Context.MODE_PRIVATE);


        sensBtn = (Button) findViewById(R.id.sensBtn);
        sensBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               if(sharedPreferences.getString("Network","asd") != null)
               {
                   vNetwork.setText(sharedPreferences.getString("Network","asd"));
                   Log.d("NetworkString", sharedPreferences.getString("Network","asd"));
                   vLocation.setText(sLocation);

                 //  vLocation.setText(locPref.getString("Location","asd"));
                   Log.d("LocationString", locPref.getString("Location","asd"));
                   m_Runnable.run();
               }

               else{
                   vNetwork.setText("Nicht verbunden");
                   Log.d("NetworkString",sharedPreferences.getString("Network","nicht gefunden"));
               }

            }
     });
    }
    private final Runnable m_Runnable = new Runnable() {
        public void run(){

            Log.d("ActivityString",dataPref.getString("Activity","asd"));
            vActivity.setText(dataPref.getString("Activity","asd"));
            MainActivity.this.mHandler.postDelayed(m_Runnable,2000);
        }
    };


    public void startSensing(View v) {
        if(sensMang.isActivityOn())
            Log.d("BoolActivity","before change is true");
        else
            Log.d("BoolActivity","before change is false");

        sensMang.setActivityOn(false);
        activitySensingon = false;

        if(sensMang.isActivityOn())
            Log.d("BoolActivity","after change is true");
        else
            Log.d("BoolActivity","after change is false");
        Application.getSensingManager().startSensing();

        //vLocation.setText(Application.getContext().);
    }
    public void checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String[] permissions = new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION};
            boolean flag = false;
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    flag = true;
                    break;
                }
            }

            if (flag) {
                requestPermissions(permissions, 1);
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onActivityEvent(ActivityEvent event){
        sActivity = event.message;
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MessageEvent event){
        sLocation = event.message;
        Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
