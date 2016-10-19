package com.ocddevelopers.androidwearables.wearonlyapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * When a user presses an action button on the timer notification, it triggers a broadcast
 * that is handled by CommandReceiver. This class looks at the broadcast's action to determine
 * what button was pressed and takes an appropriate action.
 */
public class CommandReceiver extends BroadcastReceiver {
    private static final String ACTION_TRIGGER_ALARM =
            "com.ocddevelopers.androidwearables.wearonlyapps.action.TRIGGER_ALARM";
    private static final String ACTION_PAUSE_TIMER =
            "com.ocddevelopers.androidwearables.wearonlyapps.action.PAUSE_TIMER";
    private static final String ACTION_STOP_TIMER =
            "com.ocddevelopers.androidwearables.wearonlyapps.action.STOP_TIMER";
    private static final String EXTRA_START_TIME = "start_time";

    private static PowerManager.WakeLock mWakeLock;

    public static Intent makeTriggerAlarmIntent(long startTime) {
        Intent intent = new Intent(ACTION_TRIGGER_ALARM);
        intent.putExtra(EXTRA_START_TIME, startTime);
        return intent;
    }

    public static Intent makePauseTimerIntent() {
        return new Intent(ACTION_PAUSE_TIMER);
    }

    public static Intent makeStopTimerIntent() {
        return new Intent(ACTION_STOP_TIMER);
    }

    /**
     * AlarmManager guarantees that a BroadcastReceiver's onReceive will be called in its entirety
     * before the system goes back to sleep. This guarantee implies that, if starting an Activity
     * inside onReceive, context.startActivity will be called correctly but the system may go back
     * to sleep before the Activity actually starts. startWakefulActivity solves this issue by
     * acquiring a WakeLock that lasts until the Activity is started.
     * This method assumes only one Activity will be started wakefully at a time.
     */
    public static void startWakefulActivity(Context context, Intent intent) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, "wakeful activity lock");
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(60 * 1000);

        context.startActivity(intent);
    }

    /**
     * An Activity that is started with startWakefulActivity should call
     * CommandReceiver.completeWakefulIntent in its onResume callback.
     */
    public static void completeWakefulIntent() {
        if(mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(ACTION_TRIGGER_ALARM.equals(action)) {
            Intent completedIntent = new Intent(context, TimerCompletedActivity.class);
            completedIntent.putExtra(TimerCompletedActivity.EXTRA_START_TIME,
                    intent.getLongExtra(EXTRA_START_TIME, 0L));
            completedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            CommandReceiver.startWakefulActivity(context, completedIntent);
        } else if(ACTION_PAUSE_TIMER.equals(action)) {
            TimerUtil.pauseTimer(context);
        } else if(ACTION_STOP_TIMER.equals(action)) {
            TimerUtil.stopTimer(context);
        }
    }
}
