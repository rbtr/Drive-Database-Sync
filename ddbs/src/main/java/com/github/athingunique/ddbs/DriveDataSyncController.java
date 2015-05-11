package com.github.athingunique.ddbs;

/*
 * Created by evan on 4/27/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import com.github.athingunique.ddbs.drivelayer.DriveLayer;
import com.github.athingunique.ddbs.drivelayer.FileResultsReadyCallback;
import com.github.athingunique.ddbs.googleapi.DriveApiFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class DriveDataSyncController implements FileResultsReadyCallback {

    public static final int PUT = 0;
    public static final int GET = 1;
    public static final int COMPARE = 2;

    boolean waiting = false;

    DriveLayer mDriveLayer;
    File localDb;
    DriveApi.DriveContentsResult result;

    Queue<Integer> mRequestQueue;

    private CloudDatabaseNewerStatusCallback newerStatusCallback;
    private GoogleApiClient mDriveClient;

    private DriveDataSyncController(final Context context, String dbName, CloudDatabaseNewerStatusCallback newerStatusCallback) {
        mDriveClient = DriveApiFactory.getClient(context, new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        // mDriveLayer.getFile(localDb.getName());
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // Don't care
                    }
                },
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // Resolve
                        // Resolve
                        if (!connectionResult.hasResolution()) {
                            // Show the localized error dialog
                            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
                                    (Activity) context, 0).show();
                            return;
                        }

                        try {
                            connectionResult.startResolutionForResult((Activity) context, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.e("GoogleApiClient", "Exception while starting resolution activity");
                        }
                    }
                }
        );

        mDriveClient.connect();
        mDriveLayer = new DriveLayer(mDriveClient, this);
        localDb = context.getDatabasePath(dbName);
        mRequestQueue = new LinkedList<>();
        this.newerStatusCallback = newerStatusCallback;
    }

    public static DriveDataSyncController get(Context context, String dbName, CloudDatabaseNewerStatusCallback newerStatusCallback) {
        return new DriveDataSyncController(context, dbName, newerStatusCallback);
    }

    public static DriveDataSyncController get(Context context, SQLiteOpenHelper dbHelper, CloudDatabaseNewerStatusCallback newerStatusCallback) {
        return new DriveDataSyncController(context, dbHelper.getDatabaseName(), newerStatusCallback);
    }

    private void queue(int key) {
        mRequestQueue.add(key);
        doQueue();
    }

    private void doQueue() {
        if (!waiting) {
            if (mRequestQueue.size() > 0) {
                waiting = true;
                mDriveLayer.getFile(localDb.getName());
            }
        }
    }

    private int deQueue() {
        if (mRequestQueue.size() > 0) {
            int request = mRequestQueue.poll();
            waiting = false;
            doQueue();
            return request;
        }  else {
            Log.e("Controller", "Queue size 0?...");
            return -1;
        }
    }

    public void pullDbFromCloud() {
        queue(GET);
    }

    public void putDbInCloud() {
        queue(PUT);
    }

    public void isCloudDbNewer() {
        queue(COMPARE);
    }

    public boolean compareCloudLocalNewer(Date cloudDeltaDate) {
        long lastLocalUpdate = localDb.lastModified();
        long lastCloudUpdate = cloudDeltaDate.getTime();

        if (lastLocalUpdate <= 0) {
            return true;
        }

        if (lastCloudUpdate <= 0) {
            return false;
        }

        return lastCloudUpdate > lastLocalUpdate;
    }


    @Override
    public void onMetaDataReceived(Metadata m) {
        if (mRequestQueue.size() > 0) {
            if (mRequestQueue.peek() == COMPARE) {
                mRequestQueue.poll();

                if (compareCloudLocalNewer(m.getModifiedDate())) {
                    newerStatusCallback.cloudNewer();
                } else {
                    newerStatusCallback.localNewer();
                }
            }
        } else {
            Log.e("Controller", "Queue size 0?...");
        }
    }

    @Override
    public void onFileResultsReady(DriveApi.DriveContentsResult result) {
        this.result = result;

        int which = deQueue();

        switch (which) {
            case PUT:
                writeLocalDbToCloudStream(result.getDriveContents().getOutputStream());
                result.getDriveContents().commit(mDriveClient, null);
                break;
            case GET:
                writeCloudStreamToLocalDb(result.getDriveContents().getInputStream());
                break;
        }
        if (waiting) {
            mDriveLayer.getFile(localDb.getName());
        }
    }

    private void fileCopyHelper(InputStream in, OutputStream out) {
        byte[] buffer = new byte[4096];
        int n;
        try {
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.e("IOException", "fileCopyHelper | a stream is null");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // Squash
            }
            try {
                out.close();
            } catch (IOException e) {
                // Squash
            }
        }
    }

    private void writeLocalDbToCloudStream(OutputStream outputStream) {
        InputStream localDbInputStream = null;

        try {

            localDbInputStream = new FileInputStream(localDb);
            fileCopyHelper(localDbInputStream, outputStream);

        } catch (FileNotFoundException e) {

            Log.e("Controller", "Local Db file not found");

        } finally {

            if (localDbInputStream != null) {
                try {
                    localDbInputStream.close();
                } catch (IOException e) {
                    // Squash
                }
            }
        }
    }

    private void writeCloudStreamToLocalDb(InputStream inputStream) {

        OutputStream localDbOutputStream = null;

        try {

            localDbOutputStream = new FileOutputStream(localDb);
            fileCopyHelper(inputStream, localDbOutputStream);

        } catch (FileNotFoundException e) {

            Log.e("Controller", "Local Db file not found");

        } finally {

            if (localDbOutputStream != null) {
                try {
                    localDbOutputStream.close();
                } catch (IOException e) {
                    // Squash
                }
            }
        }
    }
}
