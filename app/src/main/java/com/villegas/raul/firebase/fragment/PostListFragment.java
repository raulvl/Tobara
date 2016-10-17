package com.villegas.raul.firebase.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.LoginWithEmailActivity;
import com.villegas.raul.firebase.MainActivity;
import com.villegas.raul.firebase.Ofertas;
import com.villegas.raul.firebase.UpdateProfileActivity;
import com.villegas.raul.firebase.models.Post;
import com.villegas.raul.firebase.PostDetailActivity;
import com.villegas.raul.firebase.R;
import com.villegas.raul.firebase.models.User;
import com.villegas.raul.firebase.viewholder.PostViewHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class PostListFragment extends Fragment{

    private static final String TAG = "PostListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    public String[] opciones;
    public PostListFragment() {}
    private StorageReference mStorageRef;
    private static int selectedItem = -1;
    public static int index = -1;
    public static int top = -1;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
        mRecycler.setItemViewCacheSize(1024);



        final FloatingActionsMenu fabmenu = (FloatingActionsMenu) ((Ofertas)getActivity()).findViewById(R.id.menu_fab);
       // final FloatingActionButton fab = (FloatingActionButton) ((Ofertas)getActivity()).findViewById(R.id.fab_new_post);
        TabLayout tabs = (TabLayout)((Ofertas)getActivity()).findViewById(R.id.tabs);
        final ViewPager mViewPager = (ViewPager) ((Ofertas)getActivity()).findViewById(R.id.container);


        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0 ||dy<0 && fabmenu != null)
                    fabmenu.setVisibility(View.GONE);
                    //fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    fabmenu.setVisibility(View.VISIBLE);
                    //fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        tabsToTop(mViewPager, mRecycler, tabs);

        return rootView;
    }



    public void beginSearch(String query) {
         final Query recentPostsQuery = mDatabase.child("posts").orderByChild("title").startAt(query).endAt(query+"\uf8ff");
         setFirebaseRecyclerAdapter(mAdapter, recentPostsQuery);

         //mRecycler.smoothScrollToPosition(mRecycler.getAdapter().getItemCount());
    }


    public FirebaseRecyclerAdapter<Post, PostViewHolder> getFirebaseRecyclerAdapter(){
        return mAdapter;
    }

    public void closeMenu(){
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        Query postsQuery = getQuery(mDatabase);
        setFirebaseRecyclerAdapter(mAdapter, postsQuery);
    }

    @Override
    public  void onStart(){
        super.onStart();
        //mRecycler.smoothScrollToPosition(mRecycler.getAdapter().getItemCount());
    }
    @Override
    public void onResume()
    {
        super.onResume();
        //set recyclerview position
        mRecycler.smoothScrollToPosition(mRecycler.getAdapter().getItemCount());

    }

    @Override
    public void onPause()
    {
        super.onPause();
        //read current recyclerview position
       // mRecycler.smoothScrollToPosition(mRecycler.getAdapter().getItemCount());
    }

    public void setFirebaseRecyclerAdapter(FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter, Query postsQuery){

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();


                mStorageRef = FirebaseStorage.getInstance().getReference();

                // persistencia de datos en el post
                //postRef.keepSynced(true);

                final StorageReference photoRef = mStorageRef.child(model.image_path);


                //See post detail
                viewHolder.pictureView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });
                // Open dialog for sharing and deleting only your own posts
                viewHolder.deletepost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getOptionMenu(viewHolder, model, postRef, photoRef);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }


                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored

                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });

            }

        };
        mRecycler.setAdapter(mAdapter);

    }

   public void tabsToTop(final ViewPager v, final RecyclerView r, TabLayout t){
        if (t != null) {
            t.setOnTabSelectedListener(
                    new TabLayout.ViewPagerOnTabSelectedListener(v) {
                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {
                            r.smoothScrollToPosition(r.getAdapter().getItemCount());
                        }
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            v.setCurrentItem(tab.getPosition());
                        }

                    });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        // Set up Layout Manager, reverse layout

        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        setFirebaseRecyclerAdapter(mAdapter, postsQuery);


    }

    public void getOptionMenu(final PostViewHolder viewHolder, final Post model, final DatabaseReference postRef,  final StorageReference photoRef  ){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //Log.d("Usuario", "firebase:" +user.getDisplayName());
        //Log.d("Usuario", "database:" +model.author);
        final String userId = getUid();
        //Log.d("Usuario", "database:" +mDatabase.child("users").child(userId));
        if(userId.equals(model.uid)) {

            opciones = getResources().getStringArray(R.array.options);
        }
        else{
            opciones = getResources().getStringArray(R.array.options2);
        }

        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(getContext());
        alertdialogbuilder.setItems(opciones, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedText = Arrays.asList(opciones).get(which);


                if (which == 0) {
                    ImageView ivImage = viewHolder.pictureView;
                    Uri bmpUri = getLocalBitmapUri(ivImage);
                    if (bmpUri != null) {

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.putExtra(Intent.EXTRA_TITLE, model.title);
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, model.body);
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                        sharingIntent.setType("image/*");
                        // Launch sharing dialog for image
                        getContext().startActivity(Intent.createChooser(sharingIntent, "Share Image"));
                    }
                }
                if (which == 1) {
                    postRef.getRef().removeValue();
                    mDatabase.child("post-comments").child(postRef.getKey()).removeValue();
                    mDatabase.child("user-posts").child(model.uid).child(postRef.getKey()).removeValue();
                    // Delete the file
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // File deleted successfully
                            Toast.makeText(getActivity(), getResources().getString(R.string.delete_post),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                        }
                    });
                    Toast.makeText(getActivity(), getResources().getString(R.string.delete_post),
                            Toast.LENGTH_SHORT).show();

                }

            }
        });

        AlertDialog dialog = alertdialogbuilder.create();
        dialog.show();
    }


    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_stars_transaction]

    // Returns the URI path to the Bitmap displayed in specified ImageView
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
            File file =  new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
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
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
