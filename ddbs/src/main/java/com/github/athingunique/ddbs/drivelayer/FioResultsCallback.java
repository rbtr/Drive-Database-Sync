package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */

import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;

interface FioResultsCallback {
    void onFioResult(DriveApi.DriveContentsResult result);
    void onFileCreatedResult(DriveFolder.DriveFileResult result);
}
