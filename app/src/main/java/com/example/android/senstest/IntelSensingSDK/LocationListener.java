package com.example.android.senstest.IntelSensingSDK;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.android.senstest.Application;
import com.example.android.senstest.EventBus.MessageEvent;
import com.intel.context.error.ContextError;
import com.intel.context.item.Item;
import com.intel.context.item.LocationCurrent;
import com.intel.context.sensing.ContextTypeListener;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Anton on 23.06.2017.
 */

public class LocationListener implements ContextTypeListener {
    private final String LOG_TAG = LocationListener.class.getName();
    String location = "start";

    public void onReceive(Item state) {
        if (state instanceof LocationCurrent) {

            // Obtain the list of recognized physical activities.
            Log.d(LOG_TAG,"Received value: "+((LocationCurrent) state).getLocation().toString()+", "+state.getTimestamp());
            Log.d(LOG_TAG,"Received value: "+((LocationCurrent) state).getLocation().getLatitude());
            Log.d(LOG_TAG,"Received value: "+((LocationCurrent) state).getLocation().getLongitude());
            location =""+ ((LocationCurrent) state).getLocation().getLatitude() +"," + ((LocationCurrent) state).getLocation().getLongitude() ;
            Log.d(LOG_TAG,"Received value: "+location);
            EventBus.getDefault().post(new MessageEvent(location));
            SharedPreferences prefssettings = Application.getContext().getSharedPreferences("Data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editorsettings = prefssettings.edit();
            editorsettings.putString("loc",location);
            //+","+((LocationCurrent) state).getLocation().getLongitude());
           // editorsettings.putString("Location","test"+((LocationCurrent) state).getLocation().getLongitude());

            Log.d("LocPreftest",prefssettings.getString("loc","asd"));
           // location = ((LocationCurrent) state).getLocation().toString()+", "+state.getTimestamp();
           // StorageHelper.openDBConnection().save2LocHistory((LocationCurrent)state);
        } else {
            Log.d(LOG_TAG, "Invalid state type: " + state.getContextType());

            Log.d("Invalid state type: " , state.getContextType());
        }
    }




    public void onError(ContextError error) {
        Log.e(LOG_TAG, "Error: " + error.getMessage());
    }
}
