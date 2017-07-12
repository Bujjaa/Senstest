package com.example.android.senstest;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity {
    Button sensBtn, permBtn;
    TextView vActivity, vLocation, vNetwork;
    SharedPreferences sharedPreferences, dataPref, locPref;
    Handler mHandler;
    String sLocation, sActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.checkPermissions();
        this.mHandler = new Handler();
        EventBus.getDefault().register(this);




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

        //vLocation.setText(Application.getContext().);
    }
    public void checkPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission","Access_Fine_Location granted");
        }





        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission","READ_PHONE_STATE granted");
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE )
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_NETWORK_STATE )) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_NETWORK_STATE },
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE )
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission","ACCESS_NETWORK_STATE  granted");
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
