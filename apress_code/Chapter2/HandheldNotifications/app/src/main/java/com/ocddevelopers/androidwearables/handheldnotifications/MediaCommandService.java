package com.ocddevelopers.androidwearables.handheldnotifications;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.util.Locale;

/**
 * MediaCommandService uses text to speech to provide feedback when the user presses
 * an action button in a sample notification.
 */
public class MediaCommandService extends Service implements TextToSpeech.OnInitListener {
    public static final String ACTION_PAUSE = "com.ocd.dev.androidwearables.chapter2.action.PAUSE";
    public static final String ACTION_PREV = "com.ocd.dev.androidwearables.chapter2.action.PREV";
    public static final String ACTION_NEXT = "com.ocd.dev.androidwearables.chapter2.action.NEXT";
    public static final String ACTION_CLOSE = "com.ocd.dev.androidwearables.chapter2.action.CLOSE";
    private TextToSpeech mTextToSpeech;
    private String mMessage;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (ACTION_PAUSE.equals(action)) {
            mMessage = "Pause";
        } else if (ACTION_NEXT.equals(action)) {
            mMessage = "Next";
        } else if (ACTION_PREV.equals(action)) {
            mMessage = "Previous";
        } else if (ACTION_CLOSE.equals(action)) {
            mMessage = "Close";
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(MainActivity.NOTIFICATION_ID);
        } else {
            mMessage = "Invalid Action";
        }

        mTextToSpeech = new TextToSpeech(this, this);

        showToast();

        return START_NOT_STICKY;
    }

    private void showToast() {
        Toast.makeText(this, mMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTextToSpeech.setLanguage(Locale.US);

            if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED) {
                mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        stopSelf();
                        mTextToSpeech.shutdown();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        stopSelf();
                        mTextToSpeech.shutdown();
                    }
                });

                mTextToSpeech.speak(mMessage, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
