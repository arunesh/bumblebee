package com.chaibytes.bumblebee.backend;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.location.LocationListener;
import com.chaibytes.bumblebee.location.LocationTracker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.util.Date;

/**
 * Interacts with Cloud Firestore
 */

public class CloudFirestoreDatabase implements Backend, LocationListener {
    private static final String TAG = CloudFirestoreDatabase.class.getSimpleName();

    private static int iNum = 0;
    private static long prevTimeStamp = 0L;
    private static final long MIN_DURATION_MS = 15000L * 60L; // 15 mins in ms
    private static String prevTrip = "";

    private FirebaseFirestore db;
    private MotionData motionData;
    private String date = null;

    private Context mContext;
    private Location mCurrentLocation;

    private LocationTracker mLocationTracker;

    @Override
    public void saveData(MotionData motionData, Context context) {
        mContext = context;
        db = FirebaseFirestore.getInstance();
        mLocationTracker = new LocationTracker(mContext, this);
        this.motionData = motionData;

        date = DateFormat.getDateInstance().format(new Date());

        if (!isSameInterval(motionData.getTimeStamp())) {
            mLocationTracker.getCurrentLocation();
            // Add a new MotionData object
            saveNewdata();
        } else {
            // Same activity
            updateData();
        }
    }

    private void saveLocation(final String locationType) {
        db.collection("users").document(date)
                .collection(prevTrip).document(locationType)
                .set(mCurrentLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, locationType + " successfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void saveNewdata() {
        db.collection("users").document(date)
                .collection(prevTrip).document(Long.toString(motionData.getTimeStamp()))
                .set(motionData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void updateData() {
        db.collection("users").document(date)
                .collection(prevTrip).document(Long.toString(motionData.getTimeStamp()))
                .set(motionData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private static boolean isSameInterval(long currTimeStamp) {
        if ((currTimeStamp - prevTimeStamp) < MIN_DURATION_MS) {
            // Same activity
            return true;
        } else {
            prevTimeStamp = currTimeStamp;
            iNum++;
            prevTrip = "trip" + iNum;
            return false;
        }
    }

    @Override
    public void getCurrentUserLocation(Location location) {
        mCurrentLocation = location;
        Toast.makeText(mContext, "Got back the location to save to DB", Toast.LENGTH_LONG).show();
        saveLocation("startLocation");
    }
}
