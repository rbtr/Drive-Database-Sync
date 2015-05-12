package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;

/**
 * Interface that the {@link DriveLayer} implements so that it can receive the results of certain
 * Drive File I/O requests
 */

interface DriveFioResultsCallback {

    /**
     * Method called when a DriveContentsResult is returned, usually as a result of querying the
     * AppFolder for a Database DriveFile
     * @param result the DriveContentsResult indicating the request status
     */
    void onFioResult(DriveApi.DriveContentsResult result);

    /**
     * Method called when a DriveFile creation request finishes
     * @param result the DriveFileResult indicating the exit status of the request
     */
    void onFileCreatedResult(DriveFolder.DriveFileResult result);
}
