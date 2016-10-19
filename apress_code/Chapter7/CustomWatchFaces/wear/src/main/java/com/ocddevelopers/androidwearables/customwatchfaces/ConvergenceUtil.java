package com.ocddevelopers.androidwearables.customwatchfaces;

import android.net.Uri;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Contains helper methods to read and write the value of continuous sweep from and to the Data API.
 */
public class ConvergenceUtil {
    public static final String PATH_CONTINUOUS_SWEEP = "/convergence/sweep";
    public static final String KEY_CONTINUOUS_SWEEP = "sweep";

    public static void putContinuousSweep(GoogleApiClient googleApiClient, boolean sweep) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_CONTINUOUS_SWEEP);
        putDataMapRequest.getDataMap().putBoolean(KEY_CONTINUOUS_SWEEP, sweep);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                    }
                });
    }

    public static boolean extractContinuousSweep(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils
                .freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getType() != DataEvent.TYPE_CHANGED) {
                continue;
            }

            DataItem dataItem = event.getDataItem();
            if (!dataItem.getUri().getPath().equals(ConvergenceUtil.PATH_CONTINUOUS_SWEEP)) {
                continue;
            }

            return extractContinuousSweep(dataItem);
        }

        return false;
    }

    private static boolean extractContinuousSweep(DataItem dataItem) {
        if(dataItem == null) {
            return false;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
        DataMap config = dataMapItem.getDataMap();
        return config.getBoolean(KEY_CONTINUOUS_SWEEP, false);
    }

    public interface FetchContinuousSweepCallback {
        void onContinuousSweepFetched(boolean continuousSweep);
    }

    public static void fetchContinuousSweep(final GoogleApiClient client,
                                          final FetchContinuousSweepCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult localNodeResult) {
                        String localNode = localNodeResult.getNode().getId();
                        fetchContinuousSweepDataItem(client, localNode, callback);
                    }
                }
        );
    }

    private static void fetchContinuousSweepDataItem(GoogleApiClient client, String localNode,
                                                     final FetchContinuousSweepCallback callback) {
        Uri uri = new Uri.Builder()
                .scheme("wear")
                .path(PATH_CONTINUOUS_SWEEP)
                .authority(localNode)
                .build();

        Wearable.DataApi.getDataItem(client, uri)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                boolean continuousSweep = extractContinuousSweep(dataItemResult.getDataItem());
                callback.onContinuousSweepFetched(continuousSweep);
            }
        });
    }

}
