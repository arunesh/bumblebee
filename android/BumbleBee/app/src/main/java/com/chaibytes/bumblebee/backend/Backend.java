package com.chaibytes.bumblebee.backend;

import android.content.Context;

import com.chaibytes.bumblebee.data.MotionData;

/**
 * Interfaces with the Backend
 */

public interface Backend {
    // Save data
    void saveData(MotionData motionData, Context context);
}
