package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.TextView;

import java.util.Locale;

/**
 * Displays basic stats for a person running, including duration, distance, speed, and calories.
 * The values from the stats are generated since the focus of this example is on the user interface.
 */
public class RunStatsActivity extends Activity {
    private SimulatedRunStatsReceiver mStatsReceiver;
    private TextView mDuration, mDistance, mSpeed, mCalories;
    private Handler mHandler;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stats);

        mDuration = (TextView) findViewById(R.id.duration);
        mDistance = (TextView) findViewById(R.id.distance);
        mSpeed = (TextView) findViewById(R.id.speed);
        mCalories = (TextView) findViewById(R.id.calories);

        mStatsReceiver = new SimulatedRunStatsReceiver();
        mStatsReceiver.setRunCallback(mRunCallback);
        mStartTime = SystemClock.elapsedRealtime();
        mStatsReceiver.startRun();

        mHandler = new Handler();
        mHandler.postDelayed(mDurationRunnable, 250);
    }

    private SimulatedRunStatsReceiver.RunCallback mRunCallback = new SimulatedRunStatsReceiver.RunCallback() {
        @Override
        public void runStatsReceived(float distance, float speed, float calories) {
            mDistance.setText(String.format(Locale.US, "%.2f", distance));
            mSpeed.setText(String.format(Locale.US, "%.1f", speed));
            mCalories.setText(String.format(Locale.US, "%.1f", calories));
        }
    };

    private Runnable mDurationRunnable = new Runnable() {
        @Override
        public void run() {
            long durationInSeconds = (SystemClock.elapsedRealtime() - mStartTime + 500L)/1000L;
            int hours = (int) (durationInSeconds/3600);
            int minutes = (int) ((durationInSeconds%3600)/60);
            int seconds = (int) (durationInSeconds%60);
            String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            if(!time.equals(mDuration.getText())) {
                mDuration.setText(time);
            }

            mHandler.postDelayed(mDurationRunnable, 250);
        }
    };

    @Override
    protected void onDestroy() {
        mStatsReceiver.stopRun();
        super.onDestroy();
    }

}
