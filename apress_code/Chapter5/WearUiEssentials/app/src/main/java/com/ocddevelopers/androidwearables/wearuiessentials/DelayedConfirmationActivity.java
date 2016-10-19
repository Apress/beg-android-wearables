package com.ocddevelopers.androidwearables.wearuiessentials;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.view.View;

/**
 * Demonstrates the use of DelayedConfirmationView.
 */
public class DelayedConfirmationActivity extends Activity {
    private DelayedConfirmationView mDelayedConfirmationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delayed_confirmation);

        mDelayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        mDelayedConfirmationView.setTotalTimeMs(5000);

        mDelayedConfirmationView.setListener(mDelayedConfirmationListener);
        mDelayedConfirmationView.start();
    }

    // once the delayed confirmation animation is complete, display a ConfirmationActivity. If the
    // user taps on the DelayedConfirmationView at any time, display the ConfirmationActivity
    // immediately.
    private DelayedConfirmationView.DelayedConfirmationListener mDelayedConfirmationListener =
            new DelayedConfirmationView.DelayedConfirmationListener() {
        @Override
        public void onTimerFinished(View view) {
            startConfirmationActivity(ConfirmationActivity.SUCCESS_ANIMATION,
                    "Alarm set for 08:30");
            finishActivity();
        }

        @Override
        public void onTimerSelected(View view) {
            startConfirmationActivity(ConfirmationActivity.SUCCESS_ANIMATION,
                    "Alarm set for 08:30");
            finishActivity();
        }
    };

    private void startConfirmationActivity(int animationType, String message) {
        Intent confirmationActivity = new Intent(this, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        startActivity(confirmationActivity);
    }

    private void finishActivity() {
        // stop the delayed confirmation by settings its total time to zero, at least until
        // a better API is available.
        mDelayedConfirmationView.setTotalTimeMs(0);
        finish();
    }

}
