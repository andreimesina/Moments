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

        setTitle("Save image");

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
                    if(isGoodStory() && isGoodLocation()) {
                        enableSaveButton();
                    } else {
                        disableSaveButton();
                    }
                }
            }
        });

        mEditTextLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    if(isGoodStory() && isGoodLocation()) {
                        enableSaveButton();
                    } else {
                        disableSaveButton();
                    }
                }
            }
        });

        TextWatcher storyTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isGoodStory() && isGoodLocation()) {
                    enableSaveButton();
                } else {
                    disableSaveButton();
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        TextWatcher locationTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(isGoodStory() && isGoodLocation()) {
                    enableSaveButton();
                } else {
                    disableSaveButton();
                }
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
                Intent intent = new Intent(PostImageActivity.this, ViewImageActivity.class);

                intent.putExtra("URL", currentImagePath);
                startActivity(intent);
            }
        });

    }

    private void getImageFromCamera() {
        Intent intent = getIntent();

        if (intent != null) {
            currentImagePath = intent.getExtras().get("image_path").toString();

            if (currentImagePath != null) {
                Glide.with(this)
                        .load(currentImagePath)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(mImageView);
            }

        }
    }

    private void deleteCurrentImage() {
        File file = new File(currentImagePath);
        file.delete();
    }

    private boolean isGoodStory() {
        if(mEditTextStory.getText().toString().length() == 0) {
            mEditTextStory.setError("Tell us your story!");
            return false;
        } else if(mEditTextStory.getText().toString().length() > 100) {
            mEditTextStory.setError("Story must be max. 100 characters long!");
            return false;
        } else {
            mEditTextStory.setError(null);
            return true;
        }
    }

    private boolean isGoodLocation() {
        if(mEditTextLocation.getText().toString().length() == 0) {
            mEditTextLocation.setError("Where have you been?");
            return false;
        } else if(mEditTextLocation.getText().toString().length() > 25) {
            mEditTextLocation.setError("Location must be max. 25 characters long!");
            return false;
        } else {
            mEditTextLocation.setError(null);
            return true;
        }
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
            SharedPreferencesUtils.setString(this, "image_action",
                    "save");
            String story = (mEditTextStory.getText().toString().length() > 100)
                    ? mEditTextStory.getText().toString().substring(0, 100) + "..."
                    : mEditTextStory.getText().toString();
            SharedPreferencesUtils.setString(this, "image_story",
                    story);
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
