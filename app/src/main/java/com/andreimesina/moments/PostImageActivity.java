package com.andreimesina.moments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class PostImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostImageActivity";
    public static final int CODE_SAVE = 2;
    public static final int CODE_CANCEL = 3;

    private String currentPhotoPath;

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
        getImageFromCamera();
        setButtonsListener();

    }

    private void setViews() {
        mEditTextStory = findViewById(R.id.et_image_story);
        mEditTextLocation = findViewById(R.id.et_image_location);
        mImageView = findViewById(R.id.image_post);
        mBtnSave = findViewById(R.id.btn_save_post);
        mBtnCancel = findViewById(R.id.btn_cancel_post);
    }

    private void getImageFromCamera() {
        Intent intent = getIntent();
        if(intent != null) {
            Uri imageUri = Uri.parse(String.valueOf(intent.getExtras().get("image_uri")));
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

    private void setButtonsListener() {
        mBtnSave.setOnClickListener(this);
        mBtnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_save_post) {
            Intent intent = new Intent(PostImageActivity.this, MainActivity.class);
            if(mEditTextStory.getText().toString() != null) {
                intent.putExtra("Story", mEditTextStory.getText().toString());
            }

            if(mEditTextLocation.getText().toString() != null) {
                intent.putExtra("Location", mEditTextLocation.getText().toString());
            }

            startActivityForResult(intent, CODE_SAVE);
            finish();
        } else if(v.getId() == R.id.btn_cancel_post) {
            Intent intent = new Intent(PostImageActivity.this, MainActivity.class);

            startActivityForResult(intent, CODE_CANCEL);
            finish();
        }
    }
}
