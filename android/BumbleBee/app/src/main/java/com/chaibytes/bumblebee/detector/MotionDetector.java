package com.chaibytes.bumblebee.detector;

import android.content.Context;
import android.util.Log;

import com.chaibytes.bumblebee.backend.Backend;
import com.chaibytes.bumblebee.backend.CloudFirestoreDatabase;
import com.chaibytes.bumblebee.data.MotionData;

/**
 * Listens when there is an update in Motion and saves differential
 * state to the backend.
 */

public class MotionDetector {
    private static final String TAG = MotionDetector.class.getSimpleName();
    private static Backend backendCloudFirestore = new CloudFirestoreDatabase();

    public static void updateData(String terseResult, MotionData motionData, Context context) {
        // Check the data and decide if there needs to be a delta reported and stored to the backend.
        Log.d("dataPoint", terseResult);
        backendCloudFirestore.saveData(motionData, context);
    }

}
