package com.e13.ddbs_example;

/*
 * Created by evan on 4/28/15.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.Date;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DB_VER = 1;
    private static final String TABLE = "datetable";
    private static final String DATE_COL = "date_col";

    public DbHelper(Context context) {
        super(context, TABLE, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String exec = "CREATE TABLE " + TABLE + " (" + DATE_COL +  " INTEGER);";
        sqLiteDatabase.execSQL(exec);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // no upgrades
    }

    public void putDateInDatabase(Date date) {
        putDateInDatabase(date.getTime());
    }

    public void putDateInDatabase(long now) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement insert = makeInsertStatement(db);

        insert.bindLong(1, now);
        insert.executeInsert();
        insert.clearBindings();
    }

    public Date getDateFromDatabase() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE,
                new String[]{DATE_COL},
                null,
                null,
                null,
                null,
                null
        );

        long dateTime = -1;
        if (cursor.moveToLast()) {
            dateTime = cursor.getLong(0);
        }

        cursor.close();

        return new Date(dateTime);
    }

    private SQLiteStatement makeInsertStatement(SQLiteDatabase db) {
        String statement =  "INSERT OR REPLACE INTO " + TABLE + " (" + DATE_COL + ") VALUES (?);";
        return db.compileStatement(statement);
    }

    // Drop db method
    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
    }
}
