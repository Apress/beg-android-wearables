package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Locale;

/**
 * Contains helper methods that 1) create/cancel a timer notification or 2) schedule or remove
 * an alarm that is triggered when the timer goes off.
 */
public class TimerUtil {
    private static final int NOTIFICATION_ID = 1;
    public static final String PREFS_PREV_START_TIME = "prev_start_time";
    public static final String PREFS_TIMER_DURATION = "timer_duration";
    public static final String PREFS_IS_PAUSED = "is_paused";
    public static final String PREFS_TIME_ELAPSED = "time_elapsed";

    public static void createNewTimer(Context context, long duration) {
        long startTime = System.currentTimeMillis();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(PREFS_PREV_START_TIME, SystemClock.elapsedRealtime());
        editor.putLong(PREFS_TIMER_DURATION, duration);
        editor.putBoolean(PREFS_IS_PAUSED, false);
        editor.putLong(PREFS_TIME_ELAPSED, 0L);
        editor.commit();

        setTimerAlarm(context, startTime+duration);
        createTimerNotification(context, startTime+duration, false);
    }

    private static void setTimerAlarm(Context context, long alarmTime) {
        Intent completedIntent = CommandReceiver.makeTriggerAlarmIntent(alarmTime);
        PendingIntent completedPendingIntent = PendingIntent.getBroadcast(context, 0,
                completedIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, completedPendingIntent);
    }

    /**
     * Creates a notification that shows the state of the timer, including time remaining.
     * It contains actions to let the user pause/resume and stop the timer.
     */
    private static void createTimerNotification(Context context, long when, boolean isPaused) {
        Intent pauseIntent = CommandReceiver.makePauseTimerIntent();
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0,
                pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent stopIntent = CommandReceiver.makeStopTimerIntent();
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0,
                stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap background;
        int pauseResumeIcon;
        String pauseResumeTitle;

        // if paused, show resume action. otherwise, show pause action
        if(isPaused) {
            pauseResumeIcon = R.drawable.ic_stopwatch_play;
            pauseResumeTitle = "Resume";
            background = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.bg_timer_stopped);
        } else {
            pauseResumeIcon = R.drawable.ic_stopwatch_pause;
            pauseResumeTitle = "Pause";
            background = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.bg_timer_running);
        }

        // create notification
        NotificationCompat.WearableExtender wearableExtender =
                    new NotificationCompat.WearableExtender()
                .setBackground(background)
                .setContentAction(0)
                .setHintHideIcon(true);

        NotificationCompat.Builder timerNotificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .addAction(pauseResumeIcon, pauseResumeTitle, pausePendingIntent)
                .addAction(R.drawable.ic_full_cancel, "Delete", stopPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_ALARM)
                //.setVibrate(FORCE_TOP_PATTERN)
                .extend(wearableExtender);

        // if paused, set content title and content text. otherwise, show timer countdown
        if(isPaused) {
            int timerRemaining = (int)(when - System.currentTimeMillis())/1000;
            String time = String.format(Locale.US, "%02d:%02d", timerRemaining / 60,
                    timerRemaining % 60);
            timerNotificationBuilder.setContentTitle(time).setContentText("");
        } else {
            timerNotificationBuilder.setUsesChronometer(true).setWhen(when);
        }

        // issue notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, timerNotificationBuilder.build());
    }

    public static void stopTimer(Context context) {
        removeTimerAlarm(context);
        removeTimerNotification(context);
    }

    private static void removeTimerAlarm(Context context) {
        Intent alarmCompletedIntent = CommandReceiver.makeTriggerAlarmIntent(0L);
        PendingIntent alarmCompletedPendingIntent = PendingIntent.getBroadcast(context, 0,
                alarmCompletedIntent, PendingIntent.FLAG_NO_CREATE);

        if(alarmCompletedPendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(alarmCompletedPendingIntent);
            alarmCompletedPendingIntent.cancel();
        }
    }

    private static void removeTimerNotification(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    public static void pauseTimer(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long now = SystemClock.elapsedRealtime();
        long prevStartTime = prefs.getLong(PREFS_PREV_START_TIME, 0L);
        long elapsed = prefs.getLong(PREFS_TIME_ELAPSED, 0L);
        long duration = prefs.getLong(PREFS_TIMER_DURATION, 0L);
        boolean isPaused = prefs.getBoolean(PREFS_IS_PAUSED, false);

        if(isPaused) {
            // resume timer
            long timerDueTimeMillis = System.currentTimeMillis() + duration - elapsed;
            setTimerAlarm(context, timerDueTimeMillis);
            createTimerNotification(context, timerDueTimeMillis, false);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(PREFS_PREV_START_TIME, now);
            editor.putBoolean(PREFS_IS_PAUSED, false);
            editor.commit();
        } else {
            // pause timer
            elapsed = elapsed + now - prevStartTime;
            removeTimerAlarm(context);
            createTimerNotification(context, duration-elapsed + System.currentTimeMillis(), true);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(PREFS_TIME_ELAPSED, elapsed);
            editor.putBoolean(PREFS_IS_PAUSED, true);
            editor.commit();
        }
    }

}
