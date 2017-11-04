package com.chaibytes.bumblebee.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.chaibytes.bumblebee.location.LocationTracker;

/**
 * Singleton Class that initializes and starts/stops services such as LocationTracker.
 */

public class ServicesProviderSingleton {

    private static ServicesProviderSingleton mInstance = null;
    private LocationTracker mLocationTracker = null;
    private static Context mContext = null;

    private ServicesProviderSingleton() {
        initLocationServices();
    }

    public static synchronized ServicesProviderSingleton getInstance() {
        if (mInstance == null) {
            mInstance = new ServicesProviderSingleton();
        }

        return mInstance;
    }

    public LocationTracker getLocationTracker() {
        return mLocationTracker;
    }

    public static void init(Context context) {
        mContext = context;
    }

    public void startLocationServices() {
        mLocationTracker.connectToLocationServicesAPI();
    }

    public void stopLocationServices() {
        mLocationTracker.disconnectFromLocationServicesAPI();
    }

    private void initLocationServices() {
        mLocationTracker = new LocationTracker(mContext);
    }
}
