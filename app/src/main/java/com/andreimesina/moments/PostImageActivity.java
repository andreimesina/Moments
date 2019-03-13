package com.andreimesina.moments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.File;

public class PostImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostImageActivity";

    private String currentPhotoPath;

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
            Uri imageUri = Uri.parse(String.valueOf(intent.getExtras().get("image_uri")));
            currentPhotoPath = intent.getExtras().get("image_path").toString();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(mImageView == null) {
                mImageView = findViewById(R.id.image_post);
            }

            if(bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    private void deleteCurrentPhoto() {
        File file = new File(currentPhotoPath);
        file.delete();
    }

    private void setButtonsListener() {
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
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
        Intent intent = new Intent(PostImageActivity.this, MainActivity.class);

        if(v.getId() == R.id.btn_save_post) {
            if(mEditTextStory.getText().toString() != null) {
                intent.putExtra("Story", mEditTextStory.getText().toString());
            }

            if(mEditTextLocation.getText().toString() != null) {
                intent.putExtra("Location", mEditTextLocation.getText().toString());
            }

            finish();
        } else if(v.getId() == R.id.btn_cancel_post) {
            deleteCurrentPhoto();

            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteCurrentPhoto();
        finish();
    }
}
