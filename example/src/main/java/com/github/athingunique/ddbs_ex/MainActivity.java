package com.github.athingunique.ddbs_ex;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.athingunique.ddbs.NewerDatabaseCallback;
import com.github.athingunique.ddbs.DriveSyncController;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A simple Activity class that displays a list of some data in a local SQLite Database for
 * demonstration purposes
 */
public class MainActivity extends AppCompatActivity implements NewerDatabaseCallback {

    @InjectView(R.id.button_find_newer)
    Button mButtonFindNewer;

    /**
     * Pressing this button triggers a comparison to determine if the cloud db is newer than the
     * local db. Results are delivered to the NewerDatabaseCallback.
     */
    @OnClick(R.id.button_find_newer)
    public void findNewer() {
        mSyncController.isDriveDbNewer();
    }


    @InjectView(R.id.button_pull_cloud)
    Button mButtonPullCloud;

    /**
     * Pressing this button triggers a pull from the cloud db that overwrites the local db.
     */
    @OnClick(R.id.button_pull_cloud)
    public void pullCloud() {
        mSyncController.pullDbFromDrive();
    }


    @InjectView(R.id.button_push_local)
    Button mButtonPushLocal;

    /**
     * Pressing this button triggers a push of the local db to the cloud db.
     */
    @OnClick(R.id.button_push_local)
    public void pushLocal() {
        mSyncController.putDbInDrive();
    }


    @InjectView(R.id.button_show_latest)
    Button mButtonShowLatest;

    /**
     * Pressing this button triggers a poll of the local database to retrieve the newest Date
     * stored in it. Occurs synchronously.
     */
    @OnClick(R.id.button_show_latest)
    public void pollLatest() {
        Date date = mDbHelper.getDateFromDatabase();

        TextView tv = new TextView(this);
        tv.setText(date.toString());

        mUpdateContainer.addView(tv);
    }

    /**
     * Pressing this button puts the current date-time in to the local database.
     */
    @InjectView(R.id.button_put_now)
    Button mButtonPutNow;

    @OnClick(R.id.button_put_now)
    public void putNow() {
        Date now = new Date();
        mDbHelper.putDateInDatabase(now);
    }

    /**
     * The scrollview container that displays the textviews of date entries.
     */
    @InjectView(R.id.linearlayout_update_container)
    LinearLayout mUpdateContainer;

    /**
     * Reference to the demo SQLiteOpenHelper
     */
    private DbHelper mDbHelper;

    /**
     * Reference to the library DriveSyncController that handles interfacing the local SQLite DB and
     * the Drive backup.
     */
    private DriveSyncController mSyncController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the SQLiteOpenHelper
        mDbHelper = new DbHelper(this);

        // Init the ddbs DriveSyncController
        mSyncController = DriveSyncController.get(this, mDbHelper, this);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_rm_local_db:
                mDbHelper.clearDatabase();
                return true;
        }

        return false;
    }

    @Override
    public void driveNewer() {
        mSyncController.pullDbFromDrive();
        toaster("Cloud newer");
    }

    @Override
    public void localNewer() {
        mSyncController.putDbInDrive();
        toaster("Local newer");
    }

    private void toaster(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
