package com.e13.ddbs_example;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.e13.ddbs.CloudDatabaseNewerStatusCallback;
import com.e13.ddbs.DriveDataSyncController;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends ActionBarActivity implements CloudDatabaseNewerStatusCallback {

    /**
     * Pressing this button triggers a comparison to determine if the cloud db is newer than the
     * local db. Results are delivered to the CloudDatabaseNewerStatusCallback.
     */
    @InjectView(R.id.button_find_newer)
    Button mButtonFindNewer;

    @OnClick(R.id.button_find_newer)
    public void findNewer() {
        mSyncController.isCloudDbNewer();
    }


    /**
     * Pressing this button triggers a pull from the cloud db that overwrites the local db.
     */
    @InjectView(R.id.button_pull_cloud)
    Button mButtonPullCloud;

    @OnClick(R.id.button_pull_cloud)
    public void pullCloud() {
        mSyncController.pullDbFromCloud();
    }

    /**
     * Pressing this button triggers a push of the local db to the cloud db.
     */
    @InjectView(R.id.button_push_local)
    Button mButtonPushLocal;

    @OnClick(R.id.button_push_local)
    public void pushLocal() {
        mSyncController.putDbInCloud();
    }

    /**
     * Pressing this button triggers a poll of the local database to retrieve the newest Date
     * stored in it. Occurs synchronously.
     */
    @InjectView(R.id.button_show_latest)
    Button mButtonShowLatest;

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

    private DbHelper mDbHelper;
    private DriveDataSyncController mSyncController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new DbHelper(this);
        mSyncController = DriveDataSyncController.get(this, mDbHelper, this);

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
    public void cloudNewer() {
        mSyncController.pullDbFromCloud();
        toaster("Cloud newer");
    }

    @Override
    public void localNewer() {
        mSyncController.putDbInCloud();
        toaster("Local newer");
    }

    private void toaster(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
