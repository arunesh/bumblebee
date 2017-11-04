package com.chaibytes.bumblebee.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.chaibytes.bumblebee.MotionTest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Provides current user location
 */

public class LocationTracker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private static final int REQUEST_FINE_LOCATION = 0;

    private static final String TAG = LocationTracker.class.getSimpleName();

    private boolean mIsConnected;
    private LocationCallback locationCallback;

    public interface LocationCallback {
        void onLocationReady();
    }

    public LocationTracker(Context context) {
        mContext = context;

        checkPermissions();

        initLocationAPIServices();
    }

    public void setLocationCallback(LocationCallback locationCallback) {
        this.locationCallback = locationCallback;
    }

    private void initLocationAPIServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection Failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        mIsConnected = true;
        Log.d(TAG, "Connected to Location Services");
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions((Activity) mContext, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_FINE_LOCATION);
    }

    public Location getLastLocation() {
        if (mIsConnected) {
            try {
                // Get the location
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null) {
                    Log.d(TAG, "Location: " + location.getLatitude() + "; " + location.getLongitude());
                    return location;
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void connectToLocationServicesAPI() {
        mGoogleApiClient.connect();
    }

    public void disconnectFromLocationServicesAPI() {
        mGoogleApiClient.disconnect();
    }
}
