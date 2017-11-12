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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button sensBtn, permBtn;
    TextView vActivity, vLocation, vNetwork;
    SharedPreferences sharedPreferences, dataPref, locPref;
    Handler mHandler;
    String sLocation, sActivity;
    SharedPreferences prefs = Application.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    GLocationListener gLocationListener;
    SensorTimeseries msensorTimeseries;
    Spinner spinner ;
    double i= 0;
    ParseQuery<ParseObject> query;
    boolean activitySensingon =false;
    private long backPressedTime =0;
    String buslinie;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.checkPermissions();
        this.mHandler = new Handler();
        gLocationListener = new GLocationListener(Module.getContext(),5*1000,5*1000);
        msensorTimeseries = new SensorTimeseries();
        query = ParseQuery.getQuery("testParse1");







        vLocation = (TextView) findViewById(R.id.dataLocation);
        vNetwork = (TextView) findViewById(R.id.dataNetwork);
        permBtn = (Button) findViewById(R.id.permBtn);
        spinner = (Spinner) findViewById(R.id.buslineSpinner);
        spinner.setOnItemSelectedListener(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.busline_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        buslinie = parent.getItemAtPosition(pos).toString();
       Log.d("spinnertest",parent.getItemAtPosition(pos).toString());

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
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
        Double d1 = gLocationListener.getLatitude();
        Double d2 = gLocationListener.getLongitude();
        final Double d1e = event.data.getValues().get(0).getGeoPointEntities().get(0).getLat();
        final Double d2e = event.data.getValues().get(0).getGeoPointEntities().get(0).getLng();
        i += 0.005;
        final ParseGeoPoint point = new ParseGeoPoint(d1e,d2e);

        if(buslinie.equals("UX1")) {
            // Updating Coordinates with Parse
            // Retrieve the object by id
            query.getInBackground("SP40cEuIyj", new GetCallback<ParseObject>() {
                public void done(ParseObject busCoordinates, ParseException e) {
                    if (e == null) {
                        busCoordinates.put("Latitude", d1e);
                        busCoordinates.put("Longitude", d2e);
                        busCoordinates.put("location", point);
                        busCoordinates.saveInBackground();
                    }
                }
            });
        } else if(buslinie.equals("UX2")) {
            query.getInBackground("6G0aZpfcY7", new GetCallback<ParseObject>() {
                public void done(ParseObject busCoordinates, ParseException e) {
                    if (e == null) {
                        busCoordinates.put("Latitude", d1e);
                        busCoordinates.put("Longitude", d2e);
                        busCoordinates.put("location", point);
                        busCoordinates.saveInBackground();
                    }
                }
            });
        }
        String s = d1e+","+d2e;
        vLocation.setText(s);
        Log.d("Main: OBox event",s);
    };
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    public void onBackPressed() {        // to prevent irritating accidental logouts
        long t = System.currentTimeMillis();

            if (t - backPressedTime > 2000) {    // 2 secs
                backPressedTime = t;
                Toast.makeText(this, "Press back again to close the app",
                        Toast.LENGTH_SHORT).show();
            } else {    // this guy is serious
                // clean up#
                finish();
               // System.exit(0);
            }
        }
    }





