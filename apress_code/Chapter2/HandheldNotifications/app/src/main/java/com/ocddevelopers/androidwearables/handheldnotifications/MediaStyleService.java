package com.ocddevelopers.androidwearables.handheldnotifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;

/**
 * Demonstrates how to create a MediaStyle notification on a handheld device.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MediaStyleService extends Service {
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_STOP = "stop";
    private MediaPlayer mMediaPlayer;
    private MediaSession mMediaSession;
    private MediaController mMediaController;
    private boolean mPlayingMusic;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String action = intent.getAction();
        if(ACTION_PAUSE.equals(action)) {
            mMediaController.getTransportControls().pause();
        } else if(ACTION_STOP.equals(action)) {
            mMediaController.getTransportControls().stop();
        } else if(ACTION_PLAY.equals(action)) {
            mMediaController.getTransportControls().play();
        } else if(!mPlayingMusic) {
            init();
            updateMediaNotification();
        }

        return START_STICKY;
    }


    private void init() {
        mMediaSession = new MediaSession(this, "media_session");
        mMediaSession.setCallback(mCallback);

        mMediaController = new MediaController(this, mMediaSession.getSessionToken());

        // play sample mp3
        mMediaPlayer = MediaPlayer.create(this, R.raw.bach_air);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(mCompletionListener);

        startPlayingMusic();
    }

    private void startPlayingMusic() {
        mMediaPlayer.start();
        mPlayingMusic = true;
        updateMediaNotification();
    }

    private void pauseMusic() {
        mMediaPlayer.pause();
        setMusicPaused();
    }

    private void setMusicPaused() {
        mPlayingMusic = false;
        stopForeground(false);
        updateMediaNotification();
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            setMusicPaused();
        }
    };

    private MediaSession.Callback mCallback = new MediaSession.Callback() {
        @Override
        public void onPlay() {
            super.onPlay();
            startPlayingMusic();
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseMusic();
        }

        @Override
        public void onStop() {
            super.onStop();

            mMediaPlayer.stop();
            mMediaPlayer.release();
            mPlayingMusic = false;

            stopSelf();
        }
    };

    private void updateMediaNotification() {
        Notification mediaNotification = buildMediaNotification();

        if(mPlayingMusic) {
            startForeground(NOTIFICATION_ID, mediaNotification);
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, buildMediaNotification());
        }
    }

    private Notification buildMediaNotification() {
        Bitmap scaledLargeIcon = getAlbumArt();

        Notification.Style mediaStyle = new Notification.MediaStyle()
                .setMediaSession(mMediaSession.getSessionToken())
                .setShowActionsInCompactView(0);

        Intent intent = new Intent(this, MediaStyleService.class );
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        // MediaStyle notifications automatically set CATEGORY_TRANSPORT
        // MediaStyle notifications currently require Android 5.0 and can't use NotificatonCompat
        Notification.Builder mediaNotificationBuilder = new Notification.Builder(this)
                .setContentTitle("Media Style Notification")
                .setContentText("Album Name Here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getActivityPendingIntent())
                .setPriority(Notification.PRIORITY_HIGH)
                .setStyle(mediaStyle)
                .setLargeIcon(scaledLargeIcon)
                .setShowWhen(false)
                .setDeleteIntent(pendingIntent);

        if(mPlayingMusic) {
            mediaNotificationBuilder.addAction(makeAction(R.drawable.ic_pause, "Pause",
                    ACTION_PAUSE));
        } else {
            mediaNotificationBuilder.addAction(makeAction(R.drawable.ic_play, "Play",
                    ACTION_PLAY));
        }

        return mediaNotificationBuilder.build();
    }

    private PendingIntent getActivityPendingIntent() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Bitmap getAlbumArt() {
        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.bg_default_album_art);
        return Bitmap.createScaledBitmap(largeIcon, width, height, false);
    }

    private Notification.Action makeAction(int iconResId, String title, String intentAction) {
        Intent intent = new Intent(this, MediaStyleService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        return new Notification.Action.Builder(iconResId, title, pendingIntent).build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
