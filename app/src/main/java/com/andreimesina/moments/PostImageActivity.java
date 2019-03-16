package com.andreimesina.moments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
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

import com.andreimesina.moments.utils.SharedPreferencesUtils;

import java.io.File;
import java.io.IOException;

public class PostImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostImageActivity";

    private String currentImagePath;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private EditText mEditTextStory;
    private EditText mEditTextLocation;
    private ImageView mImageView;
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

    private void setViews() {
        mEditTextStory = findViewById(R.id.et_image_story);
        mEditTextLocation = findViewById(R.id.et_image_location);
        mImageView = findViewById(R.id.image_post);
        mBtnSave = findViewById(R.id.btn_save_post);
        mBtnCancel = findViewById(R.id.btn_cancel_post);
    }

    private void initToolbar() {
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

        setTitle("Save image");

        // Add toggle for a cool animation
        mDrawerLayout = findViewById(R.id.drawer_layout_post);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.nav_open, R.string.nav_close);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void getImageFromCamera() {
        Intent intent = getIntent();

        if(intent != null) {
            currentImagePath = intent.getExtras().get("image_path").toString();

            if(mImageView == null) {
                mImageView = findViewById(R.id.image_post);
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath, options);

            if(bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }

            try {
                fixImageViewOrientation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteCurrentImage() {
        File file = new File(currentImagePath);
        file.delete();
    }

    private void setButtonsListener() {
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    private void fixImageViewOrientation() throws IOException {
        ExifInterface exifInterface = new ExifInterface(currentImagePath);

        int rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        switch (rotation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                mImageView.animate().rotation(90).setDuration(0);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                mImageView.animate().rotation(180).setDuration(0);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                mImageView.animate().rotation(270).setDuration(0);
                break;

            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mDrawerToggle.isDrawerIndicatorEnabled() == false) {
            onBackPressed();
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_save_post) {
            SharedPreferencesUtils.setString(this, "image_action",
                    "save");
            SharedPreferencesUtils.setString(this, "image_story",
                    mEditTextStory.getText().toString());
            SharedPreferencesUtils.setString(this, "image_location",
                    mEditTextLocation.getText().toString());
        } else if(v.getId() == R.id.btn_cancel_post) {
            SharedPreferencesUtils.setString(this, "image_action",
                    "cancel");

            deleteCurrentImage();
        }

        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteCurrentImage();
        finish();
    }
}
