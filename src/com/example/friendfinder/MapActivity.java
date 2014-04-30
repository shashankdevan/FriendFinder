package com.example.friendfinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private static String username = null;

    private LocationManager locationManager;
    private GpsListener listener;
    private Location userPosition;
    public static CameraPosition lastMapLocation;
    public SharedPreferences preferences;

//    public static String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ACTIVE = true;
        Log.d(TAG, "On Create");

        setContentView(R.layout.activity_map);
        username = getIntent().getStringExtra(USERNAME);
        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

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
        ACTIVE = false;
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
        params[3] = username;

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
//            editor.putInt(ZOOM, )
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
        displayUserPosition();
        if (response != null)
            displayFriends(response.getMessage());
    }

    private void displayUserPosition() {
        double latitude = userPosition.getLatitude();
        double longitude = userPosition.getLongitude();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(username)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void displayFriends(String responseString) {
        String lines[] = responseString.split("\\r?\\n");

        for (String line : lines) {
            if (line.isEmpty())
                break;
            String params[] = line.split(",");
            Log.d(TAG, params.length + " " + line);
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(params[1]), Double.valueOf(params[2])))
                    .title(params[0])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        Toast.makeText(this, "Updating map after notification", Toast.LENGTH_LONG).show();

        username = getIntent().getStringExtra(USERNAME);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastMapLocation.target.latitude,lastMapLocation.target.longitude), 16));
    }

    protected void onStop(){
        super.onStop();
        //code to kill the LoginActivity when you press BACK button here
        Log.d(TAG, "onStop");
        Toast.makeText(this, "onStop MapActivity", Toast.LENGTH_LONG).show();
        lastMapLocation = map.getCameraPosition();
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }
}
