package com.ocddevelopers.androidwearables.customwatchfaces;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * A wearable configuration activity for the Convergence watch face. Lets users change the value of
 * continuous sweep.
 */
public class ConvergenceWearableConfigActivity extends Activity {
    private GoogleApiClient mGoogleApiClient;
    private CheckBox mContinuousSweep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convergence_config);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();

        mContinuousSweep = (CheckBox) findViewById(R.id.continuous_sweep);
        mContinuousSweep.setOnCheckedChangeListener(mCheckedChangeListener);
        mContinuousSweep.setEnabled(false);
    }

    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener =
            new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ConvergenceUtil.putContinuousSweep(mGoogleApiClient, isChecked);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            initializeContinuousSweep();
        }

        @Override
        public void onConnectionSuspended(int i) {
        }
    };

    private void initializeContinuousSweep() {
        ConvergenceUtil.fetchContinuousSweep(mGoogleApiClient,
                new ConvergenceUtil.FetchContinuousSweepCallback() {
            @Override
            public void onContinuousSweepFetched(boolean continuousSweep) {
                mContinuousSweep.setChecked(continuousSweep);
                mContinuousSweep.setEnabled(true);
            }
        });
    }
}
