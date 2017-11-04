package com.chaibytes.bumblebee.backend;

import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.data.UserLocation;

/**
 * Interfaces with the Backend
 */

public interface Backend {
    // Save new trip data
    void saveNewTripData(MotionData motionData, String newTripripName);


    // Update data to existing trip
    void updateTripData(MotionData motionData, String currentTripName);


    // Save location data
    void saveLocationData(UserLocation locationData, String tripName);

    void shutdown();
}
