package com.ocddevelopers.androidwearables.glasscamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Shows how to use the Camera API to display a viewfinder and take pictures.
 */
public class CameraActivity extends Activity {
    private static final String TAG = "CA";
    private Camera mCamera;
    private CameraPreview mPreview;
    private TuggableView mTuggableView;
    private volatile boolean mTakingPicture;
    private SoundPool mSoundPool;
    private float mVolume;
    private AudioManager mAudioManager;
    private int mPhotoReadySoundId, mPhotoShutterSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_camera, null);
        mTuggableView = new TuggableView(this, content);
        setContentView(mTuggableView);

        mCamera = CameraUtils.getCameraInstance();

        mPreview = new CameraPreview(this, mCamera, false);
        FrameLayout preview = (FrameLayout) content.findViewById(R.id.container);
        preview.addView(mPreview);

        // fixes
        preview.addView(new TextView(this));

        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mPhotoReadySoundId = mSoundPool.load(getApplicationContext(),
                R.raw.sound_photo_ready, 1);
        mPhotoShutterSoundId = mSoundPool.load(getApplicationContext(),
                R.raw.sound_photo_shutter, 1);

        float actVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float  maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_take_picture:
                takePicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void takePicture() {
        if (mTakingPicture) {
            mAudioManager.playSoundEffect(Sounds.DISALLOWED);
        } else {
            mTakingPicture = true;
            mCamera.takePicture(null, null, mPicture);
            playSound(mPhotoReadySoundId);
        }
    }

    private void playSound(int soundId) {
        mSoundPool.play(soundId, mVolume, mVolume, 1, 0, 1f);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null) {
            mCamera = CameraUtils.getCameraInstance();
        }

    }

    @Override
    protected void onPause() {
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }

        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mAudioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Stop the preview and release the camera.
            // Execute your logic as quickly as possible
            // so the capture happens quickly.
            /*
            mCamera.stopPreview();
            mCamera.release();
            return false;
            */
            takePicture();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            playSound(mPhotoShutterSoundId);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    File pictureFile = CameraUtils.getOutputPictureFile();
                    if (pictureFile == null){
                        Log.d(TAG, "Error creating media file, check storage permissions");
                        return;
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        fos.write(data);
                        fos.close();

                        CameraUtils.scanFile(CameraActivity.this, pictureFile.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, "Error accessing file: " + e.getMessage());
                    }

                    mTakingPicture = false;
                }
            }).start();

            mCamera.startPreview();
        }
    };

}
