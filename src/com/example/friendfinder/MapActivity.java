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
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class MapActivity extends Activity implements SendData {

    private String[] params = new String[4];
    private Handler myHandler = new Handler();
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        context = this;

        GpsListener listener = new GpsListener();
        LocationManager locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, listener);
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            LocationSender task = new LocationSender(location);
            Log.d("-----------", "On Location changed");
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
            params[3] = "elixir";
            Log.d("-----------", "In thread!!");
            DownloadSession mySession = new DownloadSession();
            mySession.delegate = (SendData) context;
            mySession.execute(params);
        }
    }

    public void getData(String responseString) {
        Toast.makeText(this, responseString, Toast.LENGTH_LONG).show();
    }

    private static class DownloadSession extends AsyncTask<String, Integer, String> {
        public SendData delegate;

        @Override
        protected String doInBackground(String... params) {
            String responseString = null;
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
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseString = client.execute(post, responseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            delegate.getData(result);
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
