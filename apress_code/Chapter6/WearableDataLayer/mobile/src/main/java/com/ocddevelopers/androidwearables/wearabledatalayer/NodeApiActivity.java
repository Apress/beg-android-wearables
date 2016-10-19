package com.ocddevelopers.androidwearables.wearabledatalayer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Shows how to use the Node API to 1) obtain a remote node, 2) obtain a local node, and
 * 3) check if a watch is paired to a handheld or vice versa.
 */
public class NodeApiActivity extends ActionBarActivity {
    private GoogleApiClient mGoogleApiClient;
    private TextView mConnectionStatus;
    private TextView mLocalDisplayName, mLocalNodeId;
    private TextView mRemoteDisplayName, mRemoteNodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodeapi);

        mConnectionStatus = (TextView) findViewById(R.id.connection_status);
        mLocalDisplayName = (TextView) findViewById(R.id.local_displayname);
        mLocalNodeId = (TextView) findViewById(R.id.local_nodeid);
        mRemoteDisplayName = (TextView) findViewById(R.id.remote_displayname);
        mRemoteNodeId = (TextView) findViewById(R.id.remote_nodeid);

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
        Wearable.NodeApi.removeListener(mGoogleApiClient, mNodeListener);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            Wearable.NodeApi.addListener(mGoogleApiClient, mNodeListener);
            fetchLocalNode();
            fetchRemoteNode();
        }

        @Override
        public void onConnectionSuspended(int cause) {
        }
    };

    private void fetchLocalNode() {
        Wearable.NodeApi.getLocalNode(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult localNodeResult) {
                Node localNode = localNodeResult.getNode();
                mLocalDisplayName.setText(localNode.getDisplayName());
                mLocalNodeId.setText(localNode.getId());
            }
        });
    }

    private void fetchRemoteNode() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                boolean connected;

                if(result.getNodes().size() > 0) {
                    // assumption: there is no more than 1 connected node
                    Node remoteNode = result.getNodes().get(0);
                    mRemoteDisplayName.setText(remoteNode.getDisplayName());
                    mRemoteNodeId.setText(remoteNode.getId());
                    connected = true;
                } else {
                    connected = false;
                }

                updateConnectionStatus(connected);
            }
        });
    }

    private NodeApi.NodeListener mNodeListener = new NodeApi.NodeListener() {
        @Override
        public void onPeerConnected(Node node) {
            // does not get called on UI thread
            updateConnectionStatus(true);
        }

        @Override
        public void onPeerDisconnected(Node node) {
            // does not get called on UI thread
            updateConnectionStatus(false);
        }
    };

    private void updateConnectionStatus(final boolean connected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(connected) {
                    mConnectionStatus.setText("connected to remote device");
                } else {
                    mConnectionStatus.setText("not connected to remote device");
                    mRemoteNodeId.setText("");
                    mRemoteDisplayName.setText("");
                }
            }
        });
    }
}
