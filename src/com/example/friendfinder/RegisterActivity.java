package com.example.friendfinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;

import static com.example.friendfinder.Global.*;

public class RegisterActivity extends Activity implements View.OnClickListener, DataReceiver {

    static final String TAG = "REGISTER";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSubmit;
    public static String username = null;
    public static String password = null;
    public SharedPreferences preferences;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        context = this;

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);

        /* Register a broadcast receiver to receive the messages from the GCMService
         */
        registerReceiver(messageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
        buttonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSubmit:
                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();

                GCMRegistrar.checkDevice(this);
                String regId = GCMRegistrar.getRegistrationId(this);

                Log.d(TAG, regId);

                /* Register with the GCM server is not registered the device already
                 * else register only with our server.
                 */
                if (regId.equals("")) {
                    GCMRegistrar.register(this, SENDER_ID);
                } else {
                    String[] params = {regId};
                    RegisterSession mySession = new RegisterSession(context);
                    mySession.delegate = (DataReceiver) context;
                    mySession.execute(params);
                }
                break;
        }
    }

    /* Receives the response after the registration request is processed by the server
     * On success open the map to display the friends else show an error message toast
     */
    @Override
    public void receive(ServerResponse response) {
        if (response != null) {
            if (response.getStatusCode() == 200) {
                preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(USERNAME, username);
                editor.commit();

                Intent i = new Intent(context, MapActivity.class);
                i.putExtra(USERNAME, username);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(context, response.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Async task to send the registration request to the server asynchronously
     */
    private class RegisterSession extends AsyncTask<String, Void, ServerResponse> {
        private final Context RegisterSessionContext;
        public DataReceiver delegate;
        private ProgressDialog dialog;

        public RegisterSession(Context context) {
            RegisterSessionContext = context;
            dialog = new ProgressDialog(RegisterSessionContext);
        }

        /* Showing a loading kind of dialog while the server is processing the request
         */
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage("Signing up..");
            this.dialog.show();
        }

        @Override
        protected ServerResponse doInBackground(String... params) {
            return BackendServer.register(context, username, password, params[0]);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ServerResponse response) {
            super.onPostExecute(response);
            delegate.receive(response);
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }

    /* Broadcast receiver to receive message from the GCMIntentServive. When the server sends a response
     * the error messages need to be display on the screen. This reciever captures the messages and
     * displays a toast message.
     */
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getExtras().getString(MESSAGE), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(messageReceiver);
        GCMRegistrar.onDestroy(this);
    }

}
