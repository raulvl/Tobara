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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.getIntent;

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView titleView;
    public TextView authorView;
    public ImageView heartView;
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
    private TextView location_placeholder_text;
    private ImageView location_placeholder_image;
    private TextView dateView;
    private TextView num_comments;
    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH = "extra_download_path";
    public ProgressBar pbar;

    public PostViewHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();
        titleView = (TextView) itemView.findViewById(R.id.post_title);
        authorView = (TextView) itemView.findViewById(R.id.post_author);
        heartView = (ImageView) itemView.findViewById(R.id.star);
        numStarsView = (TextView) itemView.findViewById(R.id.post_num_stars);
        num_comments = (TextView) itemView.findViewById(R.id.post_num_comments);

        pbar = (ProgressBar)itemView.findViewById(R.id.progressBar1);


        pictureView = (ImageView) itemView.findViewById(R.id.imageViewPost);

        pictureProfileView = (ImageView) itemView.findViewById(R.id.post_author_photo);

        deletepost = (ImageView) itemView.findViewById(R.id.config_posts);

        location_placeholder_image = (ImageView) itemView.findViewById(R.id.location_placeholder_icon);
        location_placeholder_text = (TextView) itemView.findViewById(R.id.text_location_picture_post);
        dateView = (TextView) itemView.findViewById(R.id.post_date);

    }

    public void bindToPost(final Post post, View.OnClickListener starClickListener) {

        final String shareTitle = post.title;

        titleView.setText(shareTitle);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        heartView.setOnClickListener(starClickListener);
        location_placeholder_text.setText(post.location_name);

        try {
            dateView.setText(getDate(post));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        pbar.setVisibility(View.VISIBLE);
        Picasso.with(context).load(post.user_image_path).resize(55, 55).onlyScaleDown().centerCrop().into(pictureProfileView);
        ChargeProgressBarPicture(post, true);
        Picasso.with(context).load(post.download_image_path).error(R.mipmap.ic_new_post_image).fit().centerCrop().into(pictureView, new Callback() {
            @Override
            public void onSuccess() {
               if(pbar!= null) {
                   ChargeProgressBarPicture(post, false);
                   pbar.setVisibility(View.GONE);
                   location_placeholder_text.setVisibility(View.VISIBLE);
                   location_placeholder_image.setVisibility(View.VISIBLE);
               }
               }

            @Override
            public void onError() {
                ChargeProgressBarPicture(post, false);
                pbar.setVisibility(View.GONE);

            }
        });


    }


    public String getDate(Post post) throws ParseException {
        String date="";
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c.getTime());


        long actual_time = c.getTime().getTime();
        SimpleDateFormat curFormater = new SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy");
        Date ptime = getDateFromString(post.date);
        long post_time = ptime.getTime();
        long secs = (actual_time - post_time)/1000;
        long hours = secs / 3600 ;
        long mins = secs /60;
        long days = hours / 24 ;
        long weeks = days / 7;

        //long milis_to_hours = TimeUnit.MILLISECONDS.toHours(hours);


        if(hours > 24){
            SimpleDateFormat holder = new SimpleDateFormat("dd MMMM yyyy");
            Date d = getDateFromString(post.date);
            String testing = holder.format(d);
            date = context.getResources().getString(R.string.date_post_over_a_day)+" "+testing;
        }
        else if(mins > 60 ) {
            date = context.getResources().getString(R.string.date_post)+" "+hours+" "+context.getResources().getString(R.string.date_hours);
        }
        else if (secs > 60){
            date = context.getResources().getString(R.string.date_post)+" "+mins+" "+context.getResources().getString(R.string.date_minutes);
        }
        else{
            date = context.getResources().getString(R.string.date_post)+" "+mins+" "+context.getResources().getString(R.string.date_seconds);
        }
        return date;
    }

    private Date getDateFromString(String sDate)
    {
        String dateFormat = "EEE MMM dd HH:mm:ss z yyyy";

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, new Locale("en_US"));
        sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        Date newDate = null;
        try {
            newDate = sdf.parse(sDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return newDate;

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

    public void getNumberOfComments(Post p, DatabaseReference postRef){

        FirebaseDatabase.getInstance().getReference().child("post-comments").child(postRef.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long n = dataSnapshot.getChildrenCount();
                System.out.println("Number of comments: " +n);
                num_comments.setText(String.valueOf(n));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){

        }
    }
}
