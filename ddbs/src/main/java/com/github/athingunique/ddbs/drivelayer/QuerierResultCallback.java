package com.github.athingunique.ddbs.drivelayer;

import com.google.android.gms.drive.Metadata;

/*
 * Created by evan on 4/28/15.
 */

/**
 * Interface that the {@link Querier} reports query results to
 */

interface QuerierResultCallback {
    /**
     * Method called when the query returns no results
     */
    void onNoQuerierResult();

    /**
     * Method called when the query returns results
     * @param m the {@link Metadata} of the result
     */
    void onQuerierResult(Metadata m);
}
