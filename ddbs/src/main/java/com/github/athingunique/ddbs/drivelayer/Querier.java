package com.github.athingunique.ddbs.drivelayer;

/*
 * Created by evan on 4/27/15.
 */

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

class Querier implements ResultCallback<DriveApi.MetadataBufferResult> {

    private final QuerierResultCallback callback;

    public Querier(QuerierResultCallback callback) {
        this.callback = callback;
    }

    public void findFile(GoogleApiClient driveClient, DriveFolder folder, String filename) {
        // Make filename filter
        Filter filenameFilter = Filters.eq(SearchableField.TITLE, filename);

        // Build query with filter
        Query query = new Query.Builder()
                .addFilter(filenameFilter)
                .build();

        // Execute query on folder
        folder.queryChildren(driveClient, query).setResultCallback(this);
    }

    @Override
    public void onResult(DriveApi.MetadataBufferResult result) {
        if (!result.getStatus().isSuccess()) {
            Log.e("Querier", "Problem while retrieving files");
            return;
        }

        // Get the metadata buffer
        MetadataBuffer buffer = result.getMetadataBuffer();

        // Check for file dne
        if (buffer.getCount() == 0) {
            callback.onNoQuerierResult();
        }

        // Get the metadata
        for (Metadata m : buffer) {
            if (m.isInAppFolder() && !m.isTrashed()) {
                callback.onQuerierResult(m);
                break;
            }
        }
    }
}
