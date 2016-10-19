package com.ocddevelopers.androidwearables.glassuiessentials;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;

/**
 * Creates a LiveCard that displays a speedometer that updates with high-frequency rendering.
 */
public class SpeedService extends Service {
    private static final String LIVE_CARD_TAG = "SpeedometerCard";
    private LiveCard mLiveCard;
    private SpeedRenderer mSpeedRenderer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        publishCard();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unpublishCard();
        super.onDestroy();
    }

    private void publishCard() {
        if(mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            // live cards must have a menu
            Intent menuIntent = new Intent(this,SpeedMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mSpeedRenderer = new SpeedRenderer(this);
            mLiveCard.setDirectRenderingEnabled(true);
            mLiveCard.getSurfaceHolder().addCallback(mSpeedRenderer);
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
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
