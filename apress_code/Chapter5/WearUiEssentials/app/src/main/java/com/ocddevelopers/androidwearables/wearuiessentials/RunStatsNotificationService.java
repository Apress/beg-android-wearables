package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * This class receives and processes commands to stop the run
 */
public class RunStatsNotificationService extends IntentService {
    private static final String ACTION_STOP_RUN =
            "com.ocddevelopers.androidwearables.wearuiessentials.action.STOP_RUN";

    public static Intent makeStopIntent(Context context) {
        Intent intent = new Intent(context, RunStatsNotificationService.class);
        intent.setAction(ACTION_STOP_RUN);
        return intent;
    }

    public RunStatsNotificationService() {
        super("RunStatsNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_STOP_RUN.equals(action)) {
                RunStatsNotificationUtil.hideNotification(this);
            }
        }
    }

}
