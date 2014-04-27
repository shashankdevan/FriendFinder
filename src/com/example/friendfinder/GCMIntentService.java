package com.example.friendfinder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

import static com.example.friendfinder.Global.SENDER_ID;
import static com.example.friendfinder.Global.USERNAME;

public class GCMIntentService extends GCMBaseIntentService {

    static final String TAG = "SERVICE";

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
        String message = intent.getExtras().getString("username");
        generateNotification(context, message);
    }

    @Override
    protected void onError(Context context, String s) {

    }

    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);

        String title = context.getString(R.string.app_name);

        Intent notificationIntent = new Intent(context, MapActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);
    }
}
