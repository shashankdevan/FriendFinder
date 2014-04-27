package com.example.friendfinder;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

import static com.example.friendfinder.Global.SENDER_ID;

public class GCMIntentService extends GCMBaseIntentService {

    static final String TAG = "SERVICE";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "On registered");
        ServerResponse serverResponse = BackendServer
                .register(context, RegisterActivity.username, RegisterActivity.password, registrationId);
        Log.d(TAG, serverResponse.getMessage());
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        BackendServer.unregister(context, registrationId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {

    }

    @Override
    protected void onError(Context context, String s) {

    }

}
