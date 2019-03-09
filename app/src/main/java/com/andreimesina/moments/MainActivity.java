package com.andreimesina.moments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.andreimesina.moments.fragments.AboutUsFragment;
import com.andreimesina.moments.fragments.ContactFragment;
import com.andreimesina.moments.fragments.HomeFragment;
import com.andreimesina.moments.utils.ActivityUtils;
import com.andreimesina.moments.utils.GoogleSignInUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String HOME_FRAGMENT = "home";
//    private static final String FAVOURITE_FRAGMENT = "favourite";
    private static final String ABOUT_US_FRAGMENT = "about us";
    private static final String CONTACT_FRAGMENT = "contact";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;


    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFirebase();
        initGoogle();

        // Starting screen with "moments"
        HomeFragment homeFragment = new HomeFragment();
        putFragment(homeFragment, HOME_FRAGMENT);

        // Create toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Add navigation drawer button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Add toggle for a cool animation
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.nav_open, R.string.nav_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Navigation item selection listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();

                        Integer itemId = menuItem.getItemId();

                        switch(itemId) {
                            case R.id.nav_home:
                                Log.d(TAG, "onNavigationItemSelected: home");
                                HomeFragment homeFragment = new HomeFragment();
                                putFragment(homeFragment, HOME_FRAGMENT);
                                break;

                            case R.id.nav_favourite:
                                // TODO
                                break;

                            case R.id.nav_about_us:
                                Log.d(TAG, "onNavigationItemSelected: about us");
                                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                                putFragment(aboutUsFragment, ABOUT_US_FRAGMENT);
                                break;

                            case R.id.nav_contact:
                                Log.d(TAG, "onNavigationItemSelected: contact");
                                ContactFragment contactFragment = new ContactFragment();
                                putFragment(contactFragment, CONTACT_FRAGMENT);
                                break;

                            default:
                                Log.d(TAG, "onNavigationItemSelected: bad id: " + menuItem.getItemId());
                                return false;
                        }

                        return true;
                    }
                }
        );

        FloatingActionButton fab = findViewById(R.id.fab_add_image);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()) {
                    takePictureIntent();
                } else {
                    requestPermission();
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // sync navigation icon state
        mDrawerToggle.syncState();
    }

    public void btnSignOutOnClick(View view) {
        if(isSignedIn()) {

            if(mAuth.getCurrentUser() != null) {
                mAuth.signOut();
            }

            if(mGoogleSignInClient != null && mGoogleSignInAccount != null) {
                mGoogleSignInClient.signOut();
            }
        }

        finishAffinity();
        ActivityUtils.goToActivity(MainActivity.this, LoginActivity.class);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initGoogle() {
        mGoogleSignInOptions = GoogleSignInUtils.getSignInOptionsProfileEmail(getString(R.string.web_client_id));
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    private void takePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void putFragment(Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.replace(R.id.placeholder, fragment, fragmentTag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        // Show back arrow for navigation if not in home screen
        if (getSupportActionBar() != null) {
            if(fragmentTag.equalsIgnoreCase(HOME_FRAGMENT)) {
                mDrawerToggle.setDrawerIndicatorEnabled(true);
            } else {
                mDrawerToggle.setDrawerIndicatorEnabled(false);

            }
        }
    }

    private boolean isSignedIn() {
        if(mGoogleSignInAccount != null || mAuth.getCurrentUser() != null) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // if navigation icon is menu
        if (mDrawerToggle.onOptionsItemSelected(item) &&
        mDrawerToggle.isDrawerIndicatorEnabled()) {
            return true;

        // if navigation icon is back arrow
        } else if(mDrawerToggle.isDrawerIndicatorEnabled() == false) {
            onBackPressed();
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        View drawer = findViewById(R.id.nav_view);
        Fragment home = getSupportFragmentManager().findFragmentByTag(HOME_FRAGMENT);

        // if navigation menu is open, close it
        if(drawer != null && mDrawerLayout.isDrawerOpen(drawer)) {
            mDrawerLayout.closeDrawers();

        // if navigation menu is closed and app is not in home screen
        } else if(home != null && home.isVisible() == false) {
            putFragment(home, home.getTag());

        // if navigation menu is closed and app is in home screen
        } else {
            moveTaskToBack(true);
        }
    }
}
