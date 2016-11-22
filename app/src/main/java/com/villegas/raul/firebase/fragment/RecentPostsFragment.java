package com.villegas.raul.firebase.fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ValueEventListener;
import com.villegas.raul.firebase.GPSTracker;
import com.villegas.raul.firebase.MainActivity;
import com.villegas.raul.firebase.R;
import com.villegas.raul.firebase.models.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(final DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys


        // [END recent_posts_query]
        final Query recentPostsQuery = databaseReference.child("posts_locations_by_user").child(getUid()).limitToFirst(100);

        return  recentPostsQuery;

    }




}
