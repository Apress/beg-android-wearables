package com.ocddevelopers.androidwearables.wearabledatalayer;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataLayerListenerService extends WearableListenerService {
    private static final String START_ACTIVITY_PATH = "/start/CounterActivity";
    private static final String START_CONFIRM_ACTIVITY_PATH = "/start_confirm/CounterActivity";
    private static final String CONFIRMATION_PATH = "/confirm/CounterActivity";
    private static final String IMAGE_ASSET_KEY = "preview";
    public static final String TAKE_IMAGE_PATH = "/image";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String path = messageEvent.getPath();

        if(START_ACTIVITY_PATH.equals(path)) {
            Intent intent = new Intent(this, CounterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if(START_CONFIRM_ACTIVITY_PATH.equals(path)) {
            Intent intent = new Intent(this, CounterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            confirmStarted(messageEvent.getSourceNodeId());
        }
    }

    private void confirmStarted(String sourceNodeId) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult = googleApiClient.blockingConnect(10, TimeUnit.SECONDS);
        if(connectionResult.isSuccess()) {
            Wearable.MessageApi.sendMessage(googleApiClient, sourceNodeId, CONFIRMATION_PATH, null);
        }
    }







    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);

        for(DataEvent dataEvent : events) {
            DataItem dataItem = dataEvent.getDataItem();
            String path = dataItem.getUri().getPath();

            if (TAKE_IMAGE_PATH.equals(path)) {
                PutDataRequest request = PutDataRequest.createFromDataItem(dataItem);
                Asset profileAsset = request.getAsset(IMAGE_ASSET_KEY);

                // creat enotification instead?
                Bitmap image = loadBitmapFromAsset(profileAsset);
                Notification notification = createNotification(image);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.notify(1, notification);
            }
        }
    }

    private Notification createNotification(Bitmap image) {
        NotificationCompat.BigPictureStyle bigPictureStyle =
                new NotificationCompat.BigPictureStyle()
                .bigPicture(image);

        return new NotificationCompat.Builder(this)
                .setContentTitle("Image Received")
                .setContentText("")
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setStyle(bigPictureStyle)
                .build();
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult result =
                googleApiClient.blockingConnect(500, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }

        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                googleApiClient, asset).await().getInputStream();
        googleApiClient.disconnect();

        if (assetInputStream == null) {
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

}
