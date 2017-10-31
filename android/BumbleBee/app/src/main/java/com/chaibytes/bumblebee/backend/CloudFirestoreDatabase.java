package com.chaibytes.bumblebee.backend;

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

    private static int iNum = 0;
    private static int iUserDataNum = 0;
    private static long prevTimeStamp = 0L;
    private static final long MIN_DURATION_MS = 15000L * 60L; // 15 mins in ms
    private static String prevTrip = "";
    private static String dataSet = "";

    private FirebaseFirestore db;
    private MotionData motionData;
    private String date = null;

    @Override
    public void saveData(MotionData motionData) {
        db = FirebaseFirestore.getInstance();
        this.motionData = motionData;

        date = DateFormat.getDateInstance().format(new Date());

        if (!isSameInterval(motionData.getTimeStamp())) {
            // Add a new MotionData object
            saveNewdata();
        } else {
            // Same activity
            updateData();
        }
    }

    private void saveNewdata() {
        db.collection("users").document(date)
                .collection(prevTrip).document(dataSet)
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
        iUserDataNum++;
        dataSet = "dataSet" + iUserDataNum;
        db.collection("users").document(date)
                .collection(prevTrip).document(dataSet)
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
            iUserDataNum = 1;
            prevTrip = "trip" + iNum;
            dataSet = "dataSet" + iUserDataNum;
            return false;
        }
    }
}
