package com.example.friendfinder;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.location.LocationProvider;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
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

    static final String TAG = "MAP";
    static boolean ACTIVE = false;

    private Context context;
    private GoogleMap map;

    private LocationManager locationManager;
    private GpsListener listener;
    private Location userPosition;
    public static CameraPosition lastMapLocation;
    public SharedPreferences preferences;
    public LocationDirectoryOpenHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACTIVE = true;
        Log.d(TAG, "On Create");

        setContentView(R.layout.activity_map);
        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* Create android database for storing friend locations
         */
        databaseHelper = new LocationDirectoryOpenHelper(context);

        /* Check if GPS is enabled, if not
        prompt the user the enable GPS
         */
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showEnableGpsDialog();
        }
        if (!manager.isProviderEnabled( LocationManager.GPS_PROVIDER )){
            if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
            }
        }

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        /* Get user last known location from SharedPreferences
        Set Camera's initial position to this location
         */
        long initial_lat = preferences.getLong(LATITUDE, (long) 39.254119);
        long initial_lng = preferences.getLong(LONGITUDE, (long) -76.712616);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initial_lat, initial_lng), 10));

        listener = new GpsListener();
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, listener);
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
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(USERNAME, null);
                editor.commit();

                Intent i = new Intent(context, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
    }
    @Override
    protected void onPause() {
//        ACTIVE = false;
        super.onPause();
        Log.d(TAG, "onPause");
        locationManager.removeUpdates(listener);
        lastMapLocation = map.getCameraPosition();
    }

    public void requestFriendLocations(Location location) {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        String[] params = new String[4];

        params[0] = timeStamp;
        params[1] = String.valueOf(location.getLatitude());
        params[2] = String.valueOf(location.getLongitude());
        params[3] = preferences.getString(USERNAME,"");

        UpdateSession mySession = new UpdateSession();
        mySession.delegate = (DataReceiver) context;
        mySession.execute(params);
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            userPosition = location;
            requestFriendLocations(location);

            /* Store current location of user
            for future reference
             */
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

    @Override
    public void receive(ServerResponse response) {

        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String responseString = response.getMessage();
        String lines[] = responseString.split("\\r?\\n");

        for (String line : lines){
            if (line.isEmpty())
                break;
            String params[] = line.split(",");
            Log.d(TAG, params.length + " " + line);
            database.execSQL("INSERT INTO friend_locations (username, latitude, longitude) VALUES ('" +
                    params[0] + "', " +
                    params[1] + ", " +
                    params[2] + ")");
        }

        displayUserPosition();
        if (response != null)
            displayFriends();
    }

    private void displayUserPosition() {
        double latitude = userPosition.getLatitude();
        double longitude = userPosition.getLongitude();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(preferences.getString(USERNAME,""))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_self_marker)));
    }

    private void displayFriends() {

        String uname, lat, lng;
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String sql = "SELECT username, latitude, longitude FROM friend_locations";
        Cursor result = database.rawQuery(sql, null);

        result.moveToFirst();
        while (result.isAfterLast() == false) {
            uname = result.getString(0);
            lat = result.getString(1);
            lng = result.getString(2);
            Log.d(TAG, uname + " " + lat + " " + lng);

            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(lat), Double.valueOf(lng)))
                    .title(uname)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_friend_marker)));
            result.moveToNext();
        }
        result.close();
    }

    protected void onNewIntent(Intent intent)
    {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        super.onNewIntent(intent);
        setIntent(intent);

        String incoming_username = intent.getStringExtra(USERNAME);
        String latitude = intent.getStringExtra(LATITUDE);
        String longitude = intent.getStringExtra(LONGITUDE);
        Toast.makeText(this, "Notification from " + incoming_username + " at " + latitude + ", " + longitude, Toast.LENGTH_LONG).show();

        database.execSQL("INSERT INTO friend_locations (username, latitude, longitude) VALUES ('" +
                incoming_username + "', " +
                latitude + ", " +
                longitude + ")");
        displayUserPosition();
        displayFriends();
        /*put this new incoming user in database and make a call to displayUser and displayFriends
         */
//        username = getIntent().getStringExtra(USERNAME);
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(preferences.getLong(LATITUDE, 0),preferences.getLong(LONGITUDE, 0)), 16));
    }

    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
        Toast.makeText(this, "onStop MapActivity", Toast.LENGTH_LONG).show();
        locationManager.removeUpdates(listener);
        lastMapLocation = map.getCameraPosition();
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        locationManager.removeUpdates(listener);
        lastMapLocation = map.getCameraPosition();

    }
}
