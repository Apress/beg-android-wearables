package com.ocddevelopers.androidwearables.locationandorientation;

import android.app.Activity;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

/**
 * Shows how to display a compass relative to true north.
 */
public class CompassActivity extends Activity {
    private static final float TOO_STEEP_PITCH_DEGREES = 70.0f;
    private static final int UPDATE_INTERVAL_MS = 60*1000; // 1 minute
    private static final int FASTEST_INTERVAL_MS = 500;
    public static final String TAG = "Compass";
    private boolean mLowAccuracy, mTooSteep;
    private SensorManager mSensorManager;
    private Sensor mRotationVectorSensor, mMagneticSensor;
    private CompassView mCompassView;
    private TextView mWarningText;
    private float[] mRotationMatrix = new float[16];
    private float[] mOrientation = new float[3];
    private GoogleApiClient mGoogleApiClient;
    private GeomagneticField mGeomagneticField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        mCompassView = (CompassView) findViewById(R.id.compass);
        mWarningText = (TextView) findViewById(R.id.warning);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerForSensorUpdates();
        mGoogleApiClient.connect();
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
        removeLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    private void removeLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert rotation vector to azimuth, pitch, & roll
                SensorManager.getRotationMatrixFromVector(mRotationMatrix , event.values);

                SensorManager.getOrientation(mRotationMatrix, mOrientation);
                float azimuthDeg = (float) Math.toDegrees(mOrientation[0]);
                float pitchDeg = (float) Math.toDegrees(mOrientation[1]);
                float rollDeg = (float) Math.toDegrees(mOrientation[2]);

                float trueNorthDeg = computeTrueNorth(azimuthDeg);
                mCompassView.setAzimuth(trueNorthDeg);

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

    /**
     * display a warning when the azimuth cannot be reliably measured because there is * too much
     * magnetic interference or the pitch is too close too +/- 90 degrees, in which case azimuth
     * is undefined.
     */
    private void updateWarning() {
        String warning;
        if(mLowAccuracy) {
            warning = "Wear is detecting too much interference";
        } else if(mTooSteep) {
            warning = "The pitch value is approaching gimbal lock";
        } else {
            warning = "";
        }

        mWarningText.setText(warning);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, locationRequest, mLocationListener)
                .setResultCallback(new ResultCallback() {

                    @Override
                    public void onResult(Result result) {
                        Status status = result.getStatus();
                        if (!status.isSuccess()) {
                            Log.e(TAG, "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());
                        }
                    }
                });
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateDeclination(location);
        }
    };

    private void updateDeclination(Location location) {
        mGeomagneticField = new GeomagneticField((float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                location.getTime());
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Location lastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(lastLocation != null) {
                updateDeclination(lastLocation);
            }

            requestLocationUpdates();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };
}
