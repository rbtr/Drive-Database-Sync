package com.github.athingunique.ddbs;

/*
 * Created by evan on 4/27/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.athingunique.ddbs.drivelayer.DriveLayer;
import com.github.athingunique.ddbs.drivelayer.FileResultsReadyCallback;
import com.github.athingunique.ddbs.googleapi.DriveApiFactory;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;

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

/**
 * Master controller class for all Drive/Database sync operations.
 * Requests are handled sequentially in a FIFO Queue; subsequent requests are not processed until
 * the pending request returns.
 * This has a slight performance hit but guarantees that the results will be returned in the order
 * that they were requested.
 */
public class DriveSyncController implements FileResultsReadyCallback {

    /**
     * Flag for <b>putting</b> the local SQLite Database in the cloud
     */
    public static final int PUT = 0;

    /**
     * Flag for <b>getting</b> the backup from the cloud
     */
    public static final int GET = 1;

    /**
     * Flag indicating to perform a comparison on the local Database and the cloud backup to attempt
     * to determine which is newer
     */
    public static final int COMPARE = 2;

    /**
     * Flag indicating the current status of the request
     */
    boolean ongoingRequest = false;

    /**
     * Reference to the DriveLayer that provides abstracted access to the Drive AppFolder
     */
    DriveLayer mDriveLayer;

    /**
     * Reference to the local SQLite Database (if it exists)
     */
    File localDb;

    /**
     * Holds the DriveContentsResult of the Request to get the backup DriveFile. Held as a class var
     * for reusability.
     */
    DriveApi.DriveContentsResult result;

    /**
     * The request Queue
     */
    Queue<Integer> mRequestQueue;

    /**
     * Callback to inform of local/cloud newer statuses
     */
    private NewerDatabaseCallback newerStatusCallback;

    private GoogleApiClient mDriveClient;

    /**
     * Constructs a new {@link DriveSyncController}.
     * @param context the Activity Context
     * @param dbName the local SQLite Database name
     * @param newerStatusCallback the callback to notify of local/cloud newer status
     */
    private DriveSyncController(final Context context, String dbName, NewerDatabaseCallback newerStatusCallback) {
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

    /**
     * Retrieves a {@link DriveSyncController} singleton
     * @param context the Activity Context
     * @param dbName the local SQLite Database name
     * @param newerStatusCallback the callback to notify of local/cloud newer status
     * @return a {@link DriveSyncController}
     */
    public static DriveSyncController get(@NonNull Context context, @NonNull String dbName, @Nullable NewerDatabaseCallback newerStatusCallback) {
        return new DriveSyncController(context, dbName, newerStatusCallback);
    }

    /**
     * Retrieves a {@link DriveSyncController} singleton
     * @param context the Activity Context
     * @param dbHelper the local {@link SQLiteOpenHelper}
     * @param newerStatusCallback the callback to notify of local/cloud newer status
     * @return a {@link DriveSyncController}
     */
    public static DriveSyncController get(@NonNull Context context, @NonNull SQLiteOpenHelper dbHelper, @Nullable NewerDatabaseCallback newerStatusCallback) {
        return new DriveSyncController(context, dbHelper.getDatabaseName(), newerStatusCallback);
    }

    private void queue(int key) {
        mRequestQueue.add(key);
        doQueue();
    }

    private void doQueue() {
        if (!ongoingRequest) {
            if (mRequestQueue.size() > 0) {
                ongoingRequest = true;
                mDriveLayer.getFile(localDb.getName());
            }
        }
    }

    private int deQueue() {
        if (mRequestQueue.size() > 0) {
            int request = mRequestQueue.poll();
            ongoingRequest = false;
            doQueue();
            return request;
        }  else {
            Log.e("Controller", "Queue size 0?...");
            return -1;
        }
    }

    /**
     * Queues an asynchronous request to pull the SQLite Database stored in the Drive AppFolder and
     * write it over the local Database
     */
    public void pullDbFromDrive() {
        queue(GET);
    }

    /**
     * Queues an asynchronous request to push the local SQLite Database to Drive, overwriting any
     * instance of the Database currently in the AppFolder
     */
    public void putDbInDrive() {
        queue(PUT);
    }

    /**
     * Queues an asynchronous request to compare the last modified dates of the local and Drive
     * Database files to determine which is newer.
     * Will call back with true if the Drive database is newer and false if the local Database is
     * newer.
     */
    public void isDriveDbNewer() {
        queue(COMPARE);
    }

    /**
     * Performs a comparison of the local and DriveFile Database files to determine which is newer
     * @param driveDeltaDate the data of the last change to the DriveFile Database
     * @return true if the DriveFile is newer, false if the local file is newer
     */
    public boolean compareDriveLocalNewer(Date driveDeltaDate) {
        long lastLocalUpdate = localDb.lastModified();
        long lastDriveUpdate = driveDeltaDate.getTime();

        if (lastLocalUpdate <= 0) {
            return true;
        }

        if (lastDriveUpdate <= 0) {
            return false;
        }

        return lastDriveUpdate > lastLocalUpdate;
    }


    /**
     * Handles received DriveFile Metadata for comparing the newer status of the local and Drive
     * copies of the Database
     * @param m the received Metadata
     */
    @Override
    public void onMetaDataReceived(Metadata m) {
        if (mRequestQueue.size() > 0) {
            if (mRequestQueue.peek() == COMPARE) {
                mRequestQueue.poll();

                if (compareDriveLocalNewer(m.getModifiedDate())) {
                    newerStatusCallback.driveNewer();
                } else {
                    newerStatusCallback.localNewer();
                }
            }
        } else {
            Log.e("Controller", "Queue size 0?...");
        }
    }

    /**
     * Handles the DriveContentsResult returned from the request to get the DriveFile of the
     * Database.
     * @param result the DriveContentsResult representing the Drive copy of the Database
     */
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
        if (ongoingRequest) {
            mDriveLayer.getFile(localDb.getName());
        }
    }

    /**
     * Helper method to copy a file from an InputStream to an OutputStream
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     */
    private void fileCopyHelper(InputStream in, OutputStream out) {
        byte[] buffer = new byte[4096];
        int n;

        // IT SURE WOULD BE NICE IF TRY-WITH-RESOURCES WAS SUPPORTED IN OLDER SDK VERSIONS :(
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

    /**
     * Helper method to write the local SQLite Database to the DriveFile in the AppFolder
     * @param outputStream the OutputStream of the DriveFile to write to
     */
    private void writeLocalDbToCloudStream(OutputStream outputStream) {
        InputStream localDbInputStream = null;

        // NOPE, STILL NO TRY-WITH-RESOURCES :((
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

    /**
     * Helper method to write the DriveFile Database to the local SQLite Database file
     * @param inputStream the InputStream of the DriveFile to read data from
     */
    private void writeCloudStreamToLocalDb(InputStream inputStream) {

        OutputStream localDbOutputStream = null;

        // PLEASE IT WOULD BE SO MUCH NICER :(((  [yes, I know this isn't possible. I'm just complaining]
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
