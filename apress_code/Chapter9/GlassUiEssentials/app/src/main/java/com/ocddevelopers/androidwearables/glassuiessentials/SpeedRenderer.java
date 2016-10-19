package com.ocddevelopers.androidwearables.glassuiessentials;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.DirectRenderingCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Draws and updates the speedometer created by SpeedService.
 */
public class SpeedRenderer implements DirectRenderingCallback {
    private static final long FRAME_TIME_MILLIS = 33;
    private static final float ANGLE_ZERO_MPH = -135f;
    private static final float DEGREES_PER_MPH = 9f/4f;
    private SurfaceHolder mHolder;
    private Bitmap mBackground, mNeedle;
    private Paint mBitmapPaint;
    private RenderThread mRenderThread;
    private boolean mPaused;


    public SpeedRenderer(Context context) {
        mBackground = ((BitmapDrawable)context.getResources()
                .getDrawable(R.drawable.bg_speedometer)).getBitmap();
        mNeedle = ((BitmapDrawable)context.getResources()
                .getDrawable(R.drawable.needle)).getBitmap();
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setFilterBitmap(true);
    }

    @Override
    public void renderingPaused(SurfaceHolder surfaceHolder, boolean paused) {
        mPaused = paused;
        updateRendering();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        updateRendering();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRendering();
    }

    private void updateRendering() {
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;

        if (shouldRender != rendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    private void draw(float speedMph) {
        Canvas canvas = mHolder.lockCanvas();

        if (canvas != null) {
            // draw background
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(mBackground, 0, 0, mBitmapPaint);

            // calculate angle
            float angle = ANGLE_ZERO_MPH + DEGREES_PER_MPH * speedMph;

            // draw needle
            canvas.rotate(angle, 320, 196);
            canvas.drawBitmap(mNeedle, 315, 109, mBitmapPaint);

            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private class RenderThread extends Thread {
        private AtomicBoolean mKeepRunning;
        private float mSpeed, mDeltaSpeed;

        public RenderThread() {
            mKeepRunning = new AtomicBoolean();
            mSpeed = 0;
            mDeltaSpeed = 0.5f;
        }

        public void quit() {
            mKeepRunning.set(false);
        }

        @Override
        public void run() {
            mKeepRunning.set(true);

            while (mKeepRunning.get()) {
                // change speed values between 0 and 110 mph
                mSpeed += mDeltaSpeed;

                if(mSpeed >  110f) {
                    mSpeed = 110;
                    mDeltaSpeed = -mDeltaSpeed;
                }

                if(mSpeed < 0) {
                    mSpeed = 0;
                    mDeltaSpeed = -mDeltaSpeed;
                }

                draw(mSpeed);
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }

}
