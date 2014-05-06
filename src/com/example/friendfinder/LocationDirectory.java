package com.example.friendfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.friendfinder.Global.LATITUDE;
import static com.example.friendfinder.Global.LONGITUDE;
import static com.example.friendfinder.Global.USERNAME;

public class LocationDirectory extends SQLiteOpenHelper {

    private static final String TAG = "LOCATION_HELPER";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "friend_locations";
    public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS " +
            DATABASE_NAME + " (" +
            USERNAME + " VARCHAR(32), " +
            LATITUDE + " TEXT," +
            LONGITUDE + " TEXT);";

    public LocationDirectory(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Database to store the friend locations temporarily while the device does not get
     * an updated list from the server.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
