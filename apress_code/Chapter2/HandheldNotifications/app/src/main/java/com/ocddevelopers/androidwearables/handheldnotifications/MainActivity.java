package com.ocddevelopers.androidwearables.handheldnotifications;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Demonstrates how to create different kinds of notifications for handheld devices.
 */
public class MainActivity extends ActionBarActivity {
    public static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // disable media style example if running less than API 21
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.media_style).setEnabled(false);
        }
    }

    public void onStandardNotificationButtonClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification standardNotification = new NotificationCompat.Builder(this)
                .setContentTitle("On time ATL–SFO")
                .setContentText("DL1234 departing 6:18pm")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, standardNotification);
    }

    private PendingIntent getActivityPendingIntent() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void onUpdateStandardNotificationButtonClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification updatedNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Delayed ATL–SFO")
                .setContentText("DL1234 now departing 7:05pm")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, updatedNotification);
    }

    public void onPublicNotificationClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Public Notification")
                .setContentText("Public content here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void onPrivateNotificationClick(View view){
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Private Notification")
                .setContentText("Sensitive or private content here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID+1, notification);
    }

    public void onSecretNotificationClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Secret Notification")
                .setContentText("Sensitive content here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_SECRET)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID+3, notification);
    }

    public void onPrivateNotificationWithPublicVersionClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification publicNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Public Version Notification")
                .setContentText("Redacted private content here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Private Notification")
                .setContentText("Sensitive or private content here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setPublicVersion(publicNotification)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID+2, notification);
    }

    public void onHighPriorityNotificationClick(View view) {
        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("High priority")
                .setContentText("Important message here")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(Notification.CATEGORY_STATUS)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void onBigTextStyleButtonClick(View view) {
        String longText = "Without BigTextStyle, only a single line of text would be visible. " +
                "Any additional text would not appear directly on the notification. " +
                "The entire first line would not even be on the notification if it were too long. " +
                "Text that doesn't fit in a standard notification becomes ellipsized. " +
                "That is, the characters that don't fit are removed and replaced by ellipsis.";

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(longText);

        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification bigTextStyleNotification = new NotificationCompat.Builder(this)
                .setContentTitle("All Hail BigTextStyle")
                .setContentText(longText)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setStyle(bigTextStyle)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, bigTextStyleNotification);
    }

    public void onBigPictureStyleButtonClick(View view) {
        Bitmap bigPicture = BitmapFactory.decodeResource(getResources(), R.drawable.mandrill);
        String contentText = "A classic image processing test image.";
        String summaryText = "This mandrill is often used as a test image.";

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle()
                .setSummaryText(summaryText)
                .bigPicture(bigPicture);

        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification bigPictureStyleNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Rando Fact")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .setStyle(bigPictureStyle)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, bigPictureStyleNotification);
    }

    public void onInboxStyleButtonClick(View view) {
        // you could use Strings instead of Spannables if you wanted a simpler implementation.
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .addLine(formatInboxStyleLine("Alice", "hey there"))
                .addLine(formatInboxStyleLine("Bob", "what are you doing?"))
                .addLine(formatInboxStyleLine("Eve", "give me a call when you get a chance"))
                .addLine(formatInboxStyleLine("Trudy", "I like potatoes"))
                .addLine(formatInboxStyleLine("Mallory", "Dinner tomorrow?"));

        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification inboxStyleNotification = new NotificationCompat.Builder(this)
                .setContentTitle("5 messages received")
                .setContentText("Alice, Bob, Eve, Trudy, Mallory")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setStyle(inboxStyle)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, inboxStyleNotification);
    }

    /**
     * Returns a Spannable that highlights the username by making its color lighter
     */
    private Spannable formatInboxStyleLine(String username, String message) {
        Spannable spannable = new SpannableString(username + " " + message);
        int color = getResources().getColor(R.color.notification_title);
        spannable.setSpan(new ForegroundColorSpan(color), 0, username.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    public void onNotificationActionsButtonClick(View view) {
        PendingIntent pausePendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_PAUSE);

        PendingIntent nextPendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_NEXT);
        Bitmap scaledLargeIcon = getAlbumArt();

        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification actionNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Song Name")
                .setContentText("Artist Name")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setLargeIcon(scaledLargeIcon)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setShowWhen(false)
                .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, actionNotification);
    }

    private Bitmap getAlbumArt() {
        // a notification's large icon must be scaled to the right size
        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.bg_default_album_art);
        return Bitmap.createScaledBitmap(largeIcon, width, height, false);
    }

    private PendingIntent getMediaCommandPendingIntent(String commandName) {
        Intent intent = new Intent(this, MediaCommandService.class);
        intent.setAction(commandName);
        return PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint("NewApi")
    public void onCustomNotificationButtonClick(View v) {
        PendingIntent pausePendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_PAUSE);
        PendingIntent nextPendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_NEXT);
        PendingIntent prevPendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_PREV);
        PendingIntent closePendingIntent =
                getMediaCommandPendingIntent(MediaCommandService.ACTION_CLOSE);

        // the custom unexpanded content view
        // custom notifications are available since API Level 11 (which is below our min SDK version)
        RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(),
                R.layout.statusbar);
        contentView.setImageViewResource(R.id.albumart, R.drawable.bg_default_album_art);
        contentView.setTextViewText(R.id.trackname, "Song Name");
        contentView.setTextViewText(R.id.artistalbum, "Artist Name");
        contentView.setOnClickPendingIntent(R.id.playpause, pausePendingIntent);
        contentView.setOnClickPendingIntent(R.id.next, nextPendingIntent);
        contentView.setOnClickPendingIntent(R.id.veto, closePendingIntent);

        PendingIntent activityPendingIntent = getActivityPendingIntent();

        Notification customNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Song Name")
                .setContentText("Artist Name")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setContent(contentView)
                .setOngoing(true)
                .build();

        // the custom expanded content view
        // expandable notifications are available since API Level 16
        if (isJellybeanOrAbove()) {
            RemoteViews expandedNotificationView =
                    new RemoteViews(getApplicationContext().getPackageName(),
                            R.layout.statusbar_expanded);
            expandedNotificationView.setImageViewResource(R.id.albumart,
                    R.drawable.bg_default_album_art);
            expandedNotificationView.setTextViewText(R.id.trackname, "Song Name");
            expandedNotificationView.setTextViewText(R.id.artist, "Artist Name");
            expandedNotificationView.setTextViewText(R.id.album, "Album Name");
            expandedNotificationView.setOnClickPendingIntent(R.id.playpause, pausePendingIntent);
            expandedNotificationView.setOnClickPendingIntent(R.id.prev, prevPendingIntent);
            expandedNotificationView.setOnClickPendingIntent(R.id.next, nextPendingIntent);
            expandedNotificationView.setOnClickPendingIntent(R.id.veto, closePendingIntent);

            customNotification.bigContentView = expandedNotificationView;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, customNotification);
    }

    private boolean isJellybeanOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public void onMediaStyleNotificationButtonClick(View view) {
        Intent mediaIntent = new Intent(this, MediaStyleService.class);
        startService(mediaIntent);
    }

}
