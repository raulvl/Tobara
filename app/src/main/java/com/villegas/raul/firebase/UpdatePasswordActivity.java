package com.villegas.raul.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText currpassET, newpassET, cnewpassET;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private StorageReference mStorageRef;
    private final String TAG = "ON";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Firebase Storage and Database configs
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReferenceFromUrl("gs://fir-cazador.appspot.com/");
        currpassET = (EditText) findViewById(R.id.currPasswordField);
        newpassET = (EditText) findViewById(R.id.newPasswordField);
        cnewpassET = (EditText) findViewById(R.id.cNewPasswordField);


    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    public void setNewPassword(final FirebaseUser user){
        if( !currpassET.getText().toString().isEmpty() && !newpassET.getText().toString().isEmpty() && !cnewpassET.getText().toString().isEmpty()){
            Log.d(TAG, "CurrPass:" + currpassET.getText().toString());
            Log.d(TAG, "NewPass:" + newpassET.getText().toString());
            Log.d(TAG, "CNewPass:" + cnewpassET.getText().toString());
            if(newpassET.getText().toString().equals(cnewpassET.getText().toString())){
                user.updatePassword(cnewpassET.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                            //mDatabase.child("users").child(getUid()).child("email").setValue(emailET.getText().toString());
                            Toast.makeText(UpdatePasswordActivity.this, getResources().getText(R.string.toast_update_pass),
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                            reAuthenticate(user.getEmail(), currpassET.getText().toString());
                        }
                    }
                });
            }else{
                Toast.makeText(UpdatePasswordActivity.this, "Your password doesn't match",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void reAuthenticate(String email, String password){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
        firebaseUser.reauthenticate(authCredential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User reauthenticated.");
                            setNewPassword(firebaseUser);
                        } else {
                            Log.d(TAG, "Error on User reauthenticated.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdatePasswordActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
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
                //setUsername(user);
                //setEmail(user);
                reAuthenticate(user.getEmail(), currpassET.getText().toString());
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


    @Override
    public void onClick(View view) {

    }
}
