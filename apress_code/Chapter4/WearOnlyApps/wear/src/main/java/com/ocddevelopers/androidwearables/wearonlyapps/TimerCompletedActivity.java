package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateUtils;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Indicates that the timer has gone off by playing a short vibration every second until the user
 * dismisses it.
 */
public class TimerCompletedActivity extends Activity {
    public static final String EXTRA_START_TIME = "start_time";
    private static final long VIBRATION_DURATION = 200L;
    private static final int DELAY_MILLIS = 1000;
    private static final int MILLIS_PER_SECOND = 1000;
    private static final int MAX_DURATION_SECONDS = (int)TimeUnit.MINUTES.toSeconds(1);
    private TextView mTimeSinceCompleted;
    private Handler mHandler;
    private Vibrator mVibrator;
    private long mTimerCompletionTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_completed);

        // cancel timer notification
        NotificationManagerCompat.from(this).cancel(1);

        mTimeSinceCompleted = (TextView)findViewById(R.id.time_since_completed);
        mHandler = new Handler();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // start playing alarm
        mTimerCompletionTime = getIntent().getLongExtra(EXTRA_START_TIME, -1);
        if(mTimerCompletionTime >= 0) {
            mRunnable.run();
        } else {
            throw new RuntimeException("TimerCompletedActivity requires the timer's start time extra");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CommandReceiver.completeWakefulIntent();
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        super.onPause();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int deltaSeconds = (int)(System.currentTimeMillis() - mTimerCompletionTime)/MILLIS_PER_SECOND;
            String delta = DateUtils.formatElapsedTime(deltaSeconds);
            mTimeSinceCompleted.setText("-" + delta);
            mVibrator.vibrate(VIBRATION_DURATION);

            // buzz alarm once a second for no more than 1 minute
            if(deltaSeconds < MAX_DURATION_SECONDS) {
                mHandler.postDelayed(mRunnable, DELAY_MILLIS);
            }
        }
    };

}
