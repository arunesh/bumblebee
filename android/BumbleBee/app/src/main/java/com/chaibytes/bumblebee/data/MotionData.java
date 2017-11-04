package com.chaibytes.bumblebee.data;

import java.text.DateFormat;
import java.util.Formatter;
import java.util.Locale;

/**
 * Data for user's Motion
 */

public class MotionData {
    long timeStamp;
    private double calorie;
    private double distance;
    private double speed;
    private long runCount;
    private long walkCount;

    private String dateFormatted;

    private UserLocation userLocation;

    public double getCalorie() {
        return calorie;
    }

    public double getDistance() {
        return distance;
    }

    public double getSpeed() {
        return speed;
    }

    public long getRunCount() {
        return runCount;
    }

    public long getWalkCount() {
        return walkCount;
    }

    public MotionData(long timeStamp, double calorie, double distance, double speed, long runCount,
                      long walkCount) {

        this.timeStamp = timeStamp;
        dateFormatted = DateFormat.getDateTimeInstance().format(timeStamp);
        this.calorie = calorie;
        this.distance = distance;
        this.speed = speed;
        this.runCount = runCount;
        this.walkCount = walkCount;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        return formatter.format("%d, %f, %f, %f, %f, %f, %d, %d\n", timeStamp,
                userLocation.getmLatitude(), userLocation.getmLongitude(), calorie,
                distance, speed, runCount, walkCount).toString();
    }
}
