package com.example.friendfinder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSignIn;
    private Button buttonRegister;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonRegister = (Button) findViewById(R.id.buttonRegister);

    }
}