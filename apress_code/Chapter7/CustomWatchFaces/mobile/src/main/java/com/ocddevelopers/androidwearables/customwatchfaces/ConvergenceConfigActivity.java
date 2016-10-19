package com.ocddevelopers.androidwearables.customwatchfaces;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.wearable.companion.WatchFaceCompanion;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

/**
 * A configuration activity for the Convergence watch face. Lets users change the value of
 * continuous sweep.
 */
public class ConvergenceConfigActivity extends ActionBarActivity {
    public static final String PATH_CONTINUOUS_SWEEP = "/convergence/sweep";
    public static final String KEY_CONTINUOUS_SWEEP = "sweep";
    private CheckBox mContinuousSweep;
    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convergence_config);

        mContinuousSweep = (CheckBox) findViewById(R.id.continuous_sweep);
        mContinuousSweep.setEnabled(false);
        mContinuousSweep.setOnCheckedChangeListener(mCheckedChangeListener);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
    }

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

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {

            if(mPeerId == null) {
                // unavailable
                Toast.makeText(getApplicationContext(), "not connected to wearable",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // get current checkbox state
                fetchContinuousSweep();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private void fetchContinuousSweep() {
        Uri uri = new Uri.Builder()
                .scheme("wear")
                .path(PATH_CONTINUOUS_SWEEP)
                .authority(mPeerId)
                .build();

        Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(
                new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                DataItem dataItem = dataItemResult.getDataItem();
                initializeContinuousSweep(dataItem);
            }
        });
    }

    private void initializeContinuousSweep(DataItem dataItem) {
        if(dataItem != null) {
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
            DataMap config = dataMapItem.getDataMap();
            boolean continuousSweep = config.getBoolean(KEY_CONTINUOUS_SWEEP);
            mContinuousSweep.setChecked(continuousSweep);
        }
        mContinuousSweep.setEnabled(true);
    }

    private CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            sendConfigUpdateMessage(isChecked);
        }
    };

    private void sendConfigUpdateMessage(boolean continuousSweep) {
        if (mPeerId != null) {
            byte[] rawData = new byte[1];
            if(continuousSweep) {
                rawData[0] = 1;
            }
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, PATH_CONTINUOUS_SWEEP, rawData);
        }
    }

}
