package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;

public interface FileResultsReadyCallback {
    void onMetaDataReceived(Metadata m);
    void onFileResultsReady(DriveApi.DriveContentsResult result);
}
