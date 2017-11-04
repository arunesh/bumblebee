package com.chaibytes.bumblebee.backend;

import android.content.Context;
import android.os.Environment;
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
    private static final String FILE_PATH = "bumblebee";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String START_LOCATION = "start_location";

    private File myExternalFile;
    private FileOutputStream fileOutputStream;
    private Context context;

    public CloudFirestoreDatabase(Context context) {
        this.context = context;
        String filename = createFileName();
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e(TAG, "Unable to save to external directory.");
        } else {
            createExternalFile(FILE_PATH, filename);
            Log.e(TAG, "Attempting to create file: " + filename);
            try {
                fileOutputStream = new FileOutputStream(myExternalFile);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "FILE CREATION FAILED.");
            }
        }
    }

    private String createFileName() {
        String dateString = new Date().toString().replace(' ', '_')
                .replace(':', '_');
        return FILE_PREFIX + dateString;
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void createExternalFile(String filepath, String filename) {
        myExternalFile = new File(context.getExternalFilesDir(filepath), filename);
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
        if (fileOutputStream == null) {
            return;
        }
        try {
            fileOutputStream.write(motionData.toString().getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Exception saving to file: " + e);
        }
    }

    // TODO: Call this !
    private void closeMotionDataFile() {
        if (fileOutputStream == null) {
            return;
        }
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
