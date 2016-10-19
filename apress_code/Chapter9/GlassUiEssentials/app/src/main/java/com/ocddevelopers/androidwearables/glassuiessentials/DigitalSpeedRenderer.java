package com.ocddevelopers.androidwearables.glassuiessentials;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Draws and updates the digital speedometer created by DigitalSpeedService.
 */
public class DigitalSpeedRenderer implements DirectRenderingCallback {
    private static final long FRAME_TIME_MILLIS = 33;
    private SurfaceHolder mHolder;
    private boolean mPaused, mUseMetric;
    private RenderThread mRenderThread;
    private DigitalSpeedView mDigitalSpeedView;

    public DigitalSpeedRenderer(Context context) {
        mDigitalSpeedView = new DigitalSpeedView(context);
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
        // measure and layout the view to find its dimensions
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
        mDigitalSpeedView.measure(measuredWidth, measuredHeight);
        mDigitalSpeedView.layout(0, 0, mDigitalSpeedView.getMeasuredWidth(), mDigitalSpeedView.getMeasuredHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = null;
        updateRendering();
    }

    public void setUseMetric(boolean useMetric) {
        mRenderThread.setUseMetric(useMetric);
        mUseMetric = useMetric;
    }

    private void updateRendering() {
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;

        if (shouldRender != rendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.setUseMetric(mUseMetric);
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }

    private void draw() {
        Canvas canvas = mHolder.lockCanvas();

        if (canvas != null) {
            canvas.drawColor(Color.BLACK);
            mDigitalSpeedView.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    private class RenderThread extends Thread {
        private AtomicBoolean mKeepRunning, mUseMetric;
        private float mSpeed, mDeltaSpeed;

        public RenderThread() {
            mKeepRunning = new AtomicBoolean();
            mUseMetric = new AtomicBoolean();
            mSpeed = 0;
            mDeltaSpeed = 0.5f;
        }

        public void quit() {
            mKeepRunning.set(false);
        }

        public void setUseMetric(boolean useMetric) {
            mUseMetric.set(useMetric);
        }

        @Override
        public void run() {
            mKeepRunning.set(true);

            while (mKeepRunning.get()) {
                mSpeed += mDeltaSpeed;

                if(mSpeed >  110f) {
                    mSpeed = 110;
                    mDeltaSpeed = -mDeltaSpeed;
                }

                if(mSpeed < 0) {
                    mSpeed = 0;
                    mDeltaSpeed = -mDeltaSpeed;
                }

                mDigitalSpeedView.setUseMetric(mUseMetric.get());
                mDigitalSpeedView.setSpeedMph(mSpeed);
                draw();
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }


}
