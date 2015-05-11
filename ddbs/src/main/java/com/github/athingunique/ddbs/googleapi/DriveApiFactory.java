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


public abstract class DriveApiFactory {

    public static GoogleApiClient getClient(Context context,
                                            GoogleApiClient.ConnectionCallbacks connectionCallbacks,
                                            GoogleApiClient.OnConnectionFailedListener connectionFailedListener)
            throws IllegalArgumentException {
        return buildDriveClient(context, connectionCallbacks, connectionFailedListener);
    }

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

