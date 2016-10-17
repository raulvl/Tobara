package com.villegas.raul.firebase.models;

/**
 * Created by raulvl on 21-06-2016.
 */
public class User {
    public String username;
    public String email;
    public String picture_profile_path;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String picture_profile_path) {
        this.username = username;
        this.email = email;
        this.picture_profile_path = picture_profile_path;
    }

}
