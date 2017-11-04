package com.chaibytes.bumblebee.detector;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.chaibytes.bumblebee.backend.Backend;
import com.chaibytes.bumblebee.backend.CloudFirestoreDatabase;
import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.data.UserLocation;
import com.chaibytes.bumblebee.util.ServicesProviderSingleton;

/**
 * Listens when there is an update in Motion and saves differential
 * state to the backend.
 */

public class MotionDetector {
    private static final String TAG = MotionDetector.class.getSimpleName();
    private static final long MIN_DURATION_MS = 60000L; // 15 mins in ms

    private Backend backend;
    private String tripName = "";

    private int iNum = 0;
    private long prevTimeStamp = 0L;

    //private static final long MIN_DURATION_MS = 15000L * 60L; // 15 mins in ms

    private Context context;

    public MotionDetector(Context context) {
        this.context = context;
        backend = new CloudFirestoreDatabase(context);
    }

    public void addData(String terseResult, MotionData motionData) {
        // Check the data and decide if there needs to be a delta reported and stored to the backend.
        Log.d("dataPoint", terseResult);

        UserLocation userLocation = getCurrentLocation();
        motionData.setUserLocation(userLocation);

        if (!isSameInterval(motionData.getTimeStamp())) {
           /* Location location = ServicesProviderSingleton.getInstance().getLocationTracker().getLastLocation();
            if (location != null) {
                // Get the UserLocation object to save
                UserLocation userLocation = new UserLocation(location.getLatitude(),
                                                            location.getLongitude(),
                                                            location.getTime());
                backend.saveLocationData(userLocation, tripName);
            }
*/

            // Add a new MotionData object
            backend.saveNewTripData(motionData, tripName);
        } else {
            // Same activity
            backend.updateTripData(motionData, tripName);
        }
    }

    private static UserLocation getCurrentLocation() {
        Location location = ServicesProviderSingleton.getInstance().getLocationTracker().getLastLocation();
        if (location != null) {
            // Get the UserLocation object to save
            UserLocation userLocation = new UserLocation(location.getLatitude(),
                    location.getLongitude(),
                    location.getTime());
            return userLocation;
        }
        return  null;
    }


    private boolean isSameInterval(long currTimeStamp) {
        if ((currTimeStamp - prevTimeStamp) < MIN_DURATION_MS) {
            // Same activity
            return true;
        } else {
            prevTimeStamp = currTimeStamp;
            iNum++;
            tripName = "trip" + iNum;
            return false;
        }
    }

    public void cleanup() {
        backend.shutdown();
    }
}
