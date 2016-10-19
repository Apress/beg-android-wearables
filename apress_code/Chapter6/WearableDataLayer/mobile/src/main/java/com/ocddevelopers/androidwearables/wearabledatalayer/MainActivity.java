package com.ocddevelopers.androidwearables.wearabledatalayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Demonstrates how to use the Message API and Data API to increment or decrement a number
 * that is synchronized between a handheld and a wearable.
 */
public class MainActivity extends ActionBarActivity {
    private static final String START_ACTIVITY_PATH = "/start/CounterActivity";
    private static final String START_CONFIRM_ACTIVITY_PATH = "/start_confirm/CounterActivity";
    private static final String CONFIRMATION_PATH = "/confirm/CounterActivity";
    private static final String COUNTER_PATH = "/counter";
    public static final String COUNTER_INCREMENT_PATH = "/counter/increment";
    public static final String COUNTER_DECREMENT_PATH = "/counter/decrement";
    private static final String KEY_COUNT = "count";
    private int mCount;
    private NumberPicker mNumberPicker;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumberPicker = (NumberPicker) findViewById(R.id.activity_main);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(100);
        mNumberPicker.setOnValueChangedListener(mValueChangeListener);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .build();

        // disable buttons until GoogleApiClient is connected
        setButtonsEnabled(false);

        mCount = 0;
    }

    private void setButtonsEnabled(boolean enabled) {
        findViewById(R.id.start_wearable).setEnabled(enabled);
        findViewById(R.id.start_confirm_wearable).setEnabled(enabled);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Wearable.MessageApi.removeListener(mGoogleApiClient, mMessageListener);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Wearable.MessageApi.addListener(mGoogleApiClient, mMessageListener);
            fetchCurrentCount();
            setButtonsEnabled(true);
        }

        @Override
        public void onConnectionSuspended(int cause) {
            setButtonsEnabled(false);
        }
    };

    private void fetchCurrentCount() {
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult nodeResult) {
                if (nodeResult.getStatus().isSuccess()) {
                    tryLoadCount(nodeResult.getNode().getId());
                }
            }
        });
    }

    private void tryLoadCount(String mNodeId) {
        Uri testUri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME)
                .authority(mNodeId).path("/counter").build();

        Wearable.DataApi.getDataItem(mGoogleApiClient, testUri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {
                    loadCountFromDataItem(dataItemResult.getDataItem());
                }
            }
        });
    }

    public void onStartWearableCounterClick(View view) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            START_ACTIVITY_PATH, null);
                }
            }
        });
    }

    public void onStartConfirmWearableCounterClick(View view) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                for (Node node : getConnectedNodesResult.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            START_CONFIRM_ACTIVITY_PATH, null);
                }
            }
        });
    }

    private MessageApi.MessageListener mMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if (CONFIRMATION_PATH.equals(messageEvent.getPath())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Wearable CounterActivity started",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (COUNTER_INCREMENT_PATH.equals(messageEvent.getPath())) {
                ++mCount;
                updateNumberPicker();
                updateCountData();
            } else if (COUNTER_DECREMENT_PATH.equals(messageEvent.getPath())) {
                --mCount;
                updateNumberPicker();
                updateCountData();
            }
        }
    };

    private void updateNumberPicker() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNumberPicker.setValue(mCount);
            }
        });
    }

    private void loadCountFromDataItem(DataItem dataItem) {
        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
        DataMap dataMap = dataMapItem.getDataMap();
        mCount = dataMap.getInt(KEY_COUNT);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNumberPicker.setValue(mCount);
            }
        });
    }

    private NumberPicker.OnValueChangeListener mValueChangeListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            // push the new value of the counter to the Data API
            mCount = newVal;
            updateCountData();
        }
    };



    private void updateCountData() {
        PutDataMapRequest updateCounterDataMapRequest = PutDataMapRequest.create(COUNTER_PATH);
        updateCounterDataMapRequest.getDataMap().putInt(KEY_COUNT, mCount);
        PutDataRequest putDataRequest = updateCounterDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
            }
        });
    }
}

