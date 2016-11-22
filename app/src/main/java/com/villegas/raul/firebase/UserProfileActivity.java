package com.villegas.raul.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.fragment.PostListFragment;
import com.villegas.raul.firebase.fragment.MyPostsFragment;
import com.villegas.raul.firebase.fragment.MyTopPostsFragment;
import com.villegas.raul.firebase.fragment.RecentPostsFragment;
import com.villegas.raul.firebase.models.Post;
import com.villegas.raul.firebase.models.User;
import com.villegas.raul.firebase.viewholder.PostViewHolder;

public class UserProfileActivity extends AppCompatActivity {

    private String mPostKey;
    private String mPostKeyFromPD;
    public static final String EXTRA_POST_KEY = "post_key";
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private DatabaseReference mDatabase;
    private  TextView authorView;
    private TextView count;
    private ImageView pictureProfileView;
    private Query q ;
    private final String TAG = "ON";
    private FirebaseAuth mAuth;


    public UserProfileActivity(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        authorView = (TextView) findViewById(R.id.post_author);
        pictureProfileView = (ImageView) findViewById(R.id.post_author_photo);
        count = (TextView) findViewById(R.id.count_offers);
        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mPostKeyFromPD = getIntent().getStringExtra("userKey");
        if (mPostKey == null) {
            if(mPostKeyFromPD == null){
                mPostKey = user.getUid();
            }else{
                mPostKey = mPostKeyFromPD;
            }


            //throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.recycler_posts_position, new PostListFragment() {
                        @Override
                        public Query getQuery(DatabaseReference databaseReference) {
                            return mDatabase.child("user-posts")
                                    .child(mPostKey);
                        }
                    });
            transaction.commit();
        }
        getUsernameEmailProfilePicture();
        getCountChildren();
    }

    public void getCountChildren(){

        mDatabase.child("user-posts")
                .child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long n = dataSnapshot.getChildrenCount();
                count.setText(String.valueOf(n));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getUsernameEmailProfilePicture(){

        mDatabase.child("users").child(mPostKey).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);
                        Picasso.with(pictureProfileView.getContext()).load(user.picture_profile_path).resize(150, 150).centerCrop().into(pictureProfileView);
                        authorView.setText(user.username);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(UserProfileActivity.this, Ofertas.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

}
