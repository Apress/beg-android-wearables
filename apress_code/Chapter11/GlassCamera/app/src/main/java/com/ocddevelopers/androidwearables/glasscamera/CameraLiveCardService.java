package com.ocddevelopers.androidwearables.glasscamera;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;

/**
 * Shows how to display a camera preview on a LiveCard.
 */
public class CameraLiveCardService extends Service {
    private static final String LIVECARD_TAG = "CamCard";
    private LiveCard mLiveCard;
    private Camera mCamera;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        publishCard();
        return START_NOT_STICKY;
    }

    private void publishCard() {
        if(mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVECARD_TAG);

            Intent menuIntent = new Intent(this, CameraMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.setDirectRenderingEnabled(true);

            mCamera = Camera.open();
            LiveCameraPreview renderer = new LiveCameraPreview(mLiveCard, mCamera);
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
    }

    @Override
    public void onDestroy() {
        unpublishCard();
        super.onDestroy();
    }

    private void unpublishCard() {
        if(mLiveCard != null) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
