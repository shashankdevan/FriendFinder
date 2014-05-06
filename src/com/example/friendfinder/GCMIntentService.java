package com.example.friendfinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

import static com.example.friendfinder.Global.*;

public class GCMIntentService extends GCMBaseIntentService {

    static final String TAG = "SERVICE";
    static String notification_title = "Friend Finder";
    public SharedPreferences preferences;
    public GCMIntentService() {
        super(SENDER_ID);
    }


    @Override
    protected void onRegistered(Context context, String registrationId) {
        /* Register the device with the friend finder server once we are done
         * with the GCM registration
         */
        ServerResponse response = BackendServer
                .register(context, RegisterActivity.username, RegisterActivity.password, registrationId);

        /* Once the registration is successful, store the user session and open the map for that user
         */
        if (response != null) {
            if (response.getStatusCode() == 200) {
                preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(USERNAME, RegisterActivity.username);
                editor.commit();

                Intent i = new Intent(context, MapActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(USERNAME, RegisterActivity.username);
                startActivity(i);
            } else {
                Global.displayMessage(context, response.getMessage());
            }
        }
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        BackendServer.unregister(context, registrationId);
    }

    /* On receiving the message generate a notification */
    @Override
    protected void onMessage(Context context, Intent intent) {
        String username = intent.getExtras().getString(USERNAME);
        String lat = intent.getExtras().getString(LATITUDE);
        String lng = intent.getExtras().getString(LONGITUDE);
        generateNotification(context, username, lat, lng);
    }

    @Override
    protected void onError(Context context, String s) {

    }

    private void generateNotification(Context context, String username, String lat, String lng) {
        String message = username + " is nearby. See " + username + "?";
        String tickerText = username + " is nearby!";
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* Generate teh notification only when there is a user currently logged in
         */
        if (preferences.getString(USERNAME, "") != "") {
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();

            Intent notificationIntent = new Intent(context, MapActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("incoming_user", username);
            editor.putString("incoming_lat", lat);
            editor.putString("incoming_lng", lng);
            editor.commit();

            PendingIntent p_intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(notification_title)
                    .setContentText(message)
                    .setSmallIcon(icon)
                    .setWhen(when)
                    .setTicker(tickerText)
                    .setContentIntent(p_intent)
                    .build();

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, notification);
        }
    }

}
