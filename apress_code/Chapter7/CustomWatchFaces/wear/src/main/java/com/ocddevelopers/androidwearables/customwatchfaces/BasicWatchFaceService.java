package com.ocddevelopers.androidwearables.customwatchfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.TimeZone;

/**
 * Implements a basic watch face. Draws the watch face based on primitive shapes.
 */
public class BasicWatchFaceService extends CanvasWatchFaceService {

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
        private Paint mHourPaint, mMinutePaint, mSecondPaint;
        private Paint mMajorTickPaint, mMinorTickPaint, mBlackFillPaint, mWhiteFillPaint;
        private boolean mLowBitAmbient, mBurnInProtection, mAmbient;
        private boolean mRegisteredTimeZoneReceiver;
        private boolean mRound;
        private float mHourHandLength, mMinuteHandLength, mSecondHandLength;
        private float mMajorTickLength, mMinorTickLength, mMajorTickWidth;
        private float mClockHandCornerRadius;
        private int mPrevSize = -1;
        private Rect mPeekCardRect;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mHourPaint = new Paint();
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);

            mMinutePaint = new Paint();
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);

            mSecondPaint = new Paint();
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);

            mMajorTickPaint = new Paint();
            mMinorTickPaint = new Paint();

            mBlackFillPaint = new Paint();
            mBlackFillPaint.setColor(Color.BLACK);

            mWhiteFillPaint = new Paint();

            setAntiAliasing(true);

            mTime = new Time();

            setWatchFaceStyle(new WatchFaceStyle.Builder(BasicWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .build());

            updateColors();
        }

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

            if(mBurnInProtection) {
                setBurnInProtection(mAmbient);
            }

            updateColors();
            invalidate();

            updateTimer();
        }

        private void setAntiAliasing(boolean antiAliasing) {
            mHourPaint.setAntiAlias(antiAliasing);
            mMinutePaint.setAntiAlias(antiAliasing);
            mSecondPaint.setAntiAlias(antiAliasing);
            mMajorTickPaint.setAntiAlias(antiAliasing);
            mMinorTickPaint.setAntiAlias(antiAliasing);
            mBlackFillPaint.setAntiAlias(antiAliasing);
            mWhiteFillPaint.setAntiAlias(antiAliasing);

        }

        private void setBurnInProtection(boolean enabled) {
            Paint.Style paintStyle = Paint.Style.FILL;
            if(enabled) {
                paintStyle = Paint.Style.STROKE;
            }

            mHourPaint.setStyle(paintStyle);
            mMinutePaint.setStyle(paintStyle);
            mWhiteFillPaint.setStyle(paintStyle);

            if(enabled) {
                mMajorTickPaint.setStrokeWidth(1f);
            } else {
                mMajorTickPaint.setStrokeWidth(mMajorTickWidth);
            }
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
            } else {
                unregisterTimeZoneChangedReceiver();
            }

            updateTimer();
        }

        private void registerTimeZoneChangedReceiver() {
            if(mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            BasicWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterTimeZoneChangedReceiver() {
            if(!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            BasicWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_WATCH_FACE);
            if(shouldContinueUpdatingTime()) {
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

            mTime.setToNow();
            mTime.set(3, 18, 4, mTime.monthDay, mTime.month, mTime.year);

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches
            // with a "chin", the watch face is centered on the entire screen, not
            // just the usable portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Compute rotations and lengths for the clock hands.
            float secRot = (float)Math.toRadians(mTime.second * DEGREES_PER_SECOND);
            float minutes = mTime.minute;
            float minRot = (float)Math.toRadians(minutes * DEGREES_PER_MINUTE);
            float hours = mTime.hour + minutes / MINUTES_PER_HOUR;
            float hrRot = (float)Math.toRadians(hours * DEGREES_PER_HOUR );

            // scale all the dimensions if needed
            initDimensions(Math.min(width, height));

            // draw the black background
            canvas.drawColor(Color.BLACK);

            // draw the minor tick marks
            if(!mAmbient || (!mBurnInProtection && !mLowBitAmbient)) {
                canvas.save();
                for (int i = 0; i < 12; ++i) {
                    if (i % 3 != 0) {
                        // draw a line wider than the screen to ensure it extends to the
                        // diagonals of a square watch face
                        canvas.drawLine(-100, centerY, width + 100, centerY, mMinorTickPaint);
                    }

                    canvas.rotate(30f, centerX, centerY);
                }
                canvas.restore();

                if (mRound) {
                    canvas.drawCircle(centerX, centerY, centerX - 32f, mBlackFillPaint);
                } else {
                    canvas.drawRect(mMinorTickLength, mMinorTickLength, width - mMinorTickLength,
                            height - mMinorTickLength, mBlackFillPaint);
                }
            }

            // draw major tick marks
            canvas.drawLine(centerX, 0, centerX, mMajorTickLength, mMajorTickPaint);
            canvas.drawLine(centerX, height - mMajorTickLength, centerX, height, mMajorTickPaint);
            canvas.drawLine(0, centerY, mMajorTickLength, centerY, mMajorTickPaint);
            canvas.drawLine(width - mMajorTickLength, centerY, width, centerY, mMajorTickPaint);

            // Draw the hour hand
            canvas.save();
            canvas.rotate((float) Math.toDegrees(hrRot), centerX, centerY);
            canvas.drawRoundRect(centerX - 3f, centerY - mHourHandLength, centerX + 3f, centerY,
                    mClockHandCornerRadius, mClockHandCornerRadius, mHourPaint);
            canvas.restore();

            // Draw the minute hand on top of the hour hand
            canvas.save();
            canvas.rotate((float) Math.toDegrees(minRot), centerX, centerY);
            if(mBurnInProtection) {
                canvas.drawRoundRect(centerX - 3f, centerY - mMinuteHandLength, centerX + 3f,
                        centerY, mClockHandCornerRadius, mClockHandCornerRadius, mBlackFillPaint);
            }
            canvas.drawRoundRect(centerX - 3f, centerY - mMinuteHandLength, centerX + 3f, centerY,
                    mClockHandCornerRadius, mClockHandCornerRadius, mMinutePaint);
            canvas.restore();

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
                float secX = (float) Math.sin(secRot) * mSecondHandLength;
                float secY = (float) -Math.cos(secRot) * mSecondHandLength;
                canvas.drawLine(centerX, centerY, centerX + secX, centerY +
                        secY, mSecondPaint);

                // draw center of watch hands
                canvas.drawCircle(centerX, centerY, 6f, mSecondPaint);
            }
        }



        private void updateColors() {
            boolean enableLowBitAmbient = mAmbient && mLowBitAmbient;
            if(enableLowBitAmbient) {
                // ambient mode with low-bit enabled
                mMajorTickPaint.setColor(Color.WHITE);
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mWhiteFillPaint.setColor(Color.WHITE);
            } else if(mAmbient) {
                // ambient mode with low-bit disabled
                mHourPaint.setColor(Color.parseColor("#CCCCCC"));
                mMinutePaint.setColor(Color.parseColor("#CCCCCC"));
                mMajorTickPaint.setColor(Color.parseColor("#666666"));
                mMinorTickPaint.setColor(Color.parseColor("#333333"));
                mWhiteFillPaint.setColor(Color.parseColor("#CCCCCC"));
                mHourPaint.setColor(Color.parseColor("#CCCCCC"));
                mMinutePaint.setColor(Color.parseColor("#CCCCCC"));
            } else {
                // interactive mode
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondPaint.setColor(Color.parseColor("#FF1D25"));
                mMajorTickPaint.setColor(Color.WHITE);
                mMinorTickPaint.setColor(Color.GRAY);
                mWhiteFillPaint.setColor(Color.WHITE);
            }
        }

        // the watch face was designed for a 320x320 screen.
        // scale all the dimensions to fit the size of the new watch.
        // assumption: the watch face is square
        private void initDimensions(int size) {
            if(mPrevSize == size) {
                return;
            }
            mPrevSize = size;

            mHourHandLength = 50f / REF_SIZE * size;
            mMinuteHandLength = 81f / REF_SIZE * size;
            mSecondHandLength = 100f / REF_SIZE * size;

            mMajorTickLength = 45f / REF_SIZE * size;
            mMinorTickLength = 25f / REF_SIZE * size;

            mHourPaint.setStrokeWidth(1f);
            mMinutePaint.setStrokeWidth(1f);
            mSecondPaint.setStrokeWidth(Math.round(2f / REF_SIZE * size));

            mMajorTickWidth = Math.round(4f / REF_SIZE * size);
            mMajorTickPaint.setStrokeWidth(mMajorTickWidth);
            mMinorTickPaint.setStrokeWidth(Math.round(2f / REF_SIZE * size));

            mClockHandCornerRadius = Math.round(11f / REF_SIZE * size);
        }
    }
}
