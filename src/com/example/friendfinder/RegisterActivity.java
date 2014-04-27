package com.example.friendfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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

import static com.example.friendfinder.Global.SENDER_ID;

public class RegisterActivity extends Activity implements View.OnClickListener, DataReceiver {

    static final String TAG = "REGISTER";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSubmit;

    public static String username = null;
    public static String password = null;

    private Context context;

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
                String regId = GCMRegistrar.getRegistrationId(this);

                Log.d(TAG, regId);

                if (regId.equals("")) {
                    GCMRegistrar.register(this, SENDER_ID);
                } else {
                    String[] params = {regId};
                    RegisterSession mySession = new RegisterSession();
                    mySession.delegate = (DataReceiver) context;
                    mySession.execute(params);
                }
                break;
        }
    }

    @Override
    public void receive(ServerResponse response) {
        if (response != null) {
            if (response.getStatusCode() == 200) {
                Intent i = new Intent(context, MapActivity.class);
                i.putExtra("username", username);
                startActivity(i);
            } else {
                Toast.makeText(context, response.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private class RegisterSession extends AsyncTask<String, Void, ServerResponse> {
        public DataReceiver delegate;

        @Override
        protected ServerResponse doInBackground(String... params) {
            return BackendServer.register(context, username, password, params[0]);
        }

        @Override
        protected void onPostExecute(ServerResponse response) {
            super.onPostExecute(response);
            delegate.receive(response);
        }
    }
}
