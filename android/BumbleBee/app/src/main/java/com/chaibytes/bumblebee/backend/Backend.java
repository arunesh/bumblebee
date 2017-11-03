package com.chaibytes.bumblebee.backend;

import android.location.Location;

import com.chaibytes.bumblebee.data.MotionData;

/**
 * Interfaces with the Backend
 */

public interface Backend {
    // Save new trip data
    void saveNewTripData(MotionData motionData, String newTripripName);


    // Update data to existing trip
    void updateTripData(MotionData motionData, String currentTripName);


    // Save location data
    void saveLocationData(Location locationData, String tripName);
}
