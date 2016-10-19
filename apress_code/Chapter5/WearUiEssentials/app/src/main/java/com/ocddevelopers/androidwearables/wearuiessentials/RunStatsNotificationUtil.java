package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Utility class to create and hide the run stats notification.
 */
public class RunStatsNotificationUtil {
    public static final int NOTIFICATION_ID = 1;

    public static void showNotification(Context context) {
        PendingIntent statsActivityPendingIntent = makeDisplayPendingIntent(context);

        Intent stopRunIntent = RunStatsNotificationService.makeStopIntent(context);
        PendingIntent stopRunPendingIntent = PendingIntent.getService(context, 0,
                stopRunIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification runStatsNotification = new NotificationCompat.Builder(context)
                .setContentTitle("Run Stats")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher)
                .setCategory(Notification.CATEGORY_STATUS)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true)
                .addAction(R.drawable.ic_full_cancel, "Stop Run", stopRunPendingIntent)
                .extend(new NotificationCompat.WearableExtender()
                        .setDisplayIntent(statsActivityPendingIntent)
                        .setCustomSizePreset(NotificationCompat.WearableExtender.SIZE_FULL_SCREEN))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, runStatsNotification);
    }

    private static PendingIntent makeDisplayPendingIntent(Context context) {
        Intent statsActivityIntent = new Intent(context, RunStatsActivity.class);
        return PendingIntent.getActivity(context, 0,
                statsActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static void hideNotification(Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
