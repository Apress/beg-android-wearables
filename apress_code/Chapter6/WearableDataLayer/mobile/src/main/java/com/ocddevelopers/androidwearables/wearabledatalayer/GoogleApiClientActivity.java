package com.ocddevelopers.androidwearables.wearabledatalayer;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Shows how to establish a connection to GoogleApiClient asynchronously.
 */
public class GoogleApiClientActivity extends ActionBarActivity {
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String ERROR_FRAGMENT_TAG = "errordialog";
    private boolean mResolvingError = false;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_api_client);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(!mResolvingError) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            // you can now use the wearable data layer API
            Toast.makeText(getApplicationContext(), "Connected to GoogleApiClient",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectionSuspended(int cause) {
            // disable any buttons or interface elements that require the wearable API
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (mResolvingError) {
                // Already attempting to resolve an error.
            } else if (connectionResult.hasResolution()) {
                try {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(GoogleApiClientActivity.this,
                            REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                showErrorDialog(connectionResult.getErrorCode());
                mResolvingError = true;
            }
        }
    };

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                this, REQUEST_RESOLVE_ERROR);

        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = ErrorDialogFragment.newInstance(errorDialog,
                new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mResolvingError = false;
            }
        });

        dialogFragment.show(getFragmentManager(), ERROR_FRAGMENT_TAG);
    }
}
