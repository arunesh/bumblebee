package com.chaibytes.bumblebee.data;

/**
 * Contains User's Location data
 */

public class UserLocation {

    private double mLatitude;

    public double getmLatitude() {
        return mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public long getmTimeStamp() {
        return mTimeStamp;
    }

    private double mLongitude;
    private long mTimeStamp;

    public UserLocation(double latitude, double longitude, long time) {
        mLatitude = latitude;
        mLongitude = longitude;
        mTimeStamp = time;
    }
}
