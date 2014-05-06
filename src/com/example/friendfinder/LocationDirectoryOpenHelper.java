package com.example.friendfinder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.friendfinder.Global.LATITUDE;
import static com.example.friendfinder.Global.LONGITUDE;
import static com.example.friendfinder.Global.USERNAME;

/**
 * Created by shashank on 4/30/14.
 */
public class LocationDirectoryOpenHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "friend_locations";
    public static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_NAME + " (" +
                    USERNAME + " VARCHAR(32), " +
                    LATITUDE + " TEXT," +
                    LONGITUDE + " TEXT);";
    private static final String TAG = "LOCATION_HELPER";

    public LocationDirectoryOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "in LocationHelper's Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Log.d(TAG, "creating database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
