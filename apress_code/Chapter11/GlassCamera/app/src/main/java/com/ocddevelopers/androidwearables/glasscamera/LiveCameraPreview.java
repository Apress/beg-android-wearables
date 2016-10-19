package com.ocddevelopers.androidwearables.glasscamera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.google.android.glass.timeline.LiveCard;

/**
 * Starts the camera preview when needed and sets appropriate preview parameters when the camera
 * is used on a LiveCard.
 */
public class LiveCameraPreview implements DirectRenderingCallback {
    private static final String TAG = "CAM";
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean mPreviewEnabled;

    public LiveCameraPreview(LiveCard liveCard, Camera camera) {
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = liveCard.getSurfaceHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFpsRange(30000, 30000);
        parameters.setPreviewSize(640, 360);
        parameters.setPictureSize(2528, 1856);
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mPreviewEnabled = true;
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void renderingPaused(SurfaceHolder holder, boolean pause) {
        if (pause && mPreviewEnabled) {
            mCamera.stopPreview();
            mPreviewEnabled = false;
        } else if(!pause && !mPreviewEnabled) {
            mCamera.startPreview();
            mPreviewEnabled = true;
        }
    }

}
