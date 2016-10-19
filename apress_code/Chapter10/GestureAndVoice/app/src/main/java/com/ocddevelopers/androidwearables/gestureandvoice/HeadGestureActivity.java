package com.ocddevelopers.androidwearables.gestureandvoice;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;

/**
 * Displays the gestures detected by HeadGestureDetector for demonstration purposes.
 */
public class HeadGestureActivity extends Activity {
    private HeadGestureDetector mHeadGestureDetector;
    private CardBuilder mCardBuilder;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowUtils.FLAG_DISABLE_HEAD_GESTURES);
        super.onCreate(savedInstanceState);

        mCardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT);
        updateText("Listening for head gestures...");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if(mSensor == null) {
            Toast.makeText(this, "Rotation vector sensor required.", Toast.LENGTH_SHORT).show();
            finish();
        }

        mHeadGestureDetector = new HeadGestureDetector();
        mHeadGestureDetector.setHeadGestureListener(mHeadGestureListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    private void updateText(String text) {
        mCardBuilder.setText(text);
        setContentView(mCardBuilder.getView());
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mHeadGestureDetector.onSensorChanged(event);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private HeadGestureDetector.HeadGestureListener mHeadGestureListener =
            new HeadGestureDetector.HeadGestureListener() {
        @Override
        public void onNodUp() {
            updateText("nod up detected");
        }

        @Override
        public void onNodDown() {
            updateText("nod down detected");
        }

        @Override
        public void onNodLeft() {
            updateText("nod left detected");
        }

        @Override
        public void onNodRight() {
            updateText("nod right detected");
        }
    };
}
