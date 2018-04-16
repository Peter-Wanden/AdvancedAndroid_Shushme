package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates Geofence objects
 * TODO (1) Create a Geofencing class with a Context and GoogleApiClient constructor that
 * initializes a private member ArrayList of Geofences called mGeofenceList
 */
public class Geofencing implements ResultCallback{

    // Constants
    private static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50; // Meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours


    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public Geofencing(Context context, GoogleApiClient client){
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    // TODO (6) Inside Geofencing, implement a public method called registerAllGeofences that
    // registers the GeofencingRequest by calling LocationServices.GeofencingApi.addGeofences
    // using the helper functions getGeofencingRequest() and getGeofencePendingIntent()
    /**
     * Registers the list of Geofences specified in mGeofenceList with Google Play Services.
     * Uses {@code #mGoogleApiClient} to connect to Google Play Services.
     * Uses {@link #getGeofnecingRequest} to get the list of Geofences to be registered.
     * Uses {@link #getGeofencePendingIntent} to get the pending intent to launch the IntentService
     * when the Geofence is triggered.
     * Triggers {@link #onResult} when the Geofences have been registered successfully.
     */
    public void registerAllGeofences() {
        if (mGoogleApiClient == null
                || !mGoogleApiClient.isConnected()
                || mGeofenceList == null
                || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofnecingRequest(),
                    getGeofencePendingIntent()

            ).setResultCallback(this);
        } catch (SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    // TODO (7) Inside Geofencing, implement a public method called unRegisterAllGeofences that
    // unregisters all geofences by calling LocationServices.GeofencingApi.removeGeofences
    // using the helper function getGeofencePendingIntent()
    /**
     * Unregisters all the Geofences created by this app from Google Play Services.
     * Uses {@code #mGoogleApiClient} to connect to Google Play Sevices.
     * Uses {@link #getGeofencePendingIntent} to get the pending intent passed when registering
     * the Geofences in the first place.
     * Triggers {@link #onResult} when the Geofences have been unregistered successfully.
     */
    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }
        try {
            LocationServices.GeofencingApi
                    .removeGeofences(mGoogleApiClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    // TODO (2) Inside Geofencing, implement a public method called updateGeofencesList that
    // given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
    // and add that Geofence to mGeofenceList
    /**
     * This method updates the local ArrayList of Gepfences using data from the passed in list
     * Uses the place ID defined by the API as the Geofence object ID.
     *
     * @param places the PlaceBuffer result of the getPlaceById call.
     */
    public void updateGeofencesList(PlaceBuffer places) {
        mGeofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) return;
        for (Place place : places) {
            // Read the place information from the Cursor
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            // Build Geofence object
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID) // Unique ID from maps.
                    .setExpirationDuration(GEOFENCE_TIMEOUT) // How long this Geofence stats active.
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS) // Exactly where and how wide the Geofence should be.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT) // When to alert our broadcast receiver.
                    .build();

            // Add it to our list of Geofences
            mGeofenceList.add(geofence);
        }
    }

    // TODO (3) Inside Geofencing, implement a private helper method called getGeofencingRequest that
    // uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list
    /**
     * Creates a GeofenceRequest object using the mGeofenceList ArrayList of GeoFences
     * used by {@code #registerGeofences}.
     *
     * @return the Geofnecing object.
     */
    private GeofencingRequest getGeofnecingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    // TODO (5) Inside Geofencing, implement a private helper method called getGeofencePendingIntent that
    // returns a PendingIntent for the GeofenceBroadcastReceiver class
    /**
     * Creates a PendingIntent object using the GeofenceTransitionsIntentService class
     * Used by {@code #registerGeofences}.
     *
     * @return the PendingIntent object.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Re-use the intent if there is one
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding/removing Geofence: %s",
                result.getStatus().toString()));
    }
}
