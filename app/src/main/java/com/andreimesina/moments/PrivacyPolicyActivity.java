package com.andreimesina.moments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import us.feras.mdv.MarkdownView;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        MarkdownView markdownView = findViewById(R.id.text_privacy_policy);
        markdownView.loadMarkdownFile("file:///android_asset/PRIVACY_POLICY.md");
    }

}
