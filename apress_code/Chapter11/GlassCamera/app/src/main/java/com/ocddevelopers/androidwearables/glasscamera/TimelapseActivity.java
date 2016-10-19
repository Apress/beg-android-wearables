package com.ocddevelopers.androidwearables.glasscamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
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

import java.io.IOException;

/**
 * Shows how to use the Camera API to record timelapse vi`deos.
 */
public class TimelapseActivity extends Activity {
    private static final String TAG = "CA";
    private Camera mCamera;
    private CameraPreview mPreview;
    private TuggableView mTuggableView;
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecording;
    private SoundPool mSoundPool;
    private int mStartRecordingSoundId, mStopRecordingSoundId;
    private float mVolume;
    private AudioManager mAudioManager;
    private String mOutputFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_camera, null);
        mTuggableView = new TuggableView(this, content);
        setContentView(mTuggableView);

        mCamera = CameraUtils.getCameraInstance();

        mPreview = new CameraPreview(this, mCamera, true);
        FrameLayout preview = (FrameLayout) content.findViewById(R.id.container);
        preview.addView(mPreview);
        preview.addView(new TextView(this)); // needed to avoid bug

        initSoundPool();
    }

    private void initSoundPool() {
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mStartRecordingSoundId = mSoundPool.load(getApplicationContext(), R.raw.sound_video_start, 1);
        mStopRecordingSoundId = mSoundPool.load(getApplicationContext(), R.raw.sound_video_stop, 1);

        float actVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float  maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;
    }

    private void playSound(int soundId) {
        mSoundPool.play(soundId, mVolume, mVolume, 1, 0, 1f);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.timelapse, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(mIsRecording) {
            menu.findItem(R.id.action_record_timelapse).setIcon(R.drawable.ic_video_off_50);
            menu.findItem(R.id.action_record_timelapse).setTitle("Stop timelapse");
        } else {
            menu.findItem(R.id.action_record_timelapse).setIcon(R.drawable.ic_video_50);
            menu.findItem(R.id.action_record_timelapse).setTitle("Record timelapse");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_record_timelapse:
                toggleRecording();
                invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mAudioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            toggleRecording();
            return true;
            /*
            mCamera.stopPreview();
            mCamera.release();
            return false;
            */
        } else {
            return super.onKeyDown(keyCode, event);
        }
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
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }

        super.onPause();
    }

    private void toggleRecording() {
        if (mIsRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            CameraUtils.scanFile(this, mOutputFilename);

            // inform the user that recording has stopped
            playSound(mStopRecordingSoundId);
            mIsRecording = false;
        } else {
            // initialize video camera
            try {
                if (prepareVideoRecorder()) {
                    // Camera is available and unlocked, MediaRecorder is prepared,
                    // now you can start recording
                    mMediaRecorder.start();

                    // inform the user that recording has started
                    playSound(mStartRecordingSoundId);
                    mIsRecording = true;
                } else {
                    // prepare didn't work, release the camera
                    releaseMediaRecorder();
                    // inform user
                }
            } catch (Exception e) {
                mCamera.release();
            }
        }

    }

    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        mMediaRecorder.setCaptureRate(0.5);

        // Step 4: Set output file
        mOutputFilename = CameraUtils.getOutputTimelapseFile().toString();
        mMediaRecorder.setOutputFile(mOutputFilename);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }



}
