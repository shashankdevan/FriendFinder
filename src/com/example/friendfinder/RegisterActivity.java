package com.example.friendfinder;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gcm.GCMRegistrar;

import static com.example.friendfinder.Global.SENDER_ID;

public class RegisterActivity extends Activity implements View.OnClickListener {

    static final String TAG = "REGISTER";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSubmit;

    public static String username = null;
    public static String password = null;

    Context context;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSubmit:
                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();

                GCMRegistrar.checkDevice(this);
                final String regId = GCMRegistrar.getRegistrationId(this);

                Log.d(TAG, regId);

                if (regId.equals("")) {
                    GCMRegistrar.register(this, SENDER_ID);
                } else {
                    AsyncTask<Void, Void, ServerResponse> registerTask = new AsyncTask<Void, Void, ServerResponse>() {
                        @Override
                        protected ServerResponse doInBackground(Void... params) {
                            return BackendServer.register(context, username, password, regId);
                        }

                        @Override
                        protected void onPostExecute(ServerResponse response) {
                            super.onPostExecute(response);
                            Log.d(TAG, response.getMessage());
                        }
                    };
                    registerTask.execute(null, null, null);
                }
                break;
        }
    }

}
