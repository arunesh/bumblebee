package com.chaibytes.bumblebee.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.chaibytes.bumblebee.location.LocationTracker;

/**
 * Singleton Class that initializes and starts/stops services such as LocationTracker.
 */

public class ServicesProvider implements Application.ActivityLifecycleCallbacks {

    private static ServicesProvider mInstance = null;
    private LocationTracker mLocationTracker = null;
    private static Context mContext = null;

    private ServicesProvider() {
        initLocationServices();
    }

    public static synchronized ServicesProvider getInstance() {
        if (mInstance == null) {
            mInstance = new ServicesProvider();
        }

        return mInstance;
    }

    public LocationTracker getLocationTracker() {
        return mLocationTracker;
    }

    public static void init(Context context) {
        mContext = context;
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        startLocationServices();
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        stopLocationServices();
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    private void startLocationServices() {
        mLocationTracker.connectToLocationServicesAPI();
    }

    private void stopLocationServices() {
        mLocationTracker.disconnectFromLocationServicesAPI();
    }

    private void initLocationServices() {
        mLocationTracker = new LocationTracker(mContext);
    }
}
