package com.villegas.raul.firebase;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//import com.facebook.share.model.ShareLinkContent;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.share.model.ShareLinkContent;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.models.Post;
import com.villegas.raul.firebase.models.User;
import com.villegas.raul.firebase.viewholder.PostViewHolder;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;


public class NewPostActivity extends BaseActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";


    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mBodyField;
    private EditText locationInfo;
    private TextView locationField;
    private Button btn_tp;
    private ImageView image;
    private StorageReference mStorageRef;
    private BroadcastReceiver mDownloadReceiver;
    private ToggleButton fb;
    private GoogleApiClient mGoogleApiClient;
    int PLACE_PICKER_REQUEST = 1;//Predefined req_code
    Location mLastLocation;
    private double latitude ;
    private double longitude ;
    private String location_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Calendar c = Calendar.getInstance();
        String fecha = String.valueOf(c.get(Calendar.DATE));


        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        onPlaceLikelihood();
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]
        FirebaseStorage storage = FirebaseStorage.getInstance();
        //mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef = storage.getReferenceFromUrl("gs://fir-cazador.appspot.com/");


        mTitleField = (EditText) findViewById(R.id.field_title);
        //mBodyField = (EditText) findViewById(R.id.field_body);
        locationInfo = (EditText) findViewById(R.id.field_location);
        //locationField = (TextView)findViewById(R.id.text_location_picture_post);
        image = (ImageView) findViewById(R.id.picture_offer);
        fb = (ToggleButton) findViewById(R.id.toggle);
        fb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isLoggedIn()) {
                        Toast.makeText(NewPostActivity.this, "you need to be logged into the facebook app in order to post", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }


        Intent intent = getIntent();
        String image_path = intent.getStringExtra("ruta");

        mFileUri = Uri.parse(image_path);
        Log.d(TAG, "get Uri from Intent desde Ofertas: " + mFileUri);
        Picasso.with(image.getContext()).load(mFileUri).fit().centerCrop().into(image);
        uploadFromUri(mFileUri);

        // floating button para tomar foto de nuevo si es que no escogiste la correcta o quieres tomar otra
        findViewById(R.id.fab_take_another_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Choose file storage location
                File file = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID().toString() + ".jpg");
                mFileUri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
                startActivityForResult(intent, RC_TAKE_PICTURE);
            }
        });

        findViewById(R.id.fab_submit_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFileUri == null) {
                    Toast.makeText(NewPostActivity.this, "Error: You need to take a picture first.", Toast.LENGTH_SHORT).show();
                } else {
                    if (fb.isChecked()) {
                        if (isLoggedIn()) {
                            postToFacebook();
                        }
                    }
                    submitPost();
                }

            }
        });


    }

    /*
    * In this method, Start PlaceAutocomplete activity
    * PlaceAutocomplete activity provides--
    * a search box to search Google places
    */
    public void findPlace() {
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(NewPostActivity.this);
            // Start the Intent by requesting a result, identified by a request code.
            startActivityForResult(intent, PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesRepairableException e) {

        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(NewPostActivity.this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    //function triggered by a button to get nearby places
    public void onPlaceLikelihood() {
        Log.i("Place", "onPlaceLikelihood");
        if (mGoogleApiClient != null) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {

                    PlaceLikelihood placeLikelihood = likelyPlaces.get(0);
                    String content = "";
                    if (placeLikelihood != null && placeLikelihood.getPlace() != null && !TextUtils.isEmpty(placeLikelihood.getPlace().getName()))
                        content = "Most likely place: " + placeLikelihood.getPlace().getName() + "\n";
                        Log.d("==precisión==", content);
                    if (placeLikelihood != null)
                        content += "Percent change of being there: " + (int) (placeLikelihood.getLikelihood() * 100) + "%";
                        int likelihood_percentage =  (int) (placeLikelihood.getLikelihood() * 100);
                        Log.d("==precisión del entero:", String.valueOf(likelihood_percentage));

                    if(likelihood_percentage > 50){
                        locationInfo.setText(placeLikelihood.getPlace().getName());
                        Log.d("==Latitud:", String.valueOf(placeLikelihood.getPlace().getLatLng().latitude));
                        Log.d("==longitud:", String.valueOf(placeLikelihood.getPlace().getLatLng().longitude));
                        location_name = placeLikelihood.getPlace().getName().toString();
                        longitude = placeLikelihood.getPlace().getLatLng().longitude;
                        latitude = placeLikelihood.getPlace().getLatLng().latitude;
                    }
                    else{
                    // change this shit to an string xml value of strings.xml
                       locationInfo.setText(""+longitude +", "+latitude);
                       location_name = String.valueOf(longitude) + ", " + String.valueOf(latitude);
                    }

                    likelyPlaces.release();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void postToFacebook() {
        Bundle params = new Bundle();
        params.putString("url", mDownloadUrl.toString());
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //submitPost();
                        Log.d("==graph response==", response.toString());
                        if (response.getError() == null) {
                            Toast.makeText(NewPostActivity.this, getResources().getString(R.string.share_succesful), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NewPostActivity.this, getResources().getString(R.string.share_error), Toast.LENGTH_SHORT).show();

                        }
                    }
                }
        ).executeAsync();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //Bitmap bp = (Bitmap) data.getExtras().get("data");
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                Place place = PlacePicker.getPlace(this, data);
                Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber());

                /*((EditText) findViewById(R.id.field_location))
                        .setText(place.getName() + ",\n" +
                                place.getAddress());
                */
                ((EditText) findViewById(R.id.field_location))
                        .setText(place.getName());
                location_name = place.getName().toString();
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.e("Tag", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (mFileUri != null) {
                    Log.d(TAG, "get Uri from OnActivitYoNrESULT: " + mFileUri);
                    Picasso.with(image.getContext()).load(mFileUri).fit().centerCrop().into(image);
                    uploadFromUri(mFileUri);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else if (resultCode == RESULT_CANCELED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, RC_TAKE_PICTURE);
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFileUri = savedInstanceState.getParcelable("mFileUri");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // [START upload_from_uri]
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");
                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        Log.d(TAG, "get download URL loco: " + mDownloadUrl);

                        // [START_EXCLUDE]
                        hideProgressDialog();

                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(NewPostActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();

                        // [END_EXCLUDE]
                    }
                });
    }


    // Método que define la foto de perfil del usuario para visualizarse en los posts
    private Uri getProfilePicture() {
        Uri profile_picture;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.getPhotoUrl() == null) {
            profile_picture = Uri.parse("android.resource://com.villegas.raul.firebase/" + R.drawable.ic_action_account_circle_40);
        } else {
            profile_picture = user.getPhotoUrl();
        }
        return profile_picture;
    }





    private void submitPost() {
        final String title = mTitleField.getText().toString();
        //final String body = mBodyField.getText().toString();
        final String image_path = "photos/" + mFileUri.getLastPathSegment();
        final String download_image_path = mDownloadUrl.toString();


        TimeZone zone = Calendar.getInstance().getTimeZone();
        Calendar c = Calendar.getInstance(zone);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        final String date = String.valueOf(c.getTime());

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }



        showProgressDialog();
        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, image_path, download_image_path, user.picture_profile_path, date, location_name, String.valueOf(latitude), String.valueOf(longitude));

                            // set location field to the photo
                            //locationField.setText("Buena choro");
                        }

                        // Finish this Activity, back to the stream
                        finish();
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String title,String image_path, String download_image_path, String user_image_path, String date, String location_name, String location_latitude, String location_longitude) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        GeoFire geoFire;
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title,image_path, download_image_path, user_image_path, date, location_name, location_latitude, location_longitude);
        Map<String, Object> postValues = post.toMap();
        geoFire = new GeoFire(mDatabase.child("post_locations"));
        geoFire.setLocation(key, new GeoLocation(Double.valueOf(location_latitude), Double.valueOf(location_longitude)));
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                final StorageReference photoRef = mStorageRef.child("photos")
                        .child(mFileUri.getLastPathSegment());
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent myIntent = new Intent(getApplicationContext(), Ofertas.class);
                        startActivityForResult(myIntent, 0);
                        // File deleted successfully
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                    }
                });
                return true;
            case R.id.action_get_location:
                findPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_activity_menu, menu);
        return true;

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude =mLastLocation.getLatitude() ;
            longitude =mLastLocation.getLongitude();
            Log.d(TAG, "Longitude: "+ longitude + " Latitude:"+latitude);
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
