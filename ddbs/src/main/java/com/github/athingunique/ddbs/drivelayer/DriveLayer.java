package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/27/15.
 */

import android.util.Log;

public class DriveLayer implements FioResultsCallback, QuerierResultCallback {

    private String filename;
    private GoogleApiClient mDriveClient;
    private FileResultsReadyCallback callback;

    public DriveLayer(GoogleApiClient driveClient, FileResultsReadyCallback callback) {
        this.mDriveClient = driveClient;
        this.callback = callback;
    }

    private DriveFolder getAppFolder() {
        return Drive.DriveApi.getAppFolder(mDriveClient);
    }

    public void getFile(String filename) {
        this.filename = filename;
        new Querier(this).findFile(mDriveClient, getAppFolder(), filename);
    }

    @Override
    public void onNoQuerierResult() {
        // File not found, create it
        new Fio(this).createFile(mDriveClient, getAppFolder(), filename);
    }

    @Override
    public void onQuerierResult(Metadata m) {
        callback.onMetaDataReceived(m);

        Fio fio = new Fio(this);
        DriveFile driveFile = fio.getDriveFileFromMetadata(mDriveClient, m);
        fio.loadDriveFile(mDriveClient, driveFile);
    }

    @Override
    public void onFioResult(DriveApi.DriveContentsResult result) {
        callback.onFileResultsReady(result);
    }

    @Override
    public void onFileCreatedResult(DriveFolder.DriveFileResult result) {
        if (!result.getStatus().isSuccess()) {
            Log.e("DriveLayer", "Error creating file");
        }

        new Fio(this).loadDriveFile(mDriveClient, result.getDriveFile());
    }
}
