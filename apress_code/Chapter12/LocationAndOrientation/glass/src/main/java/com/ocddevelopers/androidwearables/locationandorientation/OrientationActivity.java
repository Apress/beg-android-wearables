package com.ocddevelopers.androidwearables.locationandorientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

import java.util.Locale;

/**
 * Shows how to obtain the current orientation using the rotation vector sensor.
 */
public class OrientationActivity extends Activity {
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;
    private boolean mLowAccuracy, mTooSteep;
    private TextView mAzimuthText, mPitchText, mRollText, mWarningText;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor, mMagneticSensor;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_orientation, null);
        setContentView(new TuggableView(this, content));

        mAzimuthText = (TextView) findViewById(R.id.azimuth);
        mPitchText = (TextView) findViewById(R.id.pitch);
        mRollText = (TextView) findViewById(R.id.roll);
        mWarningText = (TextView) findViewById(R.id.warning);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            ((AudioManager)getSystemService(Context.AUDIO_SERVICE))
                    .playSoundEffect(Sounds.DISALLOWED);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerForSensorUpdates();
    }

    private void registerForSensorUpdates() {
        mSensorManager.registerListener(mSensorEventListener, mRotationVectorSensor,
                SensorManager.SENSOR_DELAY_UI);

        // obtain accuracy updates from the magnetic field sensor since the rotation vector
        // sensor does not provide any
        mSensorManager.registerListener(mSensorEventListener, mMagneticSensor,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert rotation vector to azimuth, pitch, & roll
                SensorManager.getRotationMatrixFromVector(mRotationMatrix , event.values);

                // take into account Glass's coordinate system
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, mRotationMatrix);

                SensorManager.getOrientation(mRotationMatrix, mOrientation);
                float azimuthDeg = (float) Math.toDegrees(mOrientation[0]);
                float pitchDeg = (float) Math.toDegrees(mOrientation[1]);
                float rollDeg = (float) Math.toDegrees(mOrientation[2]);

                mAzimuthText.setText(String.format(Locale.US, "%.1f", azimuthDeg));
                mPitchText.setText(String.format(Locale.US, "%.1f", pitchDeg));
                mRollText.setText(String.format(Locale.US, "%.1f", rollDeg));

                mTooSteep = pitchDeg > TOO_STEEP_PITCH_DEGREES ||
                        pitchDeg < -TOO_STEEP_PITCH_DEGREES;
                updateWarning();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            mLowAccuracy = accuracy < SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
            updateWarning();
        }
    };

    /**
     * display a warning when the azimuth cannot be reliably measured because there is * too much
     * magnetic interference or the pitch is too close too +/- 90 degrees, in which case azimuth
     * is undefined.
     */
    private void updateWarning() {
        String warning;
        if(mLowAccuracy) {
            warning = "Glass is detecting too much interference";
        } else if(mTooSteep) {
            warning = "The pitch value is approaching gimbal lock";
        } else {
            warning = "";
        }

        mWarningText.setText(warning);
    }

}
