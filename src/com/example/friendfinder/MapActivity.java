package com.example.friendfinder;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends Activity implements DataReceiver {

    private String[] params = new String[4];
    private Handler myHandler = new Handler();
    private Context context;

    private GoogleMap map;

    private String username = "elixir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        context = this;
        username = getIntent().getStringExtra("username");

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        GpsListener listener = new GpsListener();
        LocationManager locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, listener);
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
            map.clear();
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            LocationSender task = new LocationSender(location);
            myHandler.post(task);
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

    private class LocationSender implements Runnable {
        Location location;

        public LocationSender(Location location_) {
            location = location_;
        }

        @Override
        public void run() {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            params[0] = timeStamp;
            params[1] = String.valueOf(location.getLatitude());
            params[2] = String.valueOf(location.getLongitude());
            params[3] = username;
            DownloadSession mySession = new DownloadSession();
            mySession.delegate = (DataReceiver) context;
            mySession.execute(params);
        }
    }

    @Override
    public void receive(ServerResponse response) {
        if (response != null) {
            Toast.makeText(context, "Friends:\n" + response.getMessage(), Toast.LENGTH_LONG).show();
            displayFriends(response.getMessage());
        }
    }

    private void displayFriends(String responseString) {
        String lines[] = responseString.split("\\r?\\n");

        for (String line : lines) {
            String params[] = line.split(",");
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(params[1]), Double.valueOf(params[2])))
                    .title(params[0])
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    private static class DownloadSession extends AsyncTask<String, Integer, ServerResponse> {
        public DataReceiver delegate;

        @Override
        protected ServerResponse doInBackground(String... params) {
            ServerResponse serverResponse = null;
            String[] parameters = params;

            String time = parameters[0];
            String latitude = parameters[1];
            String longitude = parameters[2];
            String id = parameters[3];

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://mpss.csce.uark.edu/~devan/update.php");

            List<NameValuePair> value = new LinkedList<NameValuePair>();
            value.add(new BasicNameValuePair("time", time));
            value.add(new BasicNameValuePair("latitude", latitude));
            value.add(new BasicNameValuePair("longitude", longitude));
            value.add(new BasicNameValuePair("id", id));

            try {
                post.setEntity(new UrlEncodedFormEntity(value));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                HttpResponse httpResponse = client.execute(post);
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
                String responseString = reader.readLine();
                serverResponse = new ServerResponse(httpResponse.getStatusLine().getStatusCode(), responseString);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(ServerResponse response) {
            super.onPostExecute(response);
            delegate.receive(response);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }

    }

}
