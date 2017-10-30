package com.chaibytes.bumblebee.detector;

import android.support.annotation.NonNull;
import android.util.Log;

import com.chaibytes.bumblebee.data.MotionData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Listens when there is an update in Motion and saves differential
 * state to the backend.
 */

public class MotionDetector {
    private static final String TAG = MotionDetector.class.getSimpleName();

    public static void updateData(String terseResult, MotionData motionData) {
        // Check the data and decide if there needs to be a delta reported and stored to the backend.
        Log.d("PRAGYAN", terseResult);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Add a new MotionData object
        db.collection("userdata").add(motionData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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
