package com.chaibytes.bumblebee.location;

import android.location.Location;

/**
 * Gets current location
 */

public interface LocationListener {
    void getCurrentUserLocation(Location location);
}
