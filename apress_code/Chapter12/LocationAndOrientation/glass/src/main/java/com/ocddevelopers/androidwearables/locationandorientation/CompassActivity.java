package com.ocddevelopers.androidwearables.locationandorientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

import java.util.List;

/**
 * Shows how to display a compass relative to true north.
 */
public class CompassActivity extends Activity {
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;
    private static final int ARM_DISPLACEMENT_DEGREES = 6;
    private static final int LOCATION_UPDATE_INTERVAL_MS = 60000;
    private static final int LOCATION_DISTANCE_METERS = 2;
    private boolean mLowAccuracy, mTooSteep;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor, mMagneticSensor;
    private LocationManager mLocationManager;
    private CompassView mCompassView;
    private TextView mWarningText;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private GeomagneticField mGeomagneticField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_compass, null);
        setContentView(new TuggableView(this, content));

        mCompassView = (CompassView) content.findViewById(R.id.compass);
        mWarningText = (TextView) content.findViewById(R.id.warning);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        if(mGeomagneticField == null) {
            Location lastLocation = mLocationManager
                    .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (lastLocation != null) {
                updateDeclination(lastLocation);
            }
        }

        registerForSensorUpdates();
        requestGpsUpdates();
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
        removeGpsUpdates();
        super.onPause();
    }

    private void removeGpsUpdates() {
        mLocationManager.removeUpdates(mLocationListener);
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

                float trueNorthDeg = computeTrueNorth(azimuthDeg);
                // Glass's movable arm may be at an angle between 0 and 12 degrees. Assume it is
                // at an average of 6 and draw the compass relative to the user's face.
                mCompassView.setAzimuth(trueNorthDeg - ARM_DISPLACEMENT_DEGREES);

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
     * Calculate geographic north from magnetic north
     *
     * @param azimuthDeg relative to magnetic north, in degrees
     * @return the azimuth relative to true north, in degrees
     */
    private float computeTrueNorth(float azimuthDeg) {
        if (mGeomagneticField != null) {
            return azimuthDeg + mGeomagneticField.getDeclination();
        } else {
            return azimuthDeg;
        }
    }

    private void updateWarning() {
        String warning;
        if(mLowAccuracy) {
            warning = "Glass is detecting too much interference";
        } else if(mTooSteep) {
            warning = "Keep Glass horizontal to use the compass";
        } else {
            warning = "";
        }

        mWarningText.setText(warning);
    }

    private void requestGpsUpdates() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        List<String> providers = mLocationManager.getProviders(criteria, true);

        for (String provider : providers) {
            mLocationManager.requestLocationUpdates(provider, LOCATION_UPDATE_INTERVAL_MS,
                    LOCATION_DISTANCE_METERS, mLocationListener);
        }
    }


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateDeclination(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private void updateDeclination(Location location) {
        mGeomagneticField = new GeomagneticField((float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                location.getTime());
    }
}
