package com.villegas.raul.firebase.models;

/**
 * Created by raulvl on 22-06-2016.
 */
public class Comment {

    public String uid;
    public String author;
    public String text;
    public String date;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String uid, String author, String text, String date) {
        this.uid = uid;
        this.author = author;
        this.text = text;
        this.date = date;
    }

}
// [END comment_class]