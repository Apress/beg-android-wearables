package com.ocddevelopers.androidwearables.locationandorientation;

import android.app.Activity;
import android.content.Context;
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
import java.util.Locale;

/**
 * Shows how to obtain location on Glass.
 */
public class GpsActivity extends Activity {
    private LocationManager mLocationManager;
    private TextView mLatitudeText, mLongitudeText, mAltitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_gps, null);
        setContentView(new TuggableView(this, content));

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLatitudeText = (TextView) content.findViewById(R.id.latitude);
        mLongitudeText = (TextView) content.findViewById(R.id.longitude);
        mAltitudeText = (TextView) content.findViewById(R.id.altitude);
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
        requestGpsUpdates();
    }

    @Override
    protected void onPause() {
        removeGpsUpdates();
        super.onPause();
    }

    private void requestGpsUpdates() {
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);

        List<String> providers = mLocationManager.getProviders(criteria, true);

        for (String provider : providers) {
            mLocationManager.requestLocationUpdates(provider, 0,
                    0, mLocationListener);
        }
    }

    private void removeGpsUpdates() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLatitudeText.setText(String.format(Locale.US, "%.6f", location.getLatitude()));
            mLongitudeText.setText(String.format(Locale.US, "%.6f", location.getLongitude()));
            mAltitudeText.setText(String.format(Locale.US, "%.2f", location.getAltitude()));
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

}
