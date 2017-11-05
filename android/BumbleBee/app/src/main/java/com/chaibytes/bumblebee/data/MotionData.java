package com.chaibytes.bumblebee.data;

import java.text.DateFormat;
import java.util.Formatter;
import java.util.Locale;

/**
 * Data for user's Motion
 */

public class MotionData {
    private static final String WALK_STATE = "WALK";
    private static final String RUN_STATE = "RUN";
    private static final String NONE_STATE = "STAND";
    long timeStamp;
    private double calorie;
    private double distance;
    private double speed;
    private long runCount;
    private long walkCount;

    private String dateFormatted;

    private UserLocation userLocation;
    private String motionState = NONE_STATE;

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

    public static MotionData fromString(String str) {
        String[] fields = str.split(",");
        if (fields.length < 9) return null;
        long timestamp = Long.valueOf(fields[0].trim());
        double latitude = Double.valueOf(fields[1].trim());
        double longitude = Double.valueOf(fields[2].trim());
        double calorie = Double.valueOf(fields[3].trim());
        double distance = Double.valueOf(fields[4].trim());
        double speed = Double.valueOf(fields[5].trim());
        long runCount = Long.valueOf(fields[6].trim());
        long walkCount = Long.valueOf(fields[7].trim());
        String motionState = fields[8].trim();
        MotionData motionData = new MotionData(timestamp, calorie, distance, speed, runCount, walkCount);
        motionData.setUserLocation(new UserLocation(latitude, longitude, timestamp));
        motionData.setMotionState(motionState);
        return motionData;
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

    public void setMotionState(String state) {
        motionState = state;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    public String getMotionState() {
        return motionState;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);

        return formatter.format("%d, %f, %f, %f, %f, %f, %d, %d, %s\n", timeStamp,
                userLocation.getmLatitude(), userLocation.getmLongitude(), calorie,
                distance, speed, runCount, walkCount, motionState).toString();
    }

    public void computeDiff(MotionData prevMotionData) {
        boolean isWalk = getWalkCount() > prevMotionData.getWalkCount();
        boolean isRun = getRunCount() > prevMotionData.getRunCount();
        if (isRun) {
            motionState = RUN_STATE;
        } else if (isWalk) {
            motionState = WALK_STATE;
        } else {
            motionState = NONE_STATE;
        }
    }
}
