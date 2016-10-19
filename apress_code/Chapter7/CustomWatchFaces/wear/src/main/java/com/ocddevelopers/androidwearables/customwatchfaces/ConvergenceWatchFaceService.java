package com.ocddevelopers.androidwearables.customwatchfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.Wearable;

import java.util.TimeZone;

/**
 * Implements a watch face called convergence. The watch face's background and clock hands are
 * images, and the watch face includes both a wearable and a handheld configuration activity.
 */
public class ConvergenceWatchFaceService extends CanvasWatchFaceService {
    @Override
    public CanvasWatchFaceService.Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        private static final int MSG_UPDATE_WATCH_FACE = 0;
        private static final int INTERACTIVE_UPDATE_RATE_MS = 1000;
        private static final int DEGREES_PER_SECOND = 6;
        private static final int DEGREES_PER_MINUTE = 6;
        private static final int DEGREES_PER_HOUR = 30;
        private static final int MINUTES_PER_HOUR = 60;
        private static final int REF_SIZE = 320;
        private Time mTime;
        private Paint mSecondPaint, mBlackFillPaint, mWhiteFillPaint;
        private Paint mBitmapPaint, mDatePaint, mTimePaint;
        private boolean mLowBitAmbient, mBurnInProtection, mAmbient;
        private boolean mRegisteredTimeZoneReceiver;
        private boolean mRound;
        private float mSecondHandLength;
        private Rect mPeekCardRect;
        private WatchFaceBitmapHolder mRoundBackgroundBitmapHolder, mSquareBackgroundBitmapHolder;
        private WatchFaceBitmapHolder mHourHandBitmapHolder, mMinuteHandBitmapHolder;
        private GoogleApiClient mGoogleApiClient;
        private boolean mContinuousSweep;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mBitmapPaint = new Paint();

            mSecondPaint = new Paint();
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondPaint.setColor(Color.parseColor("#ef464c"));
            mSecondPaint.setStrokeWidth(2f);

            mBlackFillPaint = new Paint();
            mBlackFillPaint.setColor(Color.BLACK);

            mWhiteFillPaint = new Paint();
            mWhiteFillPaint.setColor(Color.WHITE);

            mDatePaint = new Paint();
            mDatePaint.setTextAlign(Paint.Align.CENTER);
            mDatePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            mDatePaint.setColor(Color.parseColor("#d7dada"));
            mDatePaint.setTextSize(18f);
            mDatePaint.setShadowLayer(1f, 4f, 4f, Color.BLACK);

            mTimePaint = new Paint();
            mTimePaint.setTextAlign(Paint.Align.CENTER);
            mTimePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            mTimePaint.setColor(Color.parseColor("#d7dada"));
            mTimePaint.setTextSize(26f);
            mTimePaint.setShadowLayer(1f, 2f, 2f, Color.BLACK);

            mRoundBackgroundBitmapHolder = new WatchFaceBitmapHolder(ConvergenceWatchFaceService.this,
                    R.drawable.cv_background_interactive_round,
                    R.drawable.cv_background_ambient_round,
                    R.drawable.cv_background_lowbit_round,
                    R.drawable.cv_background_burnin_round,
                    R.drawable.cv_background_lowbitburnin_round);

            mSquareBackgroundBitmapHolder = new WatchFaceBitmapHolder(ConvergenceWatchFaceService.this,
                    R.drawable.cv_background_interactive_square,
                    R.drawable.cv_background_ambient_square,
                    R.drawable.cv_background_lowbit_square,
                    R.drawable.cv_background_burnin_square,
                    R.drawable.cv_background_lowbitburnin_square);

            mHourHandBitmapHolder = new WatchFaceBitmapHolder(ConvergenceWatchFaceService.this,
                    R.drawable.cv_hour_hand_filled,
                    R.drawable.cv_hour_hand_filled,
                    R.drawable.cv_hour_hand_lowbit,
                    R.drawable.cv_hour_hand_burnin,
                    R.drawable.cv_hour_hand_lowbitburnin);

            mMinuteHandBitmapHolder = new WatchFaceBitmapHolder(ConvergenceWatchFaceService.this,
                    R.drawable.cv_minute_hand_filled,
                    R.drawable.cv_minute_hand_filled,
                    R.drawable.cv_minute_hand_lowbit,
                    R.drawable.cv_minute_hand_burnin,
                    R.drawable.cv_minute_hand_lowbitburnin);

            setAntiAliasing(true);
            mTime = new Time();

            setWatchFaceStyle(new WatchFaceStyle.Builder(ConvergenceWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .build());

            mGoogleApiClient = new GoogleApiClient.Builder(ConvergenceWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .build();
        }

        private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
                new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                initializeContinuousSweep();
                Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        };

        private void initializeContinuousSweep() {
            ConvergenceUtil.fetchContinuousSweep(mGoogleApiClient,
                    new ConvergenceUtil.FetchContinuousSweepCallback() {
                        @Override
                        public void onContinuousSweepFetched(final boolean continuousSweep) {
                            updateContinuousSweep(continuousSweep);
                        }
                    });
        }

        private void updateContinuousSweep(final boolean continuousSweep) {
            mContinuousSweep = continuousSweep;
            updateTimer();
            invalidate();
        }

        private DataApi.DataListener mDataListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                updateContinuousSweep(ConvergenceUtil.extractContinuousSweep(dataEvents));
            }
        };

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_WATCH_FACE);
            super.onDestroy();
        }

        // called when the properties of the device are determined
        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);

            mLowBitAmbient= true;
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mPeekCardRect = rect;
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            mAmbient = inAmbientMode;
            if(mLowBitAmbient) {
                setAntiAliasing(!mAmbient);
            }

            invalidate();

            updateTimer();
        }

        private void setAntiAliasing(boolean antiAliasing) {
            mBitmapPaint.setAntiAlias(antiAliasing);
            mBitmapPaint.setFilterBitmap(antiAliasing);
            mSecondPaint.setAntiAlias(antiAliasing);
            mWhiteFillPaint.setAntiAlias(antiAliasing);
            mDatePaint.setAntiAlias(antiAliasing);
            mTimePaint.setAntiAlias(antiAliasing);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if(visible) {
                registerTimeZoneChangedReceiver();

                // update time zone in case it changed while watch was not visible
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();

                mGoogleApiClient.connect();
            } else {
                unregisterTimeZoneChangedReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
                    mGoogleApiClient.disconnect();
                }
            }

            updateTimer();
        }

        private void registerTimeZoneChangedReceiver() {
            if(mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            ConvergenceWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterTimeZoneChangedReceiver() {
            if(!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            ConvergenceWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_WATCH_FACE);
            if(shouldContinueUpdatingTime() && !mContinuousSweep) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_WATCH_FACE);
            }
        }

        private boolean shouldContinueUpdatingTime() {
            return isVisible() && !isInAmbientMode();
        }

        private Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_UPDATE_WATCH_FACE:
                        invalidate();

                        if(shouldContinueUpdatingTime()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS -
                                    timeMs%INTERACTIVE_UPDATE_RATE_MS;

                            // delayMs is the delay up until the next even second (that is,
                            // the delay until the next multiple of 1000 milliseconds)
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_WATCH_FACE,
                                    delayMs);
                        }
                        break;
                }
            }
        };

        private BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mRound = insets.isRound();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            long now = System.currentTimeMillis();
            mTime.set(now);
            int milliseconds = 0;
            if(mContinuousSweep) {
                milliseconds = (int) (now % 1000);
            }

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Compute rotations and lengths for the clock hands.
            float decimalSeconds = mTime.second + milliseconds/1000f;
            float secRotRad = (float)Math.toRadians(decimalSeconds * DEGREES_PER_SECOND);
            float minutes = mTime.minute;
            float minRot = minutes * DEGREES_PER_MINUTE;
            float hours = mTime.hour + minutes / MINUTES_PER_HOUR;
            float hrRot = hours * DEGREES_PER_HOUR;

            mRoundBackgroundBitmapHolder.prepareBitmaps(width, height,
                    mLowBitAmbient, mBurnInProtection);
            mSquareBackgroundBitmapHolder.prepareBitmaps(width, height,
                    mLowBitAmbient, mBurnInProtection);
            mHourHandBitmapHolder.prepareBitmaps(width, height,
                    mLowBitAmbient, mBurnInProtection);
            mMinuteHandBitmapHolder.prepareBitmaps(width, height,
                    mLowBitAmbient, mBurnInProtection);

            WatchFaceBitmapHolder.WatchState state = (mAmbient)? WatchFaceBitmapHolder.WatchState.AMBIENT :
                    WatchFaceBitmapHolder.WatchState.INTERACTIVE;
            mRoundBackgroundBitmapHolder.setWatchState(state);
            mSquareBackgroundBitmapHolder.setWatchState(state);
            mHourHandBitmapHolder.setWatchState(state);
            mMinuteHandBitmapHolder.setWatchState(state);

            // draw background
            Bitmap background = (mRound)? mRoundBackgroundBitmapHolder.getBitmap() :
                    mSquareBackgroundBitmapHolder.getBitmap();
            canvas.drawBitmap(background, 0f, 0f, mBitmapPaint);

            // draw hour hand
            Bitmap hourHand = mHourHandBitmapHolder.getBitmap();
            canvas.save();
            canvas.rotate(hrRot, centerX, centerY);
            canvas.drawBitmap(hourHand, centerX-hourHand.getWidth()/2f,
                    centerY-hourHand.getHeight(), mBitmapPaint);
            canvas.restore();

            // draw minute hand
            Bitmap minuteHand = mMinuteHandBitmapHolder.getBitmap();
            canvas.save();
            canvas.rotate(minRot, centerX, centerY);
            canvas.drawBitmap(minuteHand, centerX-minuteHand.getWidth()/2f,
                    centerY-minuteHand.getHeight(), mBitmapPaint);
            canvas.restore();

            // draw second hand
            if (mAmbient) {
                // draw center of watch hands
                if(mBurnInProtection) {
                    canvas.drawCircle(centerX, centerY, 6f, mBlackFillPaint);
                }
                canvas.drawCircle(centerX, centerY, 6f, mWhiteFillPaint);

                // draw background for peek card
                if(mPeekCardRect != null) {
                    canvas.drawRect(mPeekCardRect, mBlackFillPaint);
                }
            } else {
                // Only draw the second hand in interactive mode.
                mSecondHandLength = 100f / 320 * width;
                float secX = (float) Math.sin(secRotRad) * mSecondHandLength;
                float secY = (float) -Math.cos(secRotRad) * mSecondHandLength;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY +
                        secY, mSecondPaint);

                // draw center of watch hands
                canvas.drawCircle(centerX, centerY, 6f, mSecondPaint);
            }

            if(mContinuousSweep && isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }
    }
}

