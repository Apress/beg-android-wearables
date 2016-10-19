package com.ocddevelopers.androidwearables.wearonlyapps;

import android.app.Activity;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.wearable.view.WearableListView;

/**
 * TimerActivity is started with the "Set a timer" system-provided voice action. If the voice action
 * contains a duration (such as "Set a timer for 5 minutes"), then this activity starts the timer
 * and finishes. Otherwise, this activity prompts the user for a duration before scheduling
 * the timer.
 */
public class TimerActivity extends Activity {
    private static final int[] TIMER_PRESETS_DURATION_SECS = { 5, 10, 20, 30, 40, 50, 60, 120, 180,
            240, 300, 360, 420, 480, 540, 600, 900, 1800, 2700, 3600, 5400 };
    private static final String[] TIMER_PRESETS_LABEL = new String[] {
            "5 seconds", "10 seconds", "20 seconds", "30 seconds", "40 seconds", "50 seconds",
            "1 minute", "2 minutes", "3 minutes", "4 minutes", "5 minutes",
            "6 minutes", "7 minutes", "8 minutes", "9 minutes", "10 minutes",
            "15 minutes", "30 minutes", "45 minutes", "1 hour", "1.5 hours"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlistview);

        int timerDuration = getIntent().getIntExtra(AlarmClock.EXTRA_LENGTH, -1);
        if(timerDuration > 0) {
            TimerUtil.createNewTimer(this, timerDuration*1000L);
            finish();
        } else {
            // of EXTRA_LENGTH is not available, then we must prompt the user for a timer duration
            WearableListView wearableListView = (WearableListView)findViewById(R.id.list);
            WearableAdapter timerDurationAdapter = new WearableAdapter(this, TIMER_PRESETS_LABEL);
            wearableListView.setAdapter(timerDurationAdapter);
            wearableListView.setClickListener(mClickListener);
        }
    }

    private WearableListView.ClickListener mClickListener = new WearableListView.ClickListener() {
        @Override
        public void onClick(WearableListView.ViewHolder viewHolder) {
            // once the user picks a duration, create timer alarm/notification and exit
            int position = viewHolder.getPosition();
            int duration = TIMER_PRESETS_DURATION_SECS[position];
            TimerUtil.createNewTimer(TimerActivity.this, duration*1000L);
            finish();
        }

        @Override
        public void onTopEmptyRegionClick() {

        }
    };

}
