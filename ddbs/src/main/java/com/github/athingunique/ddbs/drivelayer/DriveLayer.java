package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/27/15.
 */

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;

/**
 * This class acts as a communication layer providing abstracted access to the Drive AppFolder
 */

public class DriveLayer implements DriveFioResultsCallback, QuerierResultCallback {

    /**
     * The remote DriveFile name
     */
    private String driveFileName;

    /**
     * The GoogleApiClient to use to access Drive
     */
    private GoogleApiClient mDriveClient;

    /**
     * The callback to notify with FileResults
     */
    private FileResultsReadyCallback callback;

    /**
     * Constructs a new DriveLayer object
     * @param driveClient the {@link GoogleApiClient}
     * @param callback the {@link FileResultsReadyCallback}
     */
    public DriveLayer(GoogleApiClient driveClient, FileResultsReadyCallback callback) {
        this.mDriveClient = driveClient;
        this.callback = callback;
    }

    /**
     * Flag indicating to log debug messages
     */
    private static boolean debug;

    /**
     * Method to set the debug flag
     * @param debug the debug status
     * @return this, for chaining
     */
    public DriveLayer setDebug(boolean debug) {
        DriveLayer.debug = debug;
        return this;
    }

    /**
     * Helper method that retrieves the application's AppFolder
     * @return the DriveFolder representing the AppFolder
     */
    private DriveFolder getAppFolder() {
        if (debug) {
            Log.d("DriveLayer", "Getting AppFolder");
        }
        return Drive.DriveApi.getAppFolder(mDriveClient);
    }

    /**
     * Helper method that asynchronously retrieves the remote DriveFile with the passed name (if it
     * exists)
     * @param driveFileName the filename to query
     */
    public void getFile(String driveFileName) {
        if (debug) {
            Log.d("DriveLayer", "Getting File");
            Log.d("Filename", driveFileName);
        }
        this.driveFileName = driveFileName;
        new Querier(this).findFile(mDriveClient, getAppFolder(), driveFileName);
    }

    /**
     * {@link QuerierResultCallback} that creates a DriveFile with the queried name, if it is not
     * found.
     */
    @Override
    public void onNoQuerierResult() {
        if (debug) {
            Log.d("DriveLayer", "No Query results");
        }
        // File not found, create it
        new DriveFio(this).createFile(mDriveClient, getAppFolder(), driveFileName);
    }

    /**
     * {@link QuerierResultCallback} called when a DriveFile with the queried filename is found
     * @param m the {@link Metadata} for the found DriveFile
     */
    @Override
    public void onQuerierResult(Metadata m) {
        if (debug) {
            Log.d("DriveLayer", "Got Query results");
        }

        callback.onMetaDataReceived(m);

        DriveFio driveFio = new DriveFio(this);
        DriveFile driveFile = driveFio.getDriveFileFromMetadata(mDriveClient, m);
        driveFio.loadDriveFile(mDriveClient, driveFile, callback.openModeWriteable());
    }

    /**
     * {@link DriveFioResultsCallback} called when a DriveFile I/O operation returns a
     * DriveContentResult. Merely passes the Result up to the {@link FileResultsReadyCallback}
     * registered in this {@link DriveLayer}.
     * @param result the DriveContentResult
     */
    @Override
    public void onFioResult(DriveApi.DriveContentsResult result) {
        if (debug) {
            Log.d("DriveLayer", "Got Fio result");
        }
        callback.onFileResultsReady(result);
    }

    /**
     * {@link DriveFioResultsCallback} called when the DriveFile with the queried name is finished
     * being created. If the result was not a success, logs that. Regardless, attempts to move
     * forward with whatever request required that the DriveFile be created be attempting to load
     * a reference to the DriveFile
     * @param result the DriveFileResult indicating the exit status of the DriveFile creation
     *               operation
     */
    @Override
    public void onFileCreatedResult(DriveFolder.DriveFileResult result) {
        if (debug) {
            Log.d("DriveLayer", "File created");
        }

        if (!result.getStatus().isSuccess()) {
            Log.e("DriveLayer", "Error creating file");
        }

        new DriveFio(this).loadDriveFile(mDriveClient, result.getDriveFile(), callback.openModeWriteable());
    }
}
