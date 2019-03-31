package com.andreimesina.moments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.andreimesina.moments.utils.SharedPreferencesUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.io.File;

public class PostEditImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostEditImageActivity";

    private String currentImagePath;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private EditText mEditTextStory;
    private EditText mEditTextLocation;
    private ImageView mImageView;
    private Button mBtnSave;
    private Button mBtnCancel;

    private String mImageFilename;
    private String mImageStory;
    private String mImageLocation;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit_image);

        setViews();

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.getStringExtra("image_story") != null
                    && intent.getStringExtra("image_location") != null) {
                mImageFilename = intent.getStringExtra("image_filename");
                mImageStory = intent.getStringExtra("image_story");
                mImageLocation = intent.getStringExtra("image_location");
                loadFields();
                isEdit = true;
            }

            currentImagePath = intent.getStringExtra("image_url");
            loadImage();
        }

        initToolbar();
        setFieldsListener();
        setButtonsListener();
        setImageListener();
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

        if(SharedPreferencesUtils.getString(this, "image_action").equalsIgnoreCase("cancel")) {
            deleteCurrentImage();
        }
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
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Add navigation drawer button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if(isEdit) {
            setTitle("Edit Moment");
        } else {
            setTitle("Save Moment");
        }

        // Add toggle for a cool animation
        mDrawerLayout = findViewById(R.id.drawer_layout_post);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.nav_open, R.string.nav_close);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setFieldsListener() {
        mEditTextStory.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    checkStory();
                }
            }
        });

        mEditTextLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    checkLocation();
                }
            }
        });

        TextWatcher storyTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkStory();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        TextWatcher locationTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkLocation();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        mEditTextStory.addTextChangedListener(storyTextWatcher);
        mEditTextLocation.addTextChangedListener(locationTextWatcher);
    }

    private void setButtonsListener() {
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);

        disableSaveButton();
    }

    private void setImageListener() {
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostEditImageActivity.this, ViewImageActivity.class);

                intent.putExtra("image_url", currentImagePath);
                startActivity(intent);
            }
        });

    }

    private void loadFields() {
        if(mImageStory != null && mImageStory.length() > 0) {
            mEditTextStory.setText(mImageStory);
        }

        if(mImageLocation != null && mImageStory.length() > 0) {
            mEditTextLocation.setText(mImageLocation);
        }
    }

    private void loadImage() {
        if (currentImagePath != null) {
            Glide.with(this)
                    .load(currentImagePath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(mImageView);
        }
    }

    private void deleteCurrentImage() {
        File file = new File(currentImagePath);
        file.delete();
    }

    private void checkStory() {
        if(isFieldEmpty(mEditTextStory)) {
            mEditTextStory.setError("What is your story?");
            disableSaveButton();
        } else if(isFieldTooLong(mEditTextStory, 100)) {
            mEditTextStory.setError("Your story must be max. 100 characters long!");
            disableSaveButton();
        } else if(isEdit && isSameStory() && isSameLocation()) {
            mEditTextStory.setError("Your story is the same!");
            disableSaveButton();
        } else {
            mEditTextStory.setError(null);
            enableSaveButton();
        }
    }

    private void checkLocation() {
        if(isFieldTooLong(mEditTextLocation, 25)) {
            mEditTextLocation.setError("Your location must be max. 25 characters long!");
            disableSaveButton();
        } else if(isGoodStory()) {
            mEditTextLocation.setError(null);
            enableSaveButton();
        }
    }

    private boolean isGoodStory() {
        if(isFieldEmpty(mEditTextStory)) {
            return false;
        } else if(isFieldTooLong(mEditTextStory, 100)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isFieldEmpty(EditText et) {
        return et.getText().toString().length() == 0;
    }

    private boolean isFieldTooLong(EditText et, int limit) {
        if(et.getText().toString().length() > limit) {
            return true;
        }

        return false;
    }

    private boolean isSameStory() {
        if(mImageStory != null && mImageStory.length() > 0) {
            if(mEditTextStory.getText().toString().equals(mImageStory)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSameLocation() {
        if(mImageLocation != null && mImageLocation.length() > 0) {
            if(mEditTextLocation.getText().toString().equals(mImageLocation)) {
                return true;
            }
        }

        return false;
    }

    private void disableSaveButton() {
        mBtnSave.setEnabled(false);
        mBtnSave.setBackground(getResources().getDrawable(R.drawable.btn_disabled_shape));
    }

    private void enableSaveButton() {
        mBtnSave.setEnabled(true);
        mBtnSave.setBackground(getResources().getDrawable(R.drawable.btn_shape));
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
            if(isEdit == false) {
                SharedPreferencesUtils.setString(this, "image_action",
                        "save");
            } else {
                SharedPreferencesUtils.setString(this, "image_action",
                        "edit");
                SharedPreferencesUtils.setString(this, "image_filename",
                        mImageFilename);
            }

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

    @Override
    public void onBackPressed() {
        SharedPreferencesUtils.setString(this, "image_action", "cancel");
        super.onBackPressed();
    }
}
