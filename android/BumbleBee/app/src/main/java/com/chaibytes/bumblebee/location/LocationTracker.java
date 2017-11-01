package com.chaibytes.bumblebee.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
    private LocationListener mLocationListener;

    private static final String TAG = LocationTracker.class.getSimpleName();

    public LocationTracker(Context context, LocationListener listener) {
        mContext = context;
        mLocationListener = listener;
    }


    private void initLocationAPIServices() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                . addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection Failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        try {
            // Get the location
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                Toast.makeText(mContext, location.getLatitude() + ", " + location.getLongitude(),
                        Toast.LENGTH_LONG).show();
                mLocationListener.getCurrentUserLocation(location);
                mGoogleApiClient.disconnect();
            }
        } catch (SecurityException e) {
            System.out.print(e);
        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    public void getCurrentLocation() {
        initLocationAPIServices();
    }
}
