package com.villegas.raul.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.models.User;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateProfileActivity extends BaseActivity implements View.OnClickListener {

    private EditText usernameET, emailET, currpassET, newpassET, cnewpassET;
    private TextView changepictureTV, changePasswordTV;
    private ImageView changepictureIV;
    private DatabaseReference mDatabase;
    private final String TAG = "ON";
    private FirebaseUser user;
    //Settings upload/change photo
    private Uri outputFileUri;
    private static int YOUR_SELECT_PICTURE_REQUEST_CODE = 1;
    private static final int RC_TAKE_PICTURE = 101;
    private Uri mDownloadUrl = null;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        usernameET = (EditText) findViewById(R.id.usernameField);
        emailET = (EditText) findViewById(R.id.emailField);
        //passwords
        changepictureTV = (TextView) findViewById(R.id.changepicturetv);
        changepictureIV = (ImageView) findViewById(R.id.changepictureiv);

        changePasswordTV = (TextView) findViewById(R.id.changePasswordTV);

        changePasswordTV.setOnClickListener(this);
        changepictureTV.setOnClickListener(this);

        //Set Database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReferenceFromUrl("gs://fir-cazador.appspot.com/");
        //set Profile Edit
        getUsernameEmailProfilePicture();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

   public void reAuthenticate(String email, final String password){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        firebaseUser.reauthenticate(authCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User reauthenticated.");
                            setEmail(firebaseUser, password);
                            //setNewPassword(firebaseUser);
                        } else {
                            Log.d(TAG, "Error on User reauthenticated.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateProfileActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void setEmailPasswordDialog(){
        final String[] str = {""};
        if(!user.getEmail().equals(emailET.getText().toString())) {
            new MaterialDialog.Builder(this)
                    .title(R.string.title_dialog_update_email)
                    .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .input(R.string.hint_email, R.string.space_dialog_update_email , new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            str[0] = input.toString();
                            reAuthenticate(user.getEmail(), input.toString());
                        }
                    }).negativeText(R.string.cancel_button_dialog_update_email).show();
        }
    }

    public void setEmail(final FirebaseUser user, final String password){

        if(!user.getEmail().equals(emailET.getText().toString())){
            if(!emailET.getText().toString().isEmpty()){
                if(!password.isEmpty()){
                    user.updateEmail(emailET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User email address updated.");
                                mDatabase.child("users").child(getUid()).child("email").setValue(emailET.getText().toString());
                                Toast.makeText(UpdateProfileActivity.this, getResources().getText(R.string.toast_update_email),
                                        Toast.LENGTH_SHORT).show();
                            }
                            if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                reAuthenticate(user.getEmail(), password);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "In order to change your email you must type your current password",Toast.LENGTH_SHORT).show();
                }

            }

        }
    }
    public void setUsername(FirebaseUser user){

        //Log.d(TAG, "Firebase:" + user.getDisplayName());
        //Log.d(TAG, "EditText:" + usernameET.getText().toString());

        if(!user.getDisplayName().equals(usernameET.getText().toString())){
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(usernameET.getText().toString()).build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                    getUserPosts();
                                    Toast.makeText(UpdateProfileActivity.this, getResources().getText(R.string.toast_update_username),
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(UpdateProfileActivity.this, "Error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    public void getAllPosts(){
        mDatabase.child("post-comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshotArray: dataSnapshot.getChildren()) {
                    getUserComments(snapshotArray.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getUserComments(final String key){
        mDatabase.child("post-comments").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshotArrayComment: dataSnapshot.getChildren()){
                    mDatabase.child("post-comments").child(key).child(snapshotArrayComment.getKey()).child("author").setValue(usernameET.getText().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void getUserPosts(){
        mDatabase.child("user-posts").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshotArray: dataSnapshot.getChildren()){
                       Log.d(TAG, "User:" + snapshotArray.getKey());
                        mDatabase.child("posts").child(snapshotArray.getKey()).child("author").setValue(usernameET.getText().toString());
                        mDatabase.child("posts_locations_by_user").child(getUid()).child(snapshotArray.getKey()).child("author").setValue(usernameET.getText().toString());
                        mDatabase.child("users").child(getUid()).child("username").setValue(usernameET.getText().toString());
                        mDatabase.child("user-posts").child(getUid()).child(snapshotArray.getKey()).child("author").setValue(usernameET.getText().toString());
                        getUserComments(snapshotArray.getKey());
                        getAllPosts();
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getUsernameEmailProfilePicture(){
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);
                        Picasso.with(changepictureIV.getContext()).load(user.picture_profile_path).resize(60, 60).into(changepictureIV);
                        usernameET.setText(user.username);
                        emailET.setText(user.email);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile_settings, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_save:
                setUsername(user);
                setEmailPasswordDialog();
                //setNewPassword(user);
                return true;
            case android.R.id.home:
                onBackPressed();
                Intent intent = new Intent(getApplicationContext(), Ofertas.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void getFileSystemIntent() {
        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = UUID.randomUUID().toString() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);
        // Filesystem.
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), RC_TAKE_PICTURE);


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        //Bitmap bp = (Bitmap) data.getExtras().get("data");
        outputFileUri = data.getData();

        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                if (outputFileUri != null) {
                    Log.d(TAG, "get Uri from OnActivitYoNrESULT: " +outputFileUri);
                    uploadFromUri(outputFileUri);
                } else {
                    Log.w(TAG, "File URI is null");
                }
            }
            else if (resultCode == RESULT_CANCELED) {
                Intent cameraIntent = new  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, RC_TAKE_PICTURE);
            }
            else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
                        submitNewPhoto(mDownloadUrl);
                        Picasso.with(changepictureIV.getContext()).load(mDownloadUrl).fit().centerCrop().into(changepictureIV);
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
                        Toast.makeText(UpdateProfileActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();

                        // [END_EXCLUDE]
                    }
                });
    }

    public void submitNewPhoto(final Uri mDownloadUrl){
        mDatabase.child("user-posts").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshotArray: dataSnapshot.getChildren()){
                     mDatabase.child("posts").child(snapshotArray.getKey()).child("user_image_path").setValue( mDownloadUrl.toString());
                     mDatabase.child("user-posts").child(getUid()).child(snapshotArray.getKey()).child("user_image_path").setValue(mDownloadUrl.toString());
                     mDatabase.child("users").child(getUid()).child("picture_profile_path").setValue( mDownloadUrl.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.changepicturetv:
                getFileSystemIntent();
                break;
            case R.id.changePasswordTV:
                Intent i = new Intent(getApplicationContext(), UpdatePasswordActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }
}
