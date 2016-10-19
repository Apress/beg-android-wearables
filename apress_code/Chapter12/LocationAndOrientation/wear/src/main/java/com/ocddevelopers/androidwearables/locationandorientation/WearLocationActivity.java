package com.ocddevelopers.androidwearables.locationandorientation;

import android.app.Activity;
import android.content.pm.PackageManager;
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
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Locale;

/**
 * Shows how to obtain location on Android Wear.
 */
public class WearLocationActivity extends Activity {
    private static final int UPDATE_INTERVAL_MS = 500;
    private static final int FASTEST_INTERVAL_MS = 500;
    private static final String TAG = "Wear";
    private GoogleApiClient mGoogleApiClient;
    private TextView mLatitudeText, mLongitudeText, mAltitudeText, mWarningText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mLatitudeText = (TextView) findViewById(R.id.latitude);
        mLongitudeText = (TextView) findViewById(R.id.longitude);
        mAltitudeText = (TextView) findViewById(R.id.altitude);
        mWarningText = (TextView) findViewById(R.id.warning);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        removeLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            requestLocationUpdates();

            Wearable.NodeApi.addListener(mGoogleApiClient, mNodeListener);

            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                    new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    if(result.getNodes().size() == 0) {
                        // the watch is not paired to the Android Wear device
                        updateWarning(false);
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

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
                        if (status.getStatus().isSuccess()) {
                            Log.d(TAG, "Successfully requested location updates");
                        } else {
                            Log.e(TAG,
                                    "Failed in requesting location updates, "
                                            + "status code: "
                                            + status.getStatusCode()
                                            + ", message: "
                                            + status.getStatusMessage());
                        }
                    }
                });
    }

    private void removeLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, mLocationListener);
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLatitudeText.setText(String.format(Locale.US, "%.6f", location.getLatitude()));
            mLongitudeText.setText(String.format(Locale.US, "%.6f", location.getLongitude()));
            mAltitudeText.setText(String.format(Locale.US, "%.2f", location.getAltitude()));
        }
    };

    private NodeApi.NodeListener mNodeListener = new NodeApi.NodeListener() {
        @Override
        public void onPeerConnected(Node node) {
            updateWarning(true);
        }

        @Override
        public void onPeerDisconnected(Node node) {
            updateWarning(false);
        }
    };

    private boolean hasGps() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private void updateWarning(final boolean phoneAvailable) {
        // ensure that mWarningText.setText is called from the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = "";
                if(!phoneAvailable && !hasGps()) {
                    message = "GPS unavailable. Pair watch to the phone.";
                }

                mWarningText.setText(message);
            }
        });
    }
}
