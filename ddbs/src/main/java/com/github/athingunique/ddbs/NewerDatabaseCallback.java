package com.github.athingunique.ddbs;

/*
 * Created by evan on 4/28/15.
 */

/**
 * Interface that the {@link DriveSyncController} calls back through alert the implementer that
 * either the Drive version or the local version of the SQLite Database is newer
 */
public interface NewerDatabaseCallback {
    /**
     * Method called when the Drive version is newer
     */
    void driveNewer();

    /**
     * Method called when the local version is newer
     */
    void localNewer();
}
