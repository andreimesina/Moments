package com.andreimesina.moments.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleSignInUtils {


    public static GoogleSignInOptions getSignInOptionsProfileEmail(String webClientId) {
        return new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestProfile()
                .requestEmail()
                .build();
    }

    public static void signInWithGoogle(Activity activity, GoogleSignInClient client,
                                        final int REQUEST_CODE) {
        Intent intent = client.getSignInIntent();
        ActivityCompat.startActivityForResult(activity, intent, REQUEST_CODE, Bundle.EMPTY);
    }

}
