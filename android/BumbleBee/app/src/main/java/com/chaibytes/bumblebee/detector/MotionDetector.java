package com.chaibytes.bumblebee.detector;

import android.support.annotation.NonNull;
import android.util.Log;

import com.chaibytes.bumblebee.data.MotionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;

/**
 * Listens when there is an update in Motion and saves differential
 * state to the backend.
 */

public class MotionDetector {
    private static final String TAG = MotionDetector.class.getSimpleName();
    private static int iNum = 0;
    private static long prevTimeStamp = 0L;
    private static final long MIN_DURATION_MS = 15L;
    private static String prevTrip = "";

    public static void updateData(String terseResult, MotionData motionData) {
        // Check the data and decide if there needs to be a delta reported and stored to the backend.
        Log.d("PRAGYAN", terseResult);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new MotionData object

        if (!isSameInterval(motionData.getTimeStamp())) {
            db.collection("users").document(new Date().toString())
                .collection("trips").document(prevTrip)
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
        } else {
            // Same activity
            db.collection("users").document(new Date().toString())
                    .collection("trips").document(prevTrip)
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

}
