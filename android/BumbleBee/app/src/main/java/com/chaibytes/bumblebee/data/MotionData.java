package com.chaibytes.bumblebee.data;

import java.text.DateFormat;

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

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

}
