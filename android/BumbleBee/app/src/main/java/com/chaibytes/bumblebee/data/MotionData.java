package com.chaibytes.bumblebee.data;

/**
 * Data for user's Motion
 */

public class MotionData {
    long timeStamp;
    double calorie;
    double distance;
    double speed;
    long runCount;
    long walkCount;

    public MotionData(long timeStamp, double calorie, double distance, double speed, long runCount,
               long walkCount) {

        this.timeStamp = timeStamp;
        this.calorie = calorie;
        this.distance = distance;
        this.speed = speed;
        this.runCount = runCount;
        this.walkCount = walkCount;
    }

}
