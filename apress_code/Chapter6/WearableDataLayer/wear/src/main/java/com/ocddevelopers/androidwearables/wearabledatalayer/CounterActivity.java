package com.ocddevelopers.androidwearables.wearabledatalayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Demonstrates how to use the Message API and Data API to increment or decrement a number
 * that is synchronized between a handheld and a wearable.
 */
public class CounterActivity extends Activity {
    public static final String COUNTER_INCREMENT_PATH = "/counter/increment";
    public static final String COUNTER_DECREMENT_PATH = "/counter/decrement";
    public static final String COUNTER_PATH = "/counter";
    public static final String KEY_COUNT = "count";
    private TextView mCountText;
    private GoogleApiClient mGoogleApiClient;
    int mCount = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        mCountText = (TextView) findViewById(R.id.count);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Wearable.DataApi.removeListener(mGoogleApiClient, mDataListener);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void onIncrementClick(View view) {
        sendMessage(COUNTER_INCREMENT_PATH);
    }

    public void onDecrementClick(View view) {
        sendMessage(COUNTER_DECREMENT_PATH);
    }

    private void sendMessage(final String path) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            path, null);
                }
            }
        });
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, mDataListener);
            loadRemoteNodeId();
        }

        @Override
        public void onConnectionSuspended(int cause) {
        }
    };


    private void loadRemoteNodeId() {
        Wearable.DataApi.getDataItems(mGoogleApiClient).setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for (int i = 0; i < dataItems.getCount(); ++i) {
                    DataItem dataItem = dataItems.get(i);
                    if (COUNTER_PATH.equals(dataItem.getUri().getPath())) {
                        updateCountFromDataItem(dataItem);
                    }
                }
            }
        });
    }

    private DataApi.DataListener mDataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            final List<DataEvent> events = FreezableUtils
                    .freezeIterable(dataEvents);
            for(DataEvent dataEvent : events) {
                DataItem dataItem = dataEvent.getDataItem();
                if("/counter".equals(dataItem.getUri().getPath())) {
                    updateCountFromDataItem(dataItem);
                }
            }
        }
    };

    private void updateCountFromDataItem(DataItem dataItem) {
        DataMap dataMap = DataMapItem.fromDataItem(dataItem).getDataMap();
        mCount = dataMap.getInt(KEY_COUNT);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCountText.setText(Integer.toString(mCount));
            }
        });
    }

}
