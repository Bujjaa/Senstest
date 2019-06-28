package com.example.android.senstest;

import android.Manifest;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.intel.context.Sensing;
import com.intel.context.exception.ContextProviderException;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity  {
    Button sensBtn, permBtn;
    TextView vActivity, vLocation, vNetwork, vSelectedBuslinie;
    ImageView vVerbindungsaufbau,vGestartet, vGestoppt;
    SharedPreferences sharedPreferences, dataPref, locPref;
    Handler mHandler;
    String sLocation, sActivity;
    SharedPreferences prefs = Application.getContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    GLocationListener gLocationListener;
    SensorTimeseries msensorTimeseries;
    double i= 0;
    int vGestartetFirstInitiate = 0;
    boolean activitySensingon =false;
    private long backPressedTime =0;
    String buslinie, finalBuslinie, tmpLogDate;
    String parseObjectID, oldParseObjectID, logDate;
    boolean sensingAlreadyStarted = false;
    Thread t;
    HashMap busStopMap = new HashMap();
    ArrayList<String> sameCoordinates = new ArrayList<>();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = Application.getContext().registerReceiver(null, ifilter);
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String selectedDienst = getIntent().getStringExtra("DIENST");


        this.checkPermissions();
        this.mHandler = new Handler();
        gLocationListener = new GLocationListener(Module.getContext(),5*1000,5*1000);
        msensorTimeseries = new SensorTimeseries();
        buslinie = selectedDienst;



        vLocation = (TextView) findViewById(R.id.dataLocation);
        vNetwork = (TextView) findViewById(R.id.dataNetwork);
        vSelectedBuslinie = (TextView) findViewById(R.id.selectedBusline);
        permBtn = (Button) findViewById(R.id.permBtn);

        //loading images
        vVerbindungsaufbau = (ImageView) findViewById(R.id.imageVerbindungsaufbau);
        vGestartet = (ImageView) findViewById(R.id.imageGestartet);
        vGestoppt = (ImageView) findViewById(R.id.imageGestoppt);

        vSelectedBuslinie.setText(selectedDienst);




        dataPref = Application.getContext().getSharedPreferences("Data",Context.MODE_PRIVATE);
        sharedPreferences = Application.getContext().getSharedPreferences("Settings",Context.MODE_PRIVATE);
        locPref = Application.getContext().getSharedPreferences("Data",Context.MODE_PRIVATE);


        sensBtn = (Button) findViewById(R.id.sensBtn);
        sensBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(level <=10){
                    Toast.makeText(MainActivity.this, "Akku zu schwach bitte erst laden!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(sensingAlreadyStarted == true){
                    Toast.makeText(MainActivity.this, "Programm bereits gestartet, bitte erst stoppen!", Toast.LENGTH_SHORT).show();
                    return;
                }
                vVerbindungsaufbau.setVisibility(View.VISIBLE);

                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Log.d("Google Play Service", "ist aktiv");

                   if(dataPref.getString("Network","Keine Internetverbindung") != null)
                   {
                   vNetwork.setText(dataPref.getString("Network","asd"));
                   Log.d("NetworkString", dataPref.getString("Network","Keine Internetverbindung"));
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


                Log.d("tmpString","String: "+ buslinie);
                finalBuslinie = buslinie;

                ParseQuery<ParseObject> query;
                query = ParseQuery.getQuery("BusGPS");

                query.whereEqualTo("buslinien", finalBuslinie);
                query.whereEqualTo("isactive", false);
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parselist, ParseException e) {
                        if(e==null && parselist.size() != 0){
                            Log.d("ParseList", "item 0: "+parselist.get(0));
                            Log.d("ParseList", "komplette Liste: "+parselist);
                            parseObjectID = parselist.get(0).getObjectId();
                            Log.d("parsID",""+parseObjectID);
                        }
                        else if(e==null){
                            final ParseObject newBusCoordinates = new ParseObject("BusGPS");
                            newBusCoordinates.put("buslinien",finalBuslinie);
                            newBusCoordinates.put("isactive",false);
                            newBusCoordinates.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null) {
                                        parseObjectID = newBusCoordinates.getObjectId();
                                        Log.d("ParseID", "was saved: "+parseObjectID);
                                        Log.d("parsID",""+parseObjectID);
                                    }
                                    else{
                                        Log.d("ParseID", "error: "+e);
                                    }
                                }
                            });
                        }
                    }
                });


                Module.getSensingManager().setSensingSetting(SensorNames.GPS,true);

                Module.getSensingManager().startSensing();
                sensingAlreadyStarted = true;
            }

     });
        // for logging purpose

        busStopMap.put("8.025,50.912","Siegen Uni AR");
        busStopMap.put("8.024,50.901","Weidenau Brueckenstraße");
        busStopMap.put("8.03,50.905","Weidenau Wilhelm-von-Humboldt-Platz");
        busStopMap.put("8.027,50.905","Weidenau Hölderlinstraße");
        busStopMap.put("8.027,50.895","Wedenau ZOB");
        busStopMap.put("8.029,50.895","Weidenau ZOB - Bussteig 5");
        busStopMap.put("8.018,50.876","Siegen ZOB - Bussteig A");
        busStopMap.put("8.017,50.875","Siegen ZOB - Bussteig B");



        //



    }





    private final Runnable m_Runnable = new Runnable() {
        public void run(){

            Log.d("ActivityString",dataPref.getString("Activity","asd"));
            vActivity.setText(dataPref.getString("Activity","asd"));
            MainActivity.this.mHandler.postDelayed(m_Runnable,2000);
        }
    };

    public void reconnect()throws ContextProviderException{

            Module.getSensingManager().stopSensing();

            sensingAlreadyStarted = false;
            vGestoppt.setVisibility(View.VISIBLE);
            Toast.makeText(this, "GPS Übermittlung wurde gestoppt", Toast.LENGTH_SHORT).show();

            ParseQuery<ParseObject> query;
            query = ParseQuery.getQuery("BusGPS");
            query.getInBackground(parseObjectID, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.put("isactive", false);
                    object.saveInBackground();
                    Log.d("Stopsensing", "stopped Object: " + parseObjectID);
                }
            });
            oldParseObjectID = parseObjectID;
        vGestartetFirstInitiate = 0;
        vGestoppt.setVisibility(View.GONE);
            sensBtn.callOnClick();
    }

    public void stopSensing(View v) throws ContextProviderException {
        if (sensingAlreadyStarted == true) {
            Module.getSensingManager().stopSensing();

            sensingAlreadyStarted = false;
        vGestoppt.setVisibility(View.VISIBLE);
        Toast.makeText(this, "GPS Übermittlung wurde gestoppt", Toast.LENGTH_SHORT).show();

            ParseQuery<ParseObject> query;
            query = ParseQuery.getQuery("BusGPS");
        query.getInBackground(parseObjectID, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                object.put("isactive", false);
                object.saveInBackground();
                Log.d("Stopsensing", "stopped Object: " + parseObjectID);
            }
        });
        oldParseObjectID = parseObjectID;
        finish();
    }
    else {
            Toast.makeText(this, "APP wurde bereits gestoppt!", Toast.LENGTH_SHORT).show();
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



    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public void onStop(){
        super.onStop();
//        EventBus.getDefault().unregister(this);
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
        if(vGestartetFirstInitiate == 0){
            vGestartet.setVisibility(View.VISIBLE);
            vVerbindungsaufbau.setVisibility(View.GONE);
            vGestoppt.setVisibility(View.GONE);
            vGestartetFirstInitiate=1;
        }

        if(level <= 5){

            try {
                stopSensing(null);
            } catch (ContextProviderException e) {
                e.printStackTrace();
            }
        }
        if(sameCoordinates.size()>=10)
        {
            int tmpcounter = 0;
            for(int x = 0; x<sameCoordinates.size();x++){

                if(sameCoordinates.get(0)==sameCoordinates.get(x)){
                    tmpcounter++;

                }
                else{
                    break;
                }
            }
            if (tmpcounter >=8){
                try {
                    reconnect();
                    Log.d("RECONNECT","Reconnect wurde ausgeführt");
                } catch (ContextProviderException e) {
                    e.printStackTrace();
                }
                sameCoordinates.clear();
            }
            else{
                sameCoordinates.clear();
            }
        }


        final Double d1e = event.data.getValues().get(0).getGeoPointEntities().get(0).getLat();
        final Double d2e = event.data.getValues().get(0).getGeoPointEntities().get(0).getLng();
        sameCoordinates.add(d1e.toString());


        Log.d("ParseObjectID","kurz vor dem put: "+parseObjectID);
        if(parseObjectID != oldParseObjectID) {

            ParseQuery<ParseObject> query;
            query = ParseQuery.getQuery("BusGPS");
            query.getInBackground(parseObjectID, new GetCallback<ParseObject>() {
                public void done(ParseObject busCoordinates, ParseException e) {
                    if (e == null) {
                        busCoordinates.put("buslinien", finalBuslinie);
                        busCoordinates.put("Latitude", d1e);
                        busCoordinates.put("Longitude", d2e);
                        busCoordinates.put("isactive", true);
                        busCoordinates.saveInBackground();
                    }
                }
            });
        }
        DecimalFormat df = new DecimalFormat("#.###");

        String dCompare = df.format(d2e)+","+df.format(d1e);


        long i = 0;
        Iterator<Map.Entry> it = busStopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();

            String[] parts  = ((String) entry.getKey()).split(",");
            Double compareDlong= Double.parseDouble(parts[0]) - d2e;
            Double compareDlat = Double.parseDouble(parts[1]) - d1e;
            if ( compareDlat <0.0007 && compareDlat >-0.0007 && compareDlong <0.0007 && compareDlong >-0.0007) {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                Date time = new Date();
                System.out.println(timeFormat.format(time));

                logDate = dateFormat.format(date) + "XX" + timeFormat.format(time)+"XX"+entry.getValue();


                ParseQuery<ParseObject> query;
                query = ParseQuery.getQuery("BusGPS");
                query.getInBackground(parseObjectID, new GetCallback<ParseObject>() {
                    public void done(ParseObject busCoordinates, ParseException e) {
                        if (e == null&& !logDate.equals(tmpLogDate)) {
                            busCoordinates.add("Log", logDate);
                            busCoordinates.saveInBackground();
                            tmpLogDate = logDate;
                        }
                    }
                });
            }
        }

        String s = d1e+","+d2e;
        vLocation.setText(s);
        Log.d("Main: OBox event",s);
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }
    @Override
    public void onPause(){
        super.onPause();

    }
    @Override
    public void onBackPressed() {

        //stops the tracking
        permBtn.callOnClick();
        //Back to the ScrollingActivity
        Intent intent = new Intent(getBaseContext(), ScrollingActivity.class);
        startActivity(intent);
        finish();
        // to prevent irritating accidental logouts
//        long t = System.currentTimeMillis();
//
//            if (t - backPressedTime > 2000) {    // 2 secs
//                backPressedTime = t;
//                Toast.makeText(this, "Press back again to close the app",
//                        Toast.LENGTH_SHORT).show();
//            } else {    // this guy is serious
//                // clean up#
//                permBtn.callOnClick();
//                finish();
//            }
        }
    }





