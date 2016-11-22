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
    public String date;
    public String location_name;
    public String location_latitude;
    public String location_longitude;

    public int starCount = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String title, String image_path,String download_image_path, String user_image_path, String date, String location_name, String location_latitude, String location_longitude) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        //this.body = body;
        this.image_path = image_path;
        this.download_image_path = download_image_path;
        this.user_image_path = user_image_path;
        this.date = date;
        this.location_name = location_name;
        this.location_latitude = location_latitude;
        this.location_longitude = location_longitude;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("image_path", image_path);
        result.put("download_image_path", download_image_path);
        result.put("user_image_path", user_image_path);
        result.put("date", date);
        result.put("location_name",location_name);
        result.put("location_latitude",location_latitude);
        result.put("location_longitude",location_longitude);
        result.put("starCount", starCount);
        result.put("stars", stars);

        return result;
    }
    // [END post_to_map]
}

