package com.villegas.raul.firebase.viewholder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.villegas.raul.firebase.NewPostActivity;
import com.villegas.raul.firebase.R;
import com.villegas.raul.firebase.models.Post;
import com.villegas.raul.firebase.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.Intent.getIntent;

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView;
    public ImageView pictureView;
    public ImageView pictureProfileView;
    public String TAG ="PICTURE";
    public ImageButton imageButton;
    public ImageView deletepost;
    private Context context;
    private StorageReference mStorageRef;
    int progressStatus = 0;
    private Handler handler = new Handler();
    private Target target;

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public ProgressBar pbar;
    public PostViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();

        titleView = (TextView) itemView.findViewById(R.id.post_title);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        starView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        bodyView = (TextView) itemView.findViewById(R.id.post_body);

        pbar = (ProgressBar)itemView.findViewById(R.id.progressBar1);


        pictureView = (ImageView) itemView.findViewById(R.id.imageViewPost);

        pictureProfileView = (ImageView) itemView.findViewById(R.id.post_author_photo);

        deletepost = (ImageView) itemView.findViewById(R.id.config_posts);
    }

    public void bindToPost(final Post post, View.OnClickListener starClickListener) {
        final String shareBody = post.body;
        final String shareTitle = post.title;
        //final Uri uri_post = Uri.parse(post.image_path);
        titleView.setText(shareTitle);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(shareBody);
        starView.setOnClickListener(starClickListener);
        pbar.setVisibility(View.VISIBLE);
        Picasso.with(context).load(post.user_image_path).resize(55, 55).onlyScaleDown().centerCrop().into(pictureProfileView);
        ChargeProgressBarPicture(post, true);
        Picasso.with(context).load(post.download_image_path).error(R.mipmap.ic_new_post_image).fit().centerCrop().into(pictureView, new Callback() {
            @Override
            public void onSuccess() {
               if(pbar!= null) {
                   ChargeProgressBarPicture(post, false);
                   pbar.setVisibility(View.GONE);
               }
               }

            @Override
            public void onError() {
                ChargeProgressBarPicture(post, false);
                pbar.setVisibility(View.GONE);

            }
        });


    }
    public void ChargeProgressBarPicture(final Post post, Boolean b) {
        if (b == true) {
            new Thread(new Runnable() {
                public void run() {
                    while (progressStatus < 20) {
                        progressStatus += 1;
                        // Update the progress bar and display the
                        //current value in the text view
                        handler.post(new Runnable() {
                            public void run() {
                                pbar.setProgress(progressStatus);
                            }
                        });
                        try {
                            // Sleep for 200 milliseconds.
                            //Just to display the progress slowly
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){

        }
    }
}
