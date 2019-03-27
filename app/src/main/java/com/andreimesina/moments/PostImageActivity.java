package com.andreimesina.moments;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.andreimesina.moments.utils.ActivityUtils;
import com.andreimesina.moments.utils.SharedPreferencesUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dmallcott.dismissibleimageview.DismissibleImageView;

import java.io.File;

public class PostImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostImageActivity";

    private String currentImagePath;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private AppBarLayout mAppBar;
    private EditText mEditTextStory;
    private EditText mEditTextLocation;
    private DismissibleImageView mImageView;
    private Button mBtnSave;
    private Button mBtnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        setViews();
        initToolbar();
        getImageFromCamera();
        setButtonsListener();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // sync navigation icon state
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(SharedPreferencesUtils.getString(this, "image_action").equalsIgnoreCase("save")) {
            deleteCurrentImage();
        }
        finish();
    }

    private void setViews() {
        mAppBar = findViewById(R.id.layout_appbar);
        mEditTextStory = findViewById(R.id.et_image_story);
        mEditTextLocation = findViewById(R.id.et_image_location);
        mImageView = findViewById(R.id.image_post);
        mBtnSave = findViewById(R.id.btn_save_post);
        mBtnCancel = findViewById(R.id.btn_cancel_post);
    }

    private void initToolbar() {
        // Create toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Add navigation drawer button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Save image");

        // Add toggle for a cool animation
        mDrawerLayout = findViewById(R.id.drawer_layout_post);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.nav_open, R.string.nav_close);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setButtonsListener() {
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    private void getImageFromCamera() {
        Intent intent = getIntent();

        if (intent != null) {
            currentImagePath = intent.getExtras().get("image_path").toString();
            setImageScale();

            if (currentImagePath != null) {
                Glide.with(this)
                        .load(currentImagePath)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(mImageView);
            }

        }
    }

    private void setImageScale() {
        int orientation;

        try {
            orientation = ActivityUtils.getImageOrientation(currentImagePath);
        } catch (Exception e) {
            return ;
        }

        if(orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void deleteCurrentImage() {
        File file = new File(currentImagePath);
        file.delete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.isDrawerIndicatorEnabled() == false) {
            onBackPressed();
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save_post) {
            SharedPreferencesUtils.setString(this, "image_action",
                    "save");
            SharedPreferencesUtils.setString(this, "image_story",
                    mEditTextStory.getText().toString());
            SharedPreferencesUtils.setString(this, "image_location",
                    mEditTextLocation.getText().toString());
            finish();
        } else if (v.getId() == R.id.btn_cancel_post) {
            SharedPreferencesUtils.setString(this, "image_action",
                    "cancel");

            deleteCurrentImage();
            finish();
        }
    }
}
