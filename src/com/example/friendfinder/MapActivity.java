package com.example.friendfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.friendfinder.Global.*;

public class MapActivity extends Activity implements DataReceiver {

    private static final String TAG = "MAP";
    private static final long UPDATE_INTERVAL = 20 * 1000;
    public static CameraPosition lastMapLocation;

    private Context context;
    private GoogleMap map;

    private LocationManager locationManager;
    private GpsListener listener;
    private Location userPosition;
    public SharedPreferences preferences;
    public LocationDirectory databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;

        /* Initialize the database to store the friend locations temporarily till we
         * get updated locations of them from the server
         */
        databaseHelper = new LocationDirectory(context);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        listener = new GpsListener();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        /* Get user last known location from SharedPreferences and set camera's initial
         * position to that location. Currently using some default location for the first
         * time when a new user is signing onto the device.
         */
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long initial_lat = preferences.getLong(LATITUDE, (long) 39.254119);
        long initial_lng = preferences.getLong(LONGITUDE, (long) -76.712616);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initial_lat, initial_lng), 10));

    }

    /* On start of the map activity request for GPS updates
     */
    protected void onStart() {
        super.onStart();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showEnableGpsDialog();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0, listener);
    }

    /* Unregister GPS updates onPause and store the camera position
     */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(listener);
        lastMapLocation = map.getCameraPosition();
    }

    /* Dialog which prompts the user to enable GPS if it is turned off
     */
    private void showEnableGpsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to enable?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            userPosition = location;
            requestFriendLocations(location);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(LATITUDE, (long) location.getLatitude());
            editor.putLong(LONGITUDE, (long) location.getLongitude());
            editor.commit();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    /* On receiving updates from the GPS, update users location as well poll for friends who are
     * nearby him. The radius is decided at the server side.
     */
    public void requestFriendLocations(Location location) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String[] params = new String[4];

        params[0] = timeStamp;
        params[1] = String.valueOf(location.getLatitude());
        params[2] = String.valueOf(location.getLongitude());
        params[3] = preferences.getString(USERNAME, "");

        UpdateSession mySession = new UpdateSession();
        mySession.delegate = (DataReceiver) context;
        mySession.execute(params);
    }

    /* Callback which is called once the response from the server is received.
     * On every update clear the current stale entries of friend locations from database
     * and put the new ones
     */
    @Override
    public void receive(ServerResponse response) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String responseString = response.getMessage();
        String lines[] = responseString.split("\\r?\\n");

        database.execSQL("DELETE FROM friend_locations;");
        for (String line : lines) {
            if (line.isEmpty())
                break;
            String params[] = line.split(",");
            Log.d(TAG, line);
            if (params.length != 3)
                break;
            database.execSQL("INSERT INTO friend_locations (username, latitude, longitude) VALUES ('" +
                    params[0] + "', " +
                    params[1] + ", " +
                    params[2] + ")");
        }

        displayUserPosition();
        displayFriends();
    }

    /* Display the user's position with a different marker from his friends
     */
    private void displayUserPosition() {
        double latitude = userPosition.getLatitude();
        double longitude = userPosition.getLongitude();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(preferences.getString(USERNAME, ""))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_self_marker)));
    }

    /* Display the friends of the user form database
     */
    private void displayFriends() {
        String uname;
        double lat, lng;
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String sql = "SELECT username, latitude, longitude FROM friend_locations";
        Cursor result = database.rawQuery(sql, null);

        result.moveToFirst();
        while (!result.isAfterLast()) {
            uname = result.getString(0);
            lat = result.getDouble(1);
            lng = result.getDouble(2);

            map.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lng))
                    .title(uname)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_friend_marker)));
            result.moveToNext();
        }
        result.close();
    }

    protected void onNewIntent(Intent intent) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        super.onNewIntent(intent);
        setIntent(intent);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showEnableGpsDialog();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 7000, 0, listener);

        String incoming_username = preferences.getString("incoming_user", "");
        String latitude = preferences.getString("incoming_lat", "");
        String longitude = preferences.getString("incoming_lng", "");

        database.execSQL("INSERT INTO friend_locations (username, latitude, longitude) VALUES ('" +
                incoming_username + "', " +
                latitude + ", " +
                longitude + ")");
        displayUserPosition();
        displayFriends();
    }

    protected void onDestroy() {
        super.onDestroy();
        clearUserSession();
    }

    /* Clear the user session and his friends from the database
     */
    private void clearUserSession() {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USERNAME, null);
        editor.commit();
        database.execSQL("DELETE FROM friend_locations;");
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    /* Menu options for the current screen on the action bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_activity_menu, menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                clearUserSession();
                Intent i = new Intent(context, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
