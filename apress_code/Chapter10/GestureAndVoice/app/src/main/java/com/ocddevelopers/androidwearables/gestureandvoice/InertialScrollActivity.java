package com.ocddevelopers.androidwearables.gestureandvoice;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.Locale;

/**
 * Demonstrates how to use inertial scroll to let a user select a number.
 * adapted from https://github.com/googleglass/gdk-timer-sample
 */
public class InertialScrollActivity extends Activity {
    private static float FLING_VELOCITY_CUTOFF = 3f;
    private static float DECELERATION_CONSTANT = 0.2f;
    private static float COUNT_DAMPENER = 100f;
    private static float TIME_LENGTHENING = 12f;
    private float mReleaseVelocity;
    private ValueAnimator mInertialScrollAnimator;
    private GestureDetector mGestureDetector;
    private TextView mDisplacement, mDelta, mVelocity, mCountText;
    private float mCount, mPrevCount;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_scroll);
        mDisplacement = (TextView) findViewById(R.id.displacement);
        mDelta = (TextView) findViewById(R.id.delta);
        mVelocity = (TextView) findViewById(R.id.velocity);
        mCountText = (TextView) findViewById(R.id.count);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setScrollListener(mScrollListener);
        mGestureDetector.setFingerListener(mFingerListener);

        mCount = mPrevCount = 0;
        updateCount();

        // implement inertial score for the counter
        mInertialScrollAnimator = new ValueAnimator();
        mInertialScrollAnimator.setInterpolator(new DecelerateInterpolator());
        mInertialScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCount = (Float) animation.getAnimatedValue();
                updateCount();
            }
        });
    }

    @Override
    protected void onPause() {
        mInertialScrollAnimator.cancel();
        super.onPause();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return super.onGenericMotionEvent(event) || mGestureDetector.onMotionEvent(event);
    }

    private GestureDetector.ScrollListener mScrollListener = new GestureDetector.ScrollListener() {
        @Override
        public boolean onScroll(float displacement, float delta, float velocity) {
            mReleaseVelocity = velocity;

            updateText(displacement, delta, velocity);
            mCount += delta*Math.min(1, Math.abs(velocity)) / COUNT_DAMPENER;
            updateCount();

            return true;
        }
    };

    private void updateText(float displacement, float delta, float velocity) {
        mDisplacement.setText(String.format(Locale.US, "%.2f", displacement));
        mDelta.setText(String.format(Locale.US, "%.2f", delta));
        mVelocity.setText(String.format(Locale.US, "%.2f", velocity));
    }

    private void updateCount() {
        mCountText.setText("" + (int)mCount);

        if((int)mPrevCount != (int)mCount) {
            mAudioManager.playSoundEffect(Sounds.TAP);
            mPrevCount = mCount;
        }
    }

    private GestureDetector.FingerListener mFingerListener = new GestureDetector.FingerListener() {
        @Override
        public void onFingerCountChanged(int previousCount, int currentCount) {
            boolean wentDown = currentCount > previousCount;

            // when the user releases the last finger
            if (currentCount == 0 && !wentDown) {
                // Only start inertial scroll if the velocity is greater than the cutoff
                if (Math.abs(mReleaseVelocity) > FLING_VELOCITY_CUTOFF) {
                    float deceleration = Math.signum(mReleaseVelocity) * -DECELERATION_CONSTANT;
                    float flingTime = -mReleaseVelocity / deceleration * TIME_LENGTHENING;
                    float totalDelta = mReleaseVelocity * mReleaseVelocity / 2f / -deceleration;

                    mInertialScrollAnimator.cancel();
                    mInertialScrollAnimator.setFloatValues(mCount, mCount + totalDelta/100);
                    mInertialScrollAnimator.setDuration((long) flingTime);
                    mInertialScrollAnimator.start();
                }
            } else {
                mInertialScrollAnimator.cancel();
            }
        }
    };

}
