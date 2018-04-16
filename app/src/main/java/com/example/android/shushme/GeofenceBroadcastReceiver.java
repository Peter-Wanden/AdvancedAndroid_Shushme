package com.example.android.shushme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * TODO (4) Create a GeofenceBroadcastReceiver class that extends BroadcastReceiver and override
 * onReceive() to simply log a message when called. Don't forget to add a receiver tag in the Manifest
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    /**
     * Handles the Broadcast message sent when the Geofence Transition is triggered.
     * This is running on the main thread so make sure you use an AsyncTask for anything that
     * takes longer than 10 seconds.
     *
     * THIS IS A NEW RECEIVER CLASS SO DON'T FORGET TO REGISTER IT IN MANIFEST!
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive called");
    }
}
