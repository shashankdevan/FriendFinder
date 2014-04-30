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

import static com.example.friendfinder.Global.LOGIN_URL;
import static com.example.friendfinder.Global.SENDER_ID;
import static com.example.friendfinder.Global.USERNAME;

public class GCMIntentService extends GCMBaseIntentService {

    static final String TAG = "SERVICE";
    static String tickerText = null;
    static String bigText = "This is expanded text This is expanded text This is expanded text This is expanded text";
    static String notification_title = "Friend Finder";
    public SharedPreferences preferences;
    public GCMIntentService() {
        super(SENDER_ID);
    }


    @Override
    protected void onRegistered(Context context, String registrationId) {
        ServerResponse response = BackendServer
                .register(context, RegisterActivity.username, RegisterActivity.password, registrationId);

        if (response != null) {
            if (response.getStatusCode() == 200) {
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

    @Override
    protected void onMessage(Context context, Intent intent) {
        String username = intent.getExtras().getString("username");
        tickerText = username + " is nearby!";
        String message = username + " is nearby. See " + username + "?";
        generateNotification(context, message);
    }

    @Override
    protected void onError(Context context, String s) {

    }

    private void generateNotification(Context context, String message) {

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getString(USERNAME, "") != "") {
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();

            Intent notificationIntent = new Intent(context, MapActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            notificationIntent.putExtra(USERNAME, preferences.getString(USERNAME, ""));
            PendingIntent p_intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Notification notification = new Notification.Builder(context)
                    .setContentTitle(notification_title)
                    .setContentText(message)
                    .setStyle(new Notification.BigTextStyle().bigText(bigText))
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
