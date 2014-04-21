package com.example.friendfinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements View.OnClickListener {

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

        buttonSignIn.setOnClickListener(this);
        buttonRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSignIn:
                Toast.makeText(this, "Sign In", Toast.LENGTH_LONG);
                break;
            case R.id.buttonRegister:
                Toast.makeText(this, "Register", Toast.LENGTH_LONG);
                break;
        }
    }
}