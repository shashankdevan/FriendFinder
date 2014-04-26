package com.example.friendfinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;

public class RegisterActivity extends Activity implements View.OnClickListener, DataReceiver {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSubmit;
    private String[] params = new String[2];
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
        Intent i;
        switch (v.getId()) {
            case R.id.buttonSubmit:
                params[0] = editTextUsername.getText().toString();
                params[1] = editTextPassword.getText().toString();
                RegisterSession mySession = new RegisterSession();
                mySession.delegate = (DataReceiver) context;
                mySession.execute(params);

                Toast.makeText(context, "Submit", Toast.LENGTH_LONG);
                i = new Intent(context, MapActivity.class);
                startActivity(i);
                break;
        }
    }

    @Override
    public void receive(String responseString) {
        Toast.makeText(this, responseString, Toast.LENGTH_LONG);
    }

    private static class RegisterSession extends AsyncTask<String, Integer, String> {
        public DataReceiver delegate;
        private String feedback = null;

        @Override
        protected String doInBackground(String... params) {
            String responseString = null;
            String[] parameters = params;

            String username = parameters[0];
            String password = parameters[1];

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://mpss.csce.uark.edu/~devan/register.php");

            List<NameValuePair> value = new LinkedList<NameValuePair>();
            value.add(new BasicNameValuePair("username", username));
            value.add(new BasicNameValuePair("password", password));

            try {
                post.setEntity(new UrlEncodedFormEntity(value));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                HttpResponse response = client.execute(post);
                feedback = String.valueOf(response.getStatusLine().getReasonPhrase());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            delegate.receive(feedback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

    }
}
