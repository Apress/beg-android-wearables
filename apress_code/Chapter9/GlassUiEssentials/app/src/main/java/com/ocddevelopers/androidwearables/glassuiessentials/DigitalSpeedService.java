package com.ocddevelopers.androidwearables.glassuiessentials;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.glass.timeline.LiveCard;

/**
 * Creates a LiveCard that updates with high-frequency rendering and displays the current speed
 * in digital form. The speed values are generated for demonstration purposes.
 */
public class DigitalSpeedService extends Service {
    private static final String LIVE_CARD_TAG = "DigitalSpeed";
    public static final String ACTION_CHANGE_UNITS =
            "com.ocddevelopers.androidwearables.glassuiessentials.action.CHANGE_UNITS";
    private LiveCard mLiveCard;
    private DigitalSpeedRenderer mSpeedRenderer;
    private boolean mUseMetric;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if(ACTION_CHANGE_UNITS.equals(action)) {
            changeUnits();
        } else {
            publishCard();
        }
    }

    private void changeUnits() {
        mUseMetric = !mUseMetric;
        mSpeedRenderer.setUseMetric(mUseMetric);
        generateMenu();
    }

    @Override
    public void onDestroy() {
        unpublishCard();
        super.onDestroy();
    }

    private void publishCard() {
        if(mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            generateMenu();

            mSpeedRenderer = new DigitalSpeedRenderer(this);
            mLiveCard.setDirectRenderingEnabled(true);
            mLiveCard.getSurfaceHolder().addCallback(mSpeedRenderer);
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        }
    }

    private void generateMenu() {
        // live cards must have a menu
        Intent menuIntent = new Intent(this, DigitalSpeedMenuActivity.class);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        menuIntent.putExtra(DigitalSpeedMenuActivity.EXTRA_USE_METRIC, mUseMetric);
        mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent,
                PendingIntent.FLAG_CANCEL_CURRENT));
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
