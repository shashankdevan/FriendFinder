package com.example.friendfinder;

import android.text.Editable;
import org.apache.http.entity.SerializableEntity;

import java.io.Serializable;

/**
 * Created by shashank on 4/21/14.
 */
public class Credentials implements Serializable {
    private String username = null;
    private  String password = null;
//    private String email = null;

    public Credentials(String _username, String _password){
        this.username = _username;
        this.password = _password;
    }

//    public void setUserName(String _username) {
//        username = _username;
//    }

//    public void setPassword(String _password) {
//        password = _password;
//    }

}
