package com.villegas.raul.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.fragment.PostListFragment;
import com.villegas.raul.firebase.models.Post;
import com.villegas.raul.firebase.models.User;
import com.villegas.raul.firebase.models.Comment;
import com.villegas.raul.firebase.viewholder.PostViewHolder;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostDetailActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mStarCount;
    private EditText mCommentField;
    private ImageView mCommentPhoto;
    private ImageView mImageView;
    private ImageView mAuthorPhoto;
    private ImageView starView;
    private ImageView config_image;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;
    private StorageReference mStorageRef;
    private ProgressBar pbar;
    public String[] opciones;
    private DatabaseReference mDatabase;
    private StorageReference photoRef;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);





        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child(mPostKey);
        mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_author);
        mTitleView = (TextView) findViewById(R.id.post_title);
        //mBodyView = (TextView) findViewById(R.id.post_body);
        mStarCount = (TextView) findViewById(R.id.post_num_stars);
        starView = (ImageView) findViewById(R.id.star);
        config_image = (ImageView) findViewById(R.id.config_posts);
        mImageView = (ImageView) findViewById(R.id.imageViewPost);
        mAuthorPhoto = (ImageView)findViewById(R.id.post_author_photo);
        mCommentField = (EditText) findViewById(R.id.field_comment_text);
        mCommentButton = (Button) findViewById(R.id.button_post_comment);
        mCommentPhoto = (ImageView) findViewById(R.id.comment_photo);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);
        // check the behavior of this
        mCommentsRecycler.setHasFixedSize(true);
        mCommentsRecycler.setItemViewCacheSize(1024);

        pbar = (ProgressBar)findViewById(R.id.progressBar1);
        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));


    }



    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final Post post = dataSnapshot.getValue(Post.class);
                // [START_EXCLUDE]


                // persistencia de datos en el post
                //postRef.keepSynced(true);


                pbar.setVisibility(View.VISIBLE);

                try {
                    // Obtener link de la foto y enlazarla al imageview
                    Picasso.with(mImageView.getContext()).load(post.download_image_path).error(R.mipmap.ic_new_post_image).fit().centerCrop().into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            if (pbar != null) {
                                pbar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onError() {  pbar.setVisibility(View.GONE);  }
                    });


                    Picasso.with(mAuthorPhoto.getContext()).load(post.user_image_path).resize(55, 55).onlyScaleDown().centerCrop().into(mAuthorPhoto);
                    mAuthorView.setText(post.author);
                    mTitleView.setText(post.title);
                    //mBodyView.setText(post.body);
                    mStarCount.setText(String.valueOf(post.starCount));


                    config_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getOptionMenu(post);
                        }
                    });
                    // Determine if the current user has liked this post and set UI accordingly
                    if (post.stars.containsKey(getUid())) {
                        starView.setImageResource(R.drawable.ic_toggle_star_24);
                    } else {
                        starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    public void getOptionMenu(final Post model ){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //Log.d("Usuario", "firebase:" +user.getDisplayName());
        //Log.d("Usuario", "database:" +model.author);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        photoRef = mStorageRef.child(model.image_path);
        final String userId = getUid();
        //Log.d("Usuario", "database:" +mDatabase.child("users").child(userId));
        if(userId.equals(model.uid)) {

            opciones = getResources().getStringArray(R.array.options);
        }
        else{
            opciones = getResources().getStringArray(R.array.options2);
        }

        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(PostDetailActivity.this);
        alertdialogbuilder.setItems(opciones, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedText = Arrays.asList(opciones).get(which);


                if (which == 0) {
                    ImageView ivImage = mImageView;
                    Uri bmpUri = getLocalBitmapUri(ivImage);
                    if (bmpUri != null) {

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.putExtra(Intent.EXTRA_TITLE, model.title);
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, model.body);
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                        sharingIntent.setType("image/*");
                        // Launch sharing dialog for image
                        startActivity(Intent.createChooser(sharingIntent, "Share Image"));
                    }
                }
                if (which == 1) {
                    mPostReference.getRef().removeValue();
                    mDatabase.child("user-posts").child(model.uid).child(mPostReference.getKey()).removeValue();

                    // Delete the file
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Toast.makeText(PostDetailActivity.this, getResources().getString(R.string.delete_post),
                                    Toast.LENGTH_SHORT).show();


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                    Toast.makeText(PostDetailActivity.this, getResources().getString(R.string.delete_post),
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PostDetailActivity.this, Ofertas.class);
                    startActivity(intent);
                    finish();

                }

            }
        });

        AlertDialog dialog = alertdialogbuilder.create();
        dialog.show();
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file =  new File(PostDetailActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }


    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_post_comment:
                postComment();
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Ofertas.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);
                        Log.d(TAG, "onChildAdded:" + user.picture_profile_path);


                        //);
                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);
                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;
        public ImageView pictureView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorView = (TextView) itemView.findViewById(R.id.comment_author);
            bodyView = (TextView) itemView.findViewById(R.id.comment_body);
            pictureView = (ImageView) itemView.findViewById(R.id.comment_photo);

        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();
        private FirebaseUser user;
        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;
            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

            mDatabase.child("users").child(comment.uid).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);
                            Picasso.with(holder.pictureView.getContext()).load(user.picture_profile_path).into(holder.pictureView);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }
}