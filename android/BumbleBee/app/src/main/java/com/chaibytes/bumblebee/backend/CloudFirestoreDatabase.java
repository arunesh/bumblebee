package com.chaibytes.bumblebee.backend;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.data.UserLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Interacts with Cloud Firestore
 */

public class CloudFirestoreDatabase implements Backend {
    private static final String TAG = CloudFirestoreDatabase.class.getSimpleName();
    private static final String FILE_PREFIX = "bumblebee-dump.txt_";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String START_LOCATION = "start_location";

    private FileOutputStream fileOutputStream;
    private Context context;

    public CloudFirestoreDatabase(Context context) {
        this.context = context;
        String filename =  FILE_PREFIX + new Date().toString();
        try {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveLocationData(final UserLocation locationType, String tripName) {
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

    private void saveMotionDataToFile(MotionData motionData) {
        try {
            fileOutputStream.write(motionData.toString().getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Exception saving to file: " + e);
        }
    }

    // TODO: Call this !
    private void closeMotionDataFile() {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception closing to file: " + e);
        }
    }

    @Override
    public void shutdown() {
        closeMotionDataFile();
    }

    @Override
    public void saveNewTripData(MotionData motionData, String newTripName) {
        saveMotionDataToFile(motionData);
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
        saveMotionDataToFile(motionData);
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
