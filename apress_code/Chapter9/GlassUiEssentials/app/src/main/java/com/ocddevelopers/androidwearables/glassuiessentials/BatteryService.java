package com.ocddevelopers.androidwearables.glassuiessentials;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

/**
 * Demonstrates how to create a LiveCard that updates with low-frequency rendering.
 */
public class BatteryService extends Service {
    private static final String LIVE_CARD_TAG = "BatteryCard";
    private LiveCard mLiveCard;
    private RemoteViews mRemoteViews;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            // live cards must have a menu
            Intent menuIntent = new Intent(this, BatteryMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);

            // register battery receiver
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(mBatteryLevelReceiver, ifilter);

            mRemoteViews = new RemoteViews(getPackageName(), R.layout.centertext);
            updateRemoteViews(batteryStatus);
        }

        return START_STICKY;
    }

    private void updateRemoteViews(Intent batteryStatus) {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // get battery percentage and round to nearest integer
        int batteryPct = Math.round(level / (float)scale * 100);
        mRemoteViews.setTextViewText(R.id.text, batteryPct + "%");
        mLiveCard.setViews(mRemoteViews);
    }

    @Override
    public void onDestroy() {
        if(mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;

            unregisterReceiver(mBatteryLevelReceiver);
        }

        super.onDestroy();
    }

    private BroadcastReceiver mBatteryLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRemoteViews(intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
