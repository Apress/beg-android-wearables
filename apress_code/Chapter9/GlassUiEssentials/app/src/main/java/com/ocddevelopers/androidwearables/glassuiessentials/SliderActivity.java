package com.ocddevelopers.androidwearables.glassuiessentials;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.Slider;

/**
 * Demonstrates how to use different types of sliders.
 */
public final class SliderActivity extends Activity {
    private static final int NUM_SCROLL_POSITIONS = 5;
    private Handler mHandler;
    private Slider mSlider;
    private Slider.Indeterminate mIndeterminate;
    private Slider.GracePeriod mGracePeriod;
    private int mPosition;
    private Slider.Scroller mScroller;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        View contentView = new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("Tap to demonstrate how to use Slider")
                .getView();
        setContentView(contentView);
        mSlider = Slider.from(contentView);
        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.slider, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mAudioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.demo_indeterminate:
                demoIndeterminateSlider();
                return true;
            case R.id.demo_determinate:
                demoDeterminateSlider();
                return true;
            case R.id.demo_graceperiod:
                demoGracePeriodSlider();
                return true;
            case R.id.demo_scroller:
                demoScrollerSlider();
                return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void demoIndeterminateSlider() {
        if (mIndeterminate == null) {
            mIndeterminate = mSlider.startIndeterminate();
            mHandler.postDelayed(mIndeterminateRunnable, 2500);
        }
    }

    private Runnable mIndeterminateRunnable = new Runnable() {
        @Override
        public void run() {
            mIndeterminate.hide();
            mIndeterminate = null;
        }
    };

    private void demoDeterminateSlider() {
        final Slider.Determinate determinate = mSlider.startDeterminate(100, 0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(determinate, "position", 0, 100);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                determinate.hide();
            }
        });

        animator.setDuration(4000)
                .start();
    }

    private void demoGracePeriodSlider() {
        mGracePeriod = mSlider.startGracePeriod(mGracePeriodListener);
    }

    private final Slider.GracePeriod.Listener mGracePeriodListener =
            new Slider.GracePeriod.Listener() {
        @Override
        public void onGracePeriodEnd() {
            mAudioManager.playSoundEffect(Sounds.SUCCESS);
            mGracePeriod = null;
        }
        @Override
        public void onGracePeriodCancel() {
            mAudioManager.playSoundEffect(Sounds.DISMISSED);
            mGracePeriod = null;
        }
    };

    @Override
    public void onBackPressed() {
        if (mGracePeriod != null) {
            mGracePeriod.cancel();
        } else {
            super.onBackPressed();
        }
    }

    private void demoScrollerSlider() {
        if(mScroller == null) {
            mPosition = 0;
            mScroller = mSlider.startScroller(NUM_SCROLL_POSITIONS-1, mPosition);
            mScrollerRunnable.run();
        }
    }

    private Runnable mScrollerRunnable = new Runnable() {
        @Override
        public void run() {
            mScroller.setPosition(mPosition);
            ++mPosition;

            if(mPosition < NUM_SCROLL_POSITIONS) {
                mHandler.postDelayed(mScrollerRunnable, 800);
            } else {
                mScroller = null;
            }
        }
    };

}
