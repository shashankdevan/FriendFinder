package com.example.friendfinder;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.friendfinder.Global.USERNAME;

public class MapActivity extends Activity implements DataReceiver {

    static final String TAG = "MAP";

    private Context context;
    private GoogleMap map;
    private String username = null;

    private LocationManager locationManager;
    private GpsListener listener;
    private Location userPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        username = getIntent().getStringExtra(USERNAME);
        context = this;

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        listener = new GpsListener();
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(listener);
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            userPosition = location;
            requestFriendLocations(location);
        }

        private void requestFriendLocations(Location location) {
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
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
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(params[1]), Double.valueOf(params[2])))
                    .title(params[0])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

}
