package com.ocddevelopers.androidwearables.wearuiessentials;

import android.os.Handler;
import android.os.SystemClock;

/**
 * Simulates values for speed, distance, and calories for a person running. In a real app,
 * this data would be calculated based on GPS, but we use somewhat arbitrary
 * simulated values for the purposes of this example.
 */
public class SimulatedRunStatsReceiver {
    private static final float MILLIS_PER_SECOND = 1000f;
    private static final float SECONDS_PER_MINUTE = 60;
    private static final float SECONDS_PER_HOUR = 3600;
    private static final float PERIOD_IN_SECONDS = 8*SECONDS_PER_MINUTE;
    private static final float MPH_PER_METER_PER_SECOND = 2.23694f;
    private static final float METERS_PER_MILE = 1609.34f;
    private static final int MIN_SPEED_MPH = 5;
    private static final int MAX_SPEED_MPH = 8;
    private RunCallback mRunCallback;
    private Handler mHandler;
    private float mSimulatedDistance, mSimulatedCalories;
    private long mStartTimeMillis, mPrevTimeMillis;

    public interface RunCallback {
        void runStatsReceived(float distance, float speed, float calories);
    }

    public SimulatedRunStatsReceiver() {
        mHandler = new Handler();
    }

    public RunCallback getRunCallback() {
        return mRunCallback;
    }

    public void setRunCallback(RunCallback runCallback) {
        mRunCallback = runCallback;
    }

    public void startRun() {
        mSimulatedDistance = 0f;
        mSimulatedCalories = 0f;
        mStartTimeMillis = SystemClock.elapsedRealtime();
        mPrevTimeMillis = mStartTimeMillis;
        mStatsRunnable.run();
    }

    public void stopRun() {
        mHandler.removeCallbacks(mStatsRunnable);
    }

    private Runnable mStatsRunnable = new Runnable() {
        @Override
        public void run() {
            /* All simulated values should be coherent. We will choose a reasonable running speed
            use it to calculate distance and calories. */
            long now = SystemClock.elapsedRealtime();
            float timeElapsedSeconds = (now - mStartTimeMillis)/MILLIS_PER_SECOND;
            float deltaTimeSeconds = (now - mPrevTimeMillis)/MILLIS_PER_SECOND;

            /* Speed is chosen to fluctuate between MIN_SPEED_MPH and MAX_SPEED_MPH. We accomplish
            this with with a sinusoid of period PERIOD_IN_SECONDS.

            Don't spend too much time trying to figure out how this function works. It's not that
            important. Just understand that the speed is generated as a function of time. */
            float simulatedSpeedMph = (float) (0.5*MIN_SPEED_MPH + 0.5*MAX_SPEED_MPH +
                    0.5*(MAX_SPEED_MPH - MIN_SPEED_MPH)*
                            Math.sin(2 * Math.PI / PERIOD_IN_SECONDS * timeElapsedSeconds));

            // The user travelled for a time of deltaTimeSeconds at a speed of mSimulatedSpeedMph.
            // Calculate how much distance the user travelled and add it to the total distance.
            double simulatedSpeedMetersPerSecond = simulatedSpeedMph / MPH_PER_METER_PER_SECOND;
            mSimulatedDistance += deltaTimeSeconds*simulatedSpeedMetersPerSecond;

            // We've already picked a speed and used it to calculate distance. Now, we must calculate
            // how many calories the user burned in the same time interval. We'll do so with the
            // formulas provided by the ACSM at http://certification.acsm.org/metabolic-calcs
            double simulatedSpeedMetersPerMin = simulatedSpeedMetersPerSecond*SECONDS_PER_MINUTE;
            /* grade is another way to represent the incline of the run. For more detail, see
             see http://www.livestrong.com/article/422012-what-is-10-degrees-in-incline-on-a-treadmill/
             note that a grade of 0.01 (that is, 1%) approximates running outside.*/
            double grade = 0.01;
            // vo2 is the s oxygen consumption
            double vo2 = 3.5 + 0.2*simulatedSpeedMetersPerMin+ 0.9*simulatedSpeedMetersPerMin*grade;
            // For more info on metabolic rate (MET), see http://en.wikipedia.org/wiki/Metabolic_equivalent
            double metabolicRate = vo2 / 3.5;

            // In a real app, the weight would be inserted by the user. We use an arbitrary value
            // for demonstration purposes.
            double weightInKg = 70;
            double deltaTimeInHours = deltaTimeSeconds/SECONDS_PER_HOUR;
            double deltaCalories = metabolicRate*weightInKg*deltaTimeInHours;
            mSimulatedCalories += deltaCalories;

            if(mRunCallback != null) {
                float distanceInMiles = mSimulatedDistance / METERS_PER_MILE;
                mRunCallback.runStatsReceived(distanceInMiles, simulatedSpeedMph,
                        mSimulatedCalories);
            }

            mHandler.postDelayed(mStatsRunnable, 1000);
            mPrevTimeMillis = now;
        }
    };
}
