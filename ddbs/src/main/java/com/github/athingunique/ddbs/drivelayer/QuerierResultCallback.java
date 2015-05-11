package com.github.athingunique.ddbs.drivelayer;

import com.google.android.gms.drive.Metadata;

/*
 * Created by evan on 4/28/15.
 */

interface QuerierResultCallback {
    void onNoQuerierResult();
    void onQuerierResult(Metadata m);
}
