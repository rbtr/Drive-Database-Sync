package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;


/**
 * This class is a worker class that handles the explicit details of Drive File Operations.
 * Used by the DriveLayer as slightly abstracted access to the Drive AppFolder and DriveFiles
 */

class DriveFio implements ResultCallback {

    /**
     * The callback to notify about results
     */
    private DriveFioResultsCallback callback;

    /**
     * Creates a DriveFio object
     * @param callback the {@link DriveFioResultsCallback} to build this DriveFio with
     */
    public DriveFio(DriveFioResultsCallback callback) {
        this.callback = callback;
    }

    /**
     * Work method to create a DriveFile within the passed params. Sets the result callback to this,
     * which calls {@link #onResult(Result)}
     * @param driveClient the {@link GoogleApiClient} to use for this Drive request
     * @param driveFolder the {@link DriveFolder} to create the DriveFile in
     * @param filename the filename for the to-be-created {@link DriveFile}
     */
    public void createFile(GoogleApiClient driveClient, DriveFolder driveFolder, String filename) {

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(filename)
                .build();

        driveFolder.createFile(driveClient, metadataChangeSet, null).setResultCallback(this);
    }

    /**
     * Work method that retrieves a DriveFile from some passed Metadata.
     * @param driveClient the {@link GoogleApiClient} to use for this Drive request
     * @param metadata the Metadata to pull the {@link DriveFile} out of
     * @return the {@link DriveFile} extracted from the passed Metadata
     */
    public DriveFile getDriveFileFromMetadata(GoogleApiClient driveClient, Metadata metadata) {

        DriveId driveId = metadata.getDriveId();

        return Drive.DriveApi.getFile(driveClient, driveId);
    }

    /**
     * Work method that attempts to open a {@link DriveFile} for use. Sets the result callback to
     * this, which calls {@link #onResult(Result)}
     * @param driveClient the {@link GoogleApiClient} to use for this Drive request
     * @param driveFile the {@link DriveFile} to request to open
     */
    public void loadDriveFile(GoogleApiClient driveClient, DriveFile driveFile, boolean writeable) {
        int mode = writeable ? DriveFile.MODE_WRITE_ONLY : DriveFile.MODE_READ_ONLY;
        driveFile.open(driveClient, mode, null).setResultCallback(this);
    }

    /**
     * Results handler for the asynchronous work methods in this class. Triages the Result and then
     * passes it up to the appropriate callback based on the type.
     * @param result the returned result.
     */
    @Override
    public void onResult(Result result) {
        if (result instanceof DriveApi.DriveContentsResult) {
            callback.onFioResult((DriveApi.DriveContentsResult) result);
        }

        if (result instanceof DriveFolder.DriveFileResult) {
            callback.onFileCreatedResult((DriveFolder.DriveFileResult) result);
        }
    }
}
