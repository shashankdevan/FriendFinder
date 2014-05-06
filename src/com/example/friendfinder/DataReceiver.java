package com.example.friendfinder;

/* A simple interface to ease the communication between the
 * async task and the activities.
 */
public interface DataReceiver {
    public void receive(ServerResponse serverResponse);
}
