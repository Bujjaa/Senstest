package com.example.android.senstest;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.dennis.mobilesensing_module.mobilesensing.EventBus.SensorDataEvent;
import de.dennis.mobilesensing_module.mobilesensing.Module;
import de.dennis.mobilesensing_module.mobilesensing.SensingManager.SensorNames;
import de.dennis.mobilesensing_module.mobilesensing.Sensors.GoogleLocation.GLocationListener;
import de.dennis.mobilesensing_module.mobilesensing.Storage.ObjectBox.SensorTimeseries;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

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
    SharedPreferences prefs = Application.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    GLocationListener gLocationListener;
    SensorTimeseries msensorTimeseries;
    double i= 0;




    public boolean isActivitySensingon() {
        return activitySensingon;
    }


    boolean activitySensingon =false;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.checkPermissions();
        this.mHandler = new Handler();
        gLocationListener = new GLocationListener(Module.getContext(),10*1000,5*1000);
        msensorTimeseries = new SensorTimeseries();







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
                Module.getSensingManager().setSensingSetting(SensorNames.GPS,true);

                Module.getSensingManager().startSensing();


                //gLocationListener.getCoordinates();
                //vLocation.setText(gLocationListener.getCoordinates());
               // Log.d("Main:gLocationListener",gLocationListener.getCoordinates());

                //write Location coordinates into Firebase
               // firePost(gLocationListener.getCoordinates());

                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Log.d("Google Play Service", "ist aktiv");

                   if(dataPref.getString("Network","asd") != null)
                   {
                   vNetwork.setText(dataPref.getString("Network","asd"));
                   Log.d("NetworkString", dataPref.getString("Network","asd"));
                   Log.d("LocationString", locPref.getString("Location","asd"));
                       if(activitySensingon){
                           m_Runnable.run();
                       }
                   }
                   else{
                       vNetwork.setText("Nicht verbunden");
                       Log.d("NetworkString",dataPref.getString("Network","nicht gefunden"));
                   }
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
    public void firePostString(String text){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("test");
        myRef.child("child 1").child("child 2").setValue(text);
    }
    public void firePostDouble(Double d1, Double d2){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Coordinates");
        myRef.child("lat").setValue(d1);
        myRef.child("lng").setValue(d2);
    }
    public void firePostJson(JSONObject jsonObject){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("test");

        myRef.setValue(jsonObject);
    }


    public void stopSensing(View v) {
        Module.getSensingManager().stopSensing();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            //mLoginActivity.signOut();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        //vLocation.setText(Application.getContext().);
    }
    public void checkPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            String[] permissions = new String[]{Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION};
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
/*
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
    */


    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onStop(){
        super.onStop();
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("Play service", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SensorDataEvent event) throws JSONException {

        //firePostJson(event.data.toJSON());
        Double d1 = gLocationListener.getLatitude()+i;
        Double d2 = gLocationListener.getLongitude();
        firePostDouble(d1,d2);
        String s = d1+","+d2;
        vLocation.setText(s);
        Log.d("Main: OBox event",s);
    };
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


}


