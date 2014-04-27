package com.example.friendfinder;

import android.content.Context;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static com.example.friendfinder.Global.REGISTER_URL;

public class BackendServer {

    static final String TAG = "SERVER";

    static ServerResponse register(final Context context, String username, String password, String registrationId) {
        Log.i(TAG, "Registering the device");

        ServerResponse serverResponse = null;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(REGISTER_URL);

        List<NameValuePair> value = new LinkedList<NameValuePair>();
        value.add(new BasicNameValuePair("username", username));
        value.add(new BasicNameValuePair("password", password));
        value.add(new BasicNameValuePair("registrationId", registrationId));

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
        GCMRegistrar.setRegisteredOnServer(context, true);
        return serverResponse;
    }

    static void unregister(final Context context, final String regId) {
        Log.i(TAG, "Unregistering the device");
        GCMRegistrar.setRegisteredOnServer(context, false);
    }
}