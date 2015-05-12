package com.github.athingunique.ddbs.googleapi;

/*
 * Created by evan on 4/28/15.
 */

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * A Factory class to make creating a {@link GoogleApiClient} painless.
 */
public abstract class DriveApiFactory {

    /**
     * Factory method that creates and returns a GoogleApiClient
     * @param context the Activity Context
     * @param connectionCallbacks the {@link GoogleApiClient.ConnectionCallbacks}
     * @param connectionFailedListener the {@link GoogleApiClient.OnConnectionFailedListener}
     * @return a fully constructed, Drive equipped GoogleApiClient
     */
    public static GoogleApiClient getClient(Context context,
                                            GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                                            GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        return buildDriveClient(context, connectionCallbacks, connectionFailedListener);
    }

    /**
     * GoogleApiClient builder method. This is called by the Factory method {@link #getClient(Context, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener)}.
     * That might seem redundant, but it's for future-proofing the API.
     * @param context the Activity Context
     * @param connectionCallbacks the {@link GoogleApiClient.ConnectionCallbacks}
     * @param connectionFailedListener the {@link GoogleApiClient.OnConnectionFailedListener}
     * @return a fully constructed, Drive equipped GoogleApiClient
     */
    private static GoogleApiClient buildDriveClient(Context context,
                                                    final GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                                                    final GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        return new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d("DriveApiClient", "Connected");
                        if (connectionCallbacks != null) {
                            connectionCallbacks.onConnected(bundle);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("DriveApiClient", "Suspended");
                        if (connectionCallbacks != null) {
                            connectionCallbacks.onConnectionSuspended(i);
                        }
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d("DriveApiClient", "Connection failed. Cause: " + connectionResult.toString());
                        if (connectionFailedListener != null) {
                            connectionFailedListener.onConnectionFailed(connectionResult);
                        }
                    }
                })
                .build();
    }
}

