package com.ocddevelopers.androidwearables.gestureandvoice;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.util.concurrent.TimeUnit;


/**
 * Utilizes the gyroscopic sensor to detect head nod gestures.
 */
public class HeadGestureDetector {
    public interface HeadGestureListener {
        void onNodUp();
        void onNodDown();
        void onNodLeft();
        void onNodRight();
    }

    private HeadGestureListener mHeadGestureListener;
    private NodDetector[] mNodDetectors;

    public HeadGestureDetector() {
        mNodDetectors = new NodDetector[4];
        for(int i=0; i<mNodDetectors.length; ++i) {
            mNodDetectors[i] = new NodDetector();
        }
    }

    public HeadGestureListener getHeadGestureListener() {
        return mHeadGestureListener;
    }

    public void setHeadGestureListener(HeadGestureListener listener) {
        mHeadGestureListener = listener;
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() != Sensor.TYPE_GYROSCOPE) {
            return;
        }
        if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        // values in radians per second
        float axisX = sensorEvent.values[0];
        float axisY = sensorEvent.values[1];
        float axisZ = sensorEvent.values[2];

        // up, down, left, right
        float[] values = new float[] { axisX, -axisX, axisY, -axisY };

        for(int index = 0; index<4; ++index) {
            if (detectNod(index, sensorEvent.timestamp, values[index])) {
                triggerNod(index);
            }
        }
    }

    private boolean detectNod(int index, long timestamp, float angularVelocity) {
        if(mNodDetectors[index].addAngularVelocity(timestamp, angularVelocity)) {
            if (mHeadGestureListener != null) {
                return true;
            }
        }

        return false;
    }

    private void triggerNod(int index) {
        switch(index) {
            case 0:
                mHeadGestureListener.onNodUp();
                break;
            case 1:
                mHeadGestureListener.onNodDown();
                break;
            case 2:
                mHeadGestureListener.onNodLeft();
                break;
            case 3:
                mHeadGestureListener.onNodRight();
                break;
        }
    }

    private static class NodDetector {
        private static float THRESHOLD = 2f;
        private static float SMALL_THRESH = 0.1f;
        private static long MAX_DURATION = TimeUnit.MILLISECONDS.toNanos(1000);
        private long mStartTime = -1;

        public boolean addAngularVelocity(long timestamp, float angularVelocity) {
            if(mStartTime == -1 && angularVelocity > THRESHOLD) {
                // in nanoseconds
                mStartTime = timestamp;
            } else if(mStartTime > 0 && angularVelocity < SMALL_THRESH) {
                long deltaTime = timestamp - mStartTime;
                mStartTime = -1;
                if(deltaTime < MAX_DURATION) {
                    return true;
                }
            }

            return false;
        }
    }
}
