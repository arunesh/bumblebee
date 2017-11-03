package com.chaibytes.bumblebee.backend;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chaibytes.bumblebee.data.MotionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.util.Date;

/**
 * Interacts with Cloud Firestore
 */

public class CloudFirestoreDatabase implements Backend {
    private static final String TAG = CloudFirestoreDatabase.class.getSimpleName();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String START_LOCATION = "start_location";

    @Override
    public void saveLocationData(final Location locationType, String tripName) {
        db.collection("users").document(getDate())
                .collection(tripName).document(START_LOCATION)
                .set(locationType)
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

    @Override
    public void saveNewTripData(MotionData motionData, String newTripName) {
        db.collection("users").document(getDate())
                .collection(newTripName).document(Long.toString(motionData.getTimeStamp()))
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

    @Override
    public void updateTripData(MotionData motionData, String currentTripName) {
        db.collection("users").document(getDate())
                .collection(currentTripName).document(Long.toString(motionData.getTimeStamp()))
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

    private String getDate() {
        return DateFormat.getDateInstance().format(new Date());
    }
}
