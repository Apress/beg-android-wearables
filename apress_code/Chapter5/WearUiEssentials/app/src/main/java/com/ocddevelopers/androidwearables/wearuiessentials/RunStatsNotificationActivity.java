package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Activity;
import android.os.Bundle;

/**
 * This class displays the run stats notification and terminates immediately. This lets us
 * create the notification in response to an app-provided action.
 */
public class RunStatsNotificationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RunStatsNotificationUtil.showNotification(this);

        finish();
    }

}
