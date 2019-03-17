package com.andreimesina.moments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andreimesina.moments.fragments.AboutUsFragment;
import com.andreimesina.moments.fragments.ContactFragment;
import com.andreimesina.moments.fragments.HomeFragment;
import com.andreimesina.moments.utils.ActivityUtils;
import com.andreimesina.moments.utils.GoogleSignInUtils;
import com.andreimesina.moments.utils.SharedPreferencesUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String HOME_FRAGMENT = "home";
    // private static final String FAVOURITE_FRAGMENT = "favourite";
    private static final String ABOUT_US_FRAGMENT = "about us";
    private static final String CONTACT_FRAGMENT = "contact";

    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_CAMERA = 1;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;

    private String currentImagePath;
    private String compressedImagePath;
    private Uri currentImageUri;
    private Uri compressedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init db connections
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
        initNavigationView();

        // "+" button to add images
        initFloatingActionButton();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // sync navigation icon state
        mDrawerToggle.syncState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        String imageAction = SharedPreferencesUtils.getString(this, "image_action");
        if(imageAction.equalsIgnoreCase("save")) {
            // TODO: add image to list
            try {
                compressImage();
                deleteBigImage();

                String filename = SharedPreferencesUtils.getString(this, "image_filename");
                SharedPreferencesUtils.deleteValue(this, "image_filename");
                SharedPreferencesUtils.setString(this, "image_filename",
                        filename.replace(".jpeg", "c.jpeg"));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                uploadImageToStorage();
            }

            SharedPreferencesUtils.deleteValue(this, "image_action");
            SharedPreferencesUtils.deleteValue(this, "image_story");
            SharedPreferencesUtils.deleteValue(this, "image_location");
            SharedPreferencesUtils.deleteValue(this, "image_filename");

        } else if(imageAction.equalsIgnoreCase("cancel")) {
            SharedPreferencesUtils.deleteValue(this, "image_action");
            SharedPreferencesUtils.deleteValue(this, "image_filename");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            goToPostActivity();
        }
    }

    private void goToPostActivity() {
        Intent intent = new Intent(this, PostImageActivity.class);
        intent.putExtra("image_uri", currentImageUri);
        intent.putExtra("image_path", currentImagePath);

        startActivity(intent);
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
        mStorage = FirebaseStorage.getInstance();
    }

    private void initGoogle() {
        mGoogleSignInOptions = GoogleSignInUtils.getSignInOptionsProfileEmail(getString(R.string.web_client_id));
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void initNavigationView() {
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setCheckedItem(R.id.item_nav_home);

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();

                        Integer itemId = menuItem.getItemId();

                        switch(itemId) {
                            case R.id.item_nav_home:
                                Log.d(TAG, "onNavigationItemSelected: home");
                                HomeFragment homeFragment = new HomeFragment();
                                putFragment(homeFragment, HOME_FRAGMENT);
                                break;

                            case R.id.item_nav_favourite:
                                // TODO
                                break;

                            case R.id.item_nav_about_us:
                                Log.d(TAG, "onNavigationItemSelected: about us");
                                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                                putFragment(aboutUsFragment, ABOUT_US_FRAGMENT);
                                break;

                            case R.id.item_nav_contact:
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
    }

    private void initFloatingActionButton() {
        FloatingActionButton fab = findViewById(R.id.fab_add_image);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPermission()) {
                    dispatchTakePictureIntent();
                } else {
                    requestPermission();
                }
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentImagePath = image.getAbsolutePath();
        currentImageUri = Uri.fromFile(image);

        SharedPreferencesUtils.setString(this, "image_filename", imageFileName);

        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the image should go
            File imageFile = null;
            try {
                imageFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imageFile != null) {
                Uri imageURI = FileProvider.getUriForFile(this,
                        "com.andreimesina.moments.fileprovider",
                        imageFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void compressImage() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath, options);
        compressedImagePath = currentImagePath.replace(".jpg", "c.jpg");
        compressedImageUri = Uri.fromFile(new File(compressedImagePath));

        OutputStream outputStream = new FileOutputStream(compressedImagePath);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        ActivityUtils.fixImageExifOrientation(currentImagePath, compressedImagePath);

        outputStream.flush();
        outputStream.close();
    }

    private void uploadImageToStorage() {
        StorageReference storageRef = mStorage.getReference();
        StorageReference imgRef = storageRef.child("/users/" + mAuth.getUid() + "/images/"
                + SharedPreferencesUtils.getString(this, "image_filename"));

        String imgStory = SharedPreferencesUtils.getString(this, "image_story");
        String imgLocation = SharedPreferencesUtils.getString(this, "image_location");

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("test", "dev")
                .setCustomMetadata("filename", SharedPreferencesUtils
                        .getString(this, "image_filename"))
                .setCustomMetadata("story", imgStory)
                .setCustomMetadata("location", imgLocation)
                .build();

        InputStream stream = null;
        try {
            stream = new FileInputStream(compressedImagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = imgRef.putStream(stream, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Image upload failed!",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

            }
        });
    }

    private void deleteBigImage() {
        File img = new File(currentImagePath);
        img.delete();
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
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                mDrawerToggle.setDrawerIndicatorEnabled(false);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
