package com.chaibytes.bumblebee;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.chaibytes.bumblebee.data.MotionData;
import com.chaibytes.bumblebee.detector.MotionDetector;
import com.samsung.android.sdk.motion.Smotion;
import com.samsung.android.sdk.motion.SmotionPedometer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.chaibytes.bumblebee.MainActivity.TAG;

/**
 * Created by arunesh on 11/4/17.
 */

public class PedometerTracker {

    private SmotionPedometer mPedometer = null;

    private final static String[] sResults = {
            "Calorie", "Distance", "Speed", "Total Count", "Run Flat Count", "Walk Flat Count",
            "Run Up Count", "Run Down Count", "Walk Up Count", "Walk Down Count"
    };

    private int mMode = MotionTest.MODE_PEDOMETER;

    private Timer mTimer;

    private SmotionPedometer.Info mInfo;
    private PedometerCallback callback;

    private long mInterval = 1000;

    private boolean mIsUpDownAvailable;
    private Context context;
    private MotionDetector motionDetector;
    private ArrayList<MotionData> offlineData;

    public interface PedometerCallback {
        void onDataAvailable(MotionData motionData);
    }

    PedometerTracker(Context context, Looper looper, Smotion motion, boolean isUpDownAvailable) {
        this.context = context;
        motionDetector = new MotionDetector(context);
        mPedometer = new SmotionPedometer(looper, motion);
        mIsUpDownAvailable = isUpDownAvailable;
        offlineData = new ArrayList<>();
        initialize();
    }

    void setCallback(PedometerCallback callback) {
        this.callback = callback;
    }

    void start(int mode) {
        mMode = mode;

        initialize();
        mPedometer.start(changeListener);
        if (mMode == MotionTest.MODE_PEDOMETER_PERIODIC) {
            startTimer();
        }
    }

    void stop() {
        mPedometer.stop();
        if (mMode == MotionTest.MODE_PEDOMETER_PERIODIC) {
            stopTimer();
        }
        initialize();
    }

    void addOfflineData(ArrayList<MotionData> dataList) {
        offlineData.addAll(dataList);
    }

    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new PedometerTracker.MyTimer(), 0, mInterval);
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    void initialize() {
        String status = "Ready";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < sResults.length; i++) {
            if (i >= 6) {
                if (mIsUpDownAvailable) {
                    sb.append(sResults[i] + " : \n");
                }
            } else {
                sb.append(sResults[i] + " : \n");
            }
        }
        if (mMode == MotionTest.MODE_PEDOMETER_PERIODIC
                || MotionTest.mTestMode == MotionTest.MODE_PEDOMETER_PERIODIC) {
            sb.append("Interval : ");
        }
        Log.i(TAG, "Pedometer ready: " + sb.toString());
    }

    void cleanup() {
        motionDetector.cleanup();
    }

    private String getStatus(int status) {
        String str = null;
        switch (status) {
            case SmotionPedometer.Info.STATUS_WALK_UP:
                str = "Walk Up";
                break;
            case SmotionPedometer.Info.STATUS_WALK_DOWN:
                str = "Walk Down";
                break;
            case SmotionPedometer.Info.STATUS_WALK_FLAT:
                str = "Walk";
                break;
            case SmotionPedometer.Info.STATUS_RUN_DOWN:
                str = "Run Down";
                break;
            case SmotionPedometer.Info.STATUS_RUN_UP:
                str = "Run Up";
                break;
            case SmotionPedometer.Info.STATUS_RUN_FLAT:
                str = "Run";
                break;
            case SmotionPedometer.Info.STATUS_STOP:
                str = "Stop";
                break;
            case SmotionPedometer.Info.STATUS_UNKNOWN:
                str = "Unknown";
                break;
            default:
                break;
        }
        return str;
    }

    private SmotionPedometer.ChangeListener changeListener = new SmotionPedometer.ChangeListener() {

        @Override
        public void onChanged(SmotionPedometer.Info info) {
            // TODO Auto-generated method stub
            if (mMode == MotionTest.MODE_PEDOMETER) {
                displayData(info);
            }
        }
    };

    private void displayData(SmotionPedometer.Info info) {
        // TODO Auto-generated method stub
        long timestamp = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        double calorie = info.getCalorie();
        double distance = info.getDistance();
        double speed = info.getSpeed();
        long totalCount = info.getCount(SmotionPedometer.Info.COUNT_TOTAL);
        long runCount = info.getCount(SmotionPedometer.Info.COUNT_RUN_FLAT);
        long walkCount = info.getCount(SmotionPedometer.Info.COUNT_WALK_FLAT);
        long runUpCount = info.getCount(SmotionPedometer.Info.COUNT_RUN_UP);
        long runDownCount = info.getCount(SmotionPedometer.Info.COUNT_RUN_DOWN);
        long walkUpCount = info.getCount(SmotionPedometer.Info.COUNT_WALK_UP);
        long walkDownCount = info.getCount(SmotionPedometer.Info.COUNT_WALK_DOWN);

        sb.append(sResults[0] + " : " + calorie + "\n");
        sb.append(sResults[1] + " : " + distance + "\n");
        sb.append(sResults[2] + " : " + speed + "\n");
        sb.append(sResults[3] + " : " + totalCount + "\n");
        sb.append(sResults[4] + " : " + runCount + "\n");
        sb.append(sResults[5] + " : " + walkCount + "\n");

        String terseResult = String.format(Locale.getDefault(), "Cal(%2.2f), D(%2.2f), SP(%2.2f), TOT(%d), RC(%d), WC(%d)",  calorie, distance, speed, totalCount,
                runCount, walkCount);

        if (mIsUpDownAvailable) {
            sb.append(sResults[6] + " : " + runUpCount + "\n");
            sb.append(sResults[7] + " : " + runDownCount + "\n");
            sb.append(sResults[8] + " : " + walkUpCount + "\n");
            sb.append(sResults[9] + " : " + walkDownCount + "\n");
            terseResult += String.format(Locale.getDefault(), " RUP(%d), RDN(%d), WUP(%d), WDN(%d)", runUpCount, runDownCount, walkUpCount, walkDownCount);
        }
        if (mMode == MotionTest.MODE_PEDOMETER_PERIODIC
                || MotionTest.mTestMode == MotionTest.MODE_PEDOMETER_PERIODIC) {
            sb.append("Interval : " + mInterval / 1000 + " sec");
        }
        String str = getStatus(info.getStatus());

        if (str != null) {
            //Update the state to MotionDetector class
            DecimalFormat df = new DecimalFormat("#.##");
            MotionData md = new MotionData(timestamp, Double.valueOf(df.format(calorie)),
                    Double.valueOf(df.format(distance)), speed, runCount, walkCount);
            motionDetector.addData(terseResult, md);

            if (callback != null) {
                callback.onDataAvailable(md);
            }
        }
    }

    class MyTimer extends TimerTask {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mInfo = mPedometer.getInfo();
            handler.sendEmptyMessage(0);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (!offlineData.isEmpty()) {
                MotionData motionData = offlineData.remove(0);
                if (callback != null) {
                    Log.i(TAG, "Using offline data point : " + motionData.toString());
                    callback.onDataAvailable(motionData);
                } else {
                    Log.i(TAG, "Dropping offline data point.");
                }
            } else {
                // TODO Auto-generated method stub
                if (mInfo != null) {
                    // MotionTest.playSound();
                    displayData(mInfo);
                }
            }
        }
    };
}
