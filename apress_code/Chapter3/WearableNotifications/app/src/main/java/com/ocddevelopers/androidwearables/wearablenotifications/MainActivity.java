package com.ocddevelopers.androidwearables.wearablenotifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;

/**
 * Triggers several example notifications that illustrate how to customize notifications
 * specifically for wearables.
 */
public class MainActivity extends ActionBarActivity {
    private static final int NOTIFICATION_ID = 1;
    private static final String GROUP_KEY_MESSAGES = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onBuildTaskStackContentIntentClick(View view) {
        PendingIntent conversationPendingIntent = getConversationPendingIntent("Preppy Rabbit", 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Preppy Rabbit")
                .setContentText("I like carrots")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(conversationPendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private PendingIntent getConversationPendingIntent(String chattingWith, int requestCode) {
        Intent conversationIntent = new Intent(this, ChatDetailActivity.class);

        if(chattingWith != null) {
            conversationIntent.putExtra(ChatDetailActivity.EXTRA_CHATTING_WITH, chattingWith);
        }

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(ChatDetailActivity.class);
        taskStackBuilder.addNextIntent(conversationIntent);

        return taskStackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void onWearableOnlyActionsClick(View view) {
        PendingIntent handheldActionFeedbackPendingIntent =
                getActionFeedbackPendingIntent("You invoked the handheld only action", 0);

        PendingIntent wearableActionFeedbackPendingIntent =
                getActionFeedbackPendingIntent("You invoked the wearable only action", 1);

        PendingIntent bothActionFeedbackPendingIntent =
                getActionFeedbackPendingIntent("You invoked the action that appears on both devices", 2);

        NotificationCompat.Action handheldOnlyAction = new NotificationCompat.Action(
                android.R.drawable.ic_media_previous, "Handheld", handheldActionFeedbackPendingIntent);
        NotificationCompat.Action wearableOnlyAction = new NotificationCompat.Action(
                android.R.drawable.ic_media_next, "Wearable", wearableActionFeedbackPendingIntent);
        NotificationCompat.Action action = new NotificationCompat.Action(
                android.R.drawable.ic_media_pause, "Both", bothActionFeedbackPendingIntent);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addAction(wearableOnlyAction)
                .addAction(action);

        PendingIntent mainActivityPendingIntent = getMainActivityPendingIntent();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Title")
                .setContentText("Text")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(mainActivityPendingIntent)
                .setCategory(Notification.CATEGORY_STATUS)
                .addAction(action)
                .extend(wearableExtender)
                .addAction(handheldOnlyAction)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private PendingIntent getMainActivityPendingIntent() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0,
                mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getActionFeedbackPendingIntent(String actionFeedback, int requestCode) {
        Intent actionFeedbackIntent = new Intent(this, ActionFeedbackActivity.class);
        actionFeedbackIntent.putExtra(ActionFeedbackActivity.EXTRA_ACTION_FEEDBACK, actionFeedback);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this)
                .addParentStack(ActionFeedbackActivity.class)
                .addNextIntent(actionFeedbackIntent);

        return taskStackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void onPagingClick(View view) {
        CharSequence message = generateSampleMessage1();

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(message);

        Notification messageHistoryPage = new NotificationCompat.Builder(this)
                .setContentText(message) // needed for Glass
                .setStyle(bigTextStyle)
                .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addPage(messageHistoryPage);

        Bitmap preppyAvatar = getScaledLargeIconFromResource(R.drawable.preppy);

        Notification notificationWithPages = new NotificationCompat.Builder(this)
                .setContentTitle("Preppy Rabbit")
                .setContentText("carrots*")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getConversationPendingIntent("Preppy Rabbit", 0))
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setLargeIcon(preppyAvatar)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .extend(wearableExtender)
                .build();

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(NOTIFICATION_ID, notificationWithPages);
    }

    private CharSequence generateSampleMessage1() {
        return TextUtils.concat(formatMessage("Preppy Rabbit", "Hey Fox"), "\n\n",
                formatMessage("Preppy Rabbit", "Are you there buddy?"), "\n\n",
                formatMessage("Fox Vulpes", "Yes Preppy, what is it?"), "\n\n",
                formatMessage("Preppy Rabbit", "Just want you to know"), "\n\n",
                formatMessage("Preppy Rabbit", "I like karets!"), "\n\n",
                formatMessage("Preppy Rabbit", "carrots*"));
    }

    private CharSequence generateSampleMessage2() {
        return TextUtils.concat(formatMessage("Fox Vulpes", "We're heading out. Are you ready?"), "\n\n",
                formatMessage("Serious Toad", "I think so!"), "\n\n",
                formatMessage("Serious Toad", "Yes yes, I'm ready."));
    }

    private Spannable formatMessage(String author, String message) {
        Spannable spannable = new SpannableString(author + " "  + message);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private Bitmap getScaledLargeIconFromResource(int resource) {
        Resources res = getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);
        Bitmap largeIcon = BitmapFactory.decodeResource(res, resource);
        return Bitmap.createScaledBitmap(largeIcon, width, height, false);
    }

    public void onStackingClick(View view) {
        String[] authors = { "Preppy Rabbit", "Serious Toad" };
        String[] messages = { "carrots*", "Yes yes, I'm ready." };
        CharSequence[] messageHistories = { generateSampleMessage1(), generateSampleMessage2() };

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setBackground(getScaledLargeIconFromResource(R.drawable.wear_bg));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .addLine(formatInboxStyleLine(authors[0], messages[0]))
                .addLine(formatInboxStyleLine(authors[1], messages[1]));

        PendingIntent mainActivityPendingIntent = getMainActivityPendingIntent();

        Notification summaryNotification = new NotificationCompat.Builder(this)
                .setContentTitle("2 messages received")
                .setContentText("Preppy Rabbit, Serious Toad")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(mainActivityPendingIntent)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .extend(wearableExtender)
                .setGroup(GROUP_KEY_MESSAGES)
                .setGroupSummary(true)
                .setStyle(inboxStyle)
                .build();

        Notification messageNotification1 = generateSampleNotification(authors[0], messages[0],
                messageHistories[0], 0);
        Notification messageNotification2 = generateSampleNotification(authors[1], messages[1],
                messageHistories[1], 1);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, messageNotification1);
        notificationManager.notify(NOTIFICATION_ID+1, messageNotification2);
        notificationManager.notify(NOTIFICATION_ID+2, summaryNotification);
    }

    private Spannable formatInboxStyleLine(String username, String message) {
        TextAppearanceSpan notificationSenderSpan = new TextAppearanceSpan(this,
                R.style.NotificationPrimaryText);
        Spannable spannable = new SpannableString(username + " " + message);
        spannable.setSpan(notificationSenderSpan, 0, username.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private Notification generateSampleNotification(String author, String message,
                                                    CharSequence messageHistory, int requestCode) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(messageHistory);

        Notification messageHistoryPage = new NotificationCompat.Builder(this)
                .setStyle(bigTextStyle)
                .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addPage(messageHistoryPage);

        Notification notificationWithPages = new NotificationCompat.Builder(this)
                .setContentTitle(author)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getConversationPendingIntent(author, requestCode))
                .setGroup(GROUP_KEY_MESSAGES)
                .extend(wearableExtender)
                .build();

        return notificationWithPages;
    }

    public void onVoiceReplyClick(View view) {
        String[] choices = new String[] { "Yes", "No", "In a meeting" };

        RemoteInput remoteInput = new RemoteInput.Builder(ChatDetailActivity.EXTRA_VOICE_REPLY)
                .setLabel("Reply")
                .setChoices(choices)
                .setAllowFreeFormInput(false)
                .build();

        PendingIntent replyPendingIntent = getConversationPendingIntent("Preppy Rabbit", 0);

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_full_reply, "Reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addAction(replyAction);

        Bitmap preppyAvatar = getScaledLargeIconFromResource(R.drawable.preppy);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Preppy Rabbit")
                .setContentText("Hey Fox")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getConversationPendingIntent("Preppy Rabbit", 20))
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(preppyAvatar)
                .extend(wearableExtender)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public void onBackgroundOnlyImageClick(View view) {
        Notification imageOneNotification = getImageOnlyNotification("Fox Avatar", R.drawable.fox);
        Notification imageTwoNotification = getImageOnlyNotification("Rabbit Avatar",
                R.drawable.preppy);

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .addPage(imageOneNotification)
                .addPage(imageTwoNotification);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("My Slideshow")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getMainActivityPendingIntent())
                .setCategory(Notification.CATEGORY_SOCIAL)
                .extend(wearableExtender)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification getImageOnlyNotification(String title, int drawableResource) {
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                .setBackground(BitmapFactory.decodeResource(getResources(), drawableResource))
                .setHintShowBackgroundOnly(true);

        return new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText("")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .extend(wearableExtender)
                .build();
    }

    public void onContentActionClick(View view) {
        PendingIntent pausePendingIntent = getActionFeedbackPendingIntent("You pressed pause", 0);

        NotificationCompat.Action wearablePauseAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_pause_dark,
                        "Pause", pausePendingIntent)
                        .build();

        NotificationCompat.Action handheldPauseAction =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_media_pause,
                        "Pause", pausePendingIntent)
                        .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .addAction(wearablePauseAction)
                .setContentAction(0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Song Name")
                .setContentText("Artist Name")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentIntent(getMainActivityPendingIntent())
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .addAction(handheldPauseAction)
                .extend(wearableExtender)
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
