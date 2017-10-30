package com.chaibytes.bumblebee.detector;

import android.util.Log;

/**
 * Listens when there is an update in Motion and saves differential
 * state to the backend.
 */

public class MotionDetector {

    public static void updateData(StringBuffer sb, String terseResult) {
        // Check the data and decide if there needs to be a delta reported and stored to the backend.
        //Log.d("PRAGYAN", sb.toString());

        Log.d("PRAGYAN", terseResult);
    }
}
