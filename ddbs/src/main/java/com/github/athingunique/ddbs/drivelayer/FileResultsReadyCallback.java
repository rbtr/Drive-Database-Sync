package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;

/**
 * Interface that the {@link com.github.athingunique.ddbs.DriveSyncController} implements by default
 * so that it can receive results of DriveFile requests
 */
public interface FileResultsReadyCallback {
    /**
     * Method called when DriveFile Metadata has been retrieved
     * @param m the DriveFile Metadata retrieved
     */
    void onMetaDataReceived(Metadata m);

    /**
     * Method called when some DriveContentsResult is ready, indicating results of an attempt to
     * get a reference to the Database DriveFile
     * @param result the DriveContentsResult indicating the status of request
     */
    void onFileResultsReady(DriveApi.DriveContentsResult result);

    /**
     * Helper method that returns if the DriveFile should be opened as RW or just R
     * @return the mode to open the DriveFile. True = RW, False = R.
     */
    boolean openModeWriteable();
}
