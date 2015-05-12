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

/**
 * This class is a worker class that is used to query Drive for data
 */

class Querier implements ResultCallback<DriveApi.MetadataBufferResult> {

    /**
     * The callback to deliver query results to
     */
    private final QuerierResultCallback callback;

    /**
     * Constructs a new Querier
     * @param callback the {@link QuerierResultCallback}
     */
    public Querier(QuerierResultCallback callback) {
        this.callback = callback;
    }


    /**
     * Asynchronously queries Drive within the passed params to find a DriveFile
     * @param driveClient the {@link GoogleApiClient} to use for this Drive request
     * @param folder the {@link DriveFolder} to query within
     * @param filename the filename to query for
     */
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

    /**
     * Handles the result of the query. If the results are nonzero, passes up the {@link Metadata}
     * out of the result one at a time through {@link QuerierResultCallback#onQuerierResult(Metadata)}
     * with the {@link QuerierResultCallback} registered in ths {@link Querier}. If the results are
     * null, calls {@link QuerierResultCallback#onNoQuerierResult()}
     * @param result the result of the query
     */
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
            // Call null result callback
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
