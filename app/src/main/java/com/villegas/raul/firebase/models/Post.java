package com.villegas.raul.firebase.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raulvl on 20-06-2016.
 */
public class Post {
    public String uid;
    public String author;
    public String title;
    public String body;
    public String image_path;
    public String download_image_path;
    public String user_image_path;
    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String body, String image_path,String download_image_path, String user_image_path) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
        this.image_path = image_path;
        this.download_image_path = download_image_path;
        this.user_image_path = user_image_path;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("image_path", image_path);
        result.put("download_image_path", download_image_path);
        result.put("user_image_path", user_image_path);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }
    // [END post_to_map]
}

