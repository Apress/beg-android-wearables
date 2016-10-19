package com.ocddevelopers.androidwearables.customwatchfaces;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * Receives instructions from the handheld configuration activity to increment or decrement the
 * value of the count and updates the Data API accordingly.
 */
public class ConvergenceWearableListenerService extends WearableListenerService {
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (!messageEvent.getPath().equals(ConvergenceUtil.PATH_CONTINUOUS_SWEEP)) {
            return;
        }

        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(10, TimeUnit.SECONDS);
        if (!connectionResult.isSuccess()) {
            return;
        }

        boolean continuousSweep = messageEvent.getData()[0] > 0;
        ConvergenceUtil.putContinuousSweep(mGoogleApiClient, continuousSweep);

        mGoogleApiClient.disconnect();
    }
}
