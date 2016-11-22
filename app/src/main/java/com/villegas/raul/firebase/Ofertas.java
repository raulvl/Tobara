package com.villegas.raul.firebase;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.villegas.raul.firebase.fragment.PostListFragment;
import com.villegas.raul.firebase.fragment.MyPostsFragment;
import com.villegas.raul.firebase.fragment.MyTopPostsFragment;
import com.villegas.raul.firebase.fragment.RecentPostsFragment;
import com.villegas.raul.firebase.models.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import static android.os.Build.VERSION_CODES.M;

public class Ofertas extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private Intent i;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // usuarios y posts
    private String mUserId;
    private String itemsUrl;
    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private ImageView imagen;
    private StorageReference mStorageRef;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int YOUR_SELECT_PICTURE_REQUEST_CODE = 1;
    private Uri outputFileUri;

    private static final int RC_TAKE_PICTURE = 101;
    private static final int RC_STORAGE_PERMS = 102;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private File file;
    private ImageButton imageButton;
    private ImageView navProfilePicture;
    private TextView usernameNV, emailNV;
    // CallbackManager callbackManager;
    // ShareDialog shareDialog;
    private DatabaseReference mDatabase;
    private static final String TAG = "Getting user:";
    private FloatingActionsMenu FabMenu;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofertas);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // calling to the db

        mDatabase = FirebaseDatabase.getInstance().getReference();
        context = Ofertas.this;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Defining layout of navView
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nview = navigationView.getHeaderView(0);
        navProfilePicture = (ImageView) nview.findViewById(R.id.imageViewNav);
        usernameNV = (TextView) nview.findViewById(R.id.textviewUsername);
        emailNV = (TextView) nview.findViewById(R.id.textViewEmail);

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new RecentPostsFragment(),
                    new MyPostsFragment(),
                    new MyTopPostsFragment(),
            };

            private final String[] mFragmentNames = new String[]{
                    getResources().getString(R.string.recent_offers),
                    getResources().getString(R.string.my_offers),
                    getResources().getString(R.string.top_offers)
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        goTop(mViewPager, tabLayout);
        //imagen = (ImageView) findViewById(R.id.imageViewPost);
        FabMenu = (FloatingActionsMenu) findViewById(R.id.menu_fab);









        findViewById(R.id.accion_buscar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCameraIntent();
                FabMenu.collapse();
            }
        });
        findViewById(R.id.accion_carrito).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFileSystemIntent();
                FabMenu.collapse();
            }
        });


        itemsUrl = "https://fir-cazador.firebaseio.com/" + "/users/" + mUserId + "/items";
        mAuth = FirebaseAuth.getInstance();
        // final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        // [START auth_state_listener]

        if (mAuth.getCurrentUser() == null) {
            loadMainView();
        }
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    emailNV.setText(user.getEmail());
                    setPictureProfileDrawable(user);
                    //Picasso.with(navProfilePicture.getContext()).load(user.getPhotoUrl()).resize(110, 110).onlyScaleDown().centerCrop().into(navProfilePicture);
                    Log.d("Ofertas", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("Ofertas", "onAuthStateChanged:signed_out");
                    mAuth.signOut();
                }
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        };


    }

    private void goTop(final ViewPager v, TabLayout t){
        t.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                v.setCurrentItem(tab.getPosition(), true);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                try {
                    Fragment f = mPagerAdapter.getItem(tab.getPosition());
                    if (f != null) {
                        View fragmentView = f.getView();
                        RecyclerView mRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.messages_list);//mine one is RecyclerView
                        if (mRecyclerView != null)
                            mRecyclerView.smoothScrollToPosition(mRecyclerView.getAdapter().getItemCount());
                    }
                } catch (NullPointerException npe) {
                }
            }
        });
    }

    private void setPictureProfileDrawable(FirebaseUser user) {

        final String userId = getUid();
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user value
                            User user = dataSnapshot.getValue(User.class);

                            Picasso.with(navProfilePicture.getContext()).load(user.picture_profile_path).resize(110, 110).onlyScaleDown().centerCrop().into(navProfilePicture);
                            usernameNV.setText(user.username);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });

    }

    private void getCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID().toString() + ".jpg");
        mFileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        startActivityForResult(intent, YOUR_SELECT_PICTURE_REQUEST_CODE);


    }


    private String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            String str = cursor.getString(index);
            cursor.close();
            return str;
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
                "Select Picture"), YOUR_SELECT_PICTURE_REQUEST_CODE);


    }

   /*
   {
  "rules": {
    "users":{
      ".read": "auth != null",
    	".write": "auth != null"
    }
    "posts":{
      ".indexOn": ["title"]
    }
  }
}
   last rules :
   {
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
    */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null || data.getData() == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = mFileUri;
                    File imageFile = new File(SiliCompressor.with(context).compress(getRealPathFromURI(selectedImageUri)));
                    Uri newImageUri = Uri.fromFile(imageFile);
                    Intent i = new Intent(getApplicationContext(), NewPostActivity.class);
                    i.putExtra("ruta", newImageUri.toString());
                    startActivity(i);
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                    Log.d("ImageURI", selectedImageUri.getLastPathSegment());
                    Intent i = new Intent(getApplicationContext(), NewPostActivity.class);
                    i.putExtra("ruta", selectedImageUri.toString());
                    startActivity(i);
                }
            }
        }
    }

    private void loadMainView() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ofertas, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                PagerAdapter pagerAdapter = (PagerAdapter) mViewPager.getAdapter();
                for (int i = 0; i < pagerAdapter.getCount(); i++) {

                    Fragment viewPagerFragment = (Fragment) mViewPager.getAdapter().instantiateItem(mViewPager, i);
                    if (viewPagerFragment != null && viewPagerFragment.isAdded()) {

                        if (viewPagerFragment instanceof PostListFragment) {
                            PostListFragment pFragment = (PostListFragment) viewPagerFragment;
                            if (pFragment != null) {
                                pFragment.closeMenu();

                            }
                        }
                    }
                }

                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                PagerAdapter pagerAdapter = (PagerAdapter) mViewPager.getAdapter();
                for (int i = 0; i < pagerAdapter.getCount(); i++) {

                    Fragment viewPagerFragment = (Fragment) mViewPager.getAdapter().instantiateItem(mViewPager, i);
                    if (viewPagerFragment != null && viewPagerFragment.isAdded()) {

                        if (viewPagerFragment instanceof PostListFragment) {
                            PostListFragment pFragment = (PostListFragment) viewPagerFragment;
                            if (pFragment != null) {
                                if (query.isEmpty()) {
                                    searchView.setIconified(true);
                                }
                                pFragment.beginSearch(query);

                            }
                        }
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Here u can get the value "query" which is entered in the search box.

                // Log.e("Query",query);


                return false;

            }
        });
        searchView.setIconified(true);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_logout:
                loadMainView();
                //startActivity(new Intent(this,MainActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void log_out() {
        mAuth.signOut();
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Intent i = new Intent(this, UpdateProfileActivity.class);
            startActivity(i);

        }else if(id == R.id.nav_profile){
            Intent i = new Intent(this, UserProfileActivity.class);
            startActivity(i);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
