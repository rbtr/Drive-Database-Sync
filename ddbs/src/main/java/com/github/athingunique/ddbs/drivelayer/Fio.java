package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/28/15.
 */


class Fio implements ResultCallback {

    private FioResultsCallback callback;

    public Fio(FioResultsCallback callback) {
        this.callback = callback;
    }

    public void createFile(GoogleApiClient driveClient, DriveFolder driveFolder, String filename) {

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(filename)
                .build();

        driveFolder.createFile(driveClient, metadataChangeSet, null).setResultCallback(this);
    }

    public DriveFile getDriveFileFromMetadata(GoogleApiClient driveClient, Metadata metadata) {

        DriveId driveId = metadata.getDriveId();

        return Drive.DriveApi.getFile(driveClient, driveId);
    }

    public void loadDriveFile(GoogleApiClient driveClient, DriveFile driveFile) {
        driveFile.open(driveClient, DriveFile.MODE_READ_ONLY, null).setResultCallback(this);
    }

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
