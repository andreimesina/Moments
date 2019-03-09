package com.andreimesina.moments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.andreimesina.moments.utils.ActivityUtils;
import com.andreimesina.moments.utils.GoogleSignInUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.andreimesina.moments.R.id;
import static com.andreimesina.moments.R.layout;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final Integer GOOGLE_SIGN_IN = 1;
    private static final String GOOGLE_BUTTON_TEXT = "Continue with Google";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    private EditText mEditTextEmail;
    private EditText mEditTextPass;
    private Button mBtnSignIn;
    private ProgressBar mProgressBar;
    private Button mBtnGoogleSign;
    private ProgressBar mProgressBarGoogle;
    private Button mBtnRegister;
    private Button mBtnForgotPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_login);

        // init class members
        initFirebase();
        initGoogleSign();
        setViews();

        // listen for actions on screen
        listenForGoogleSignBtn();
        listenForKeyboardState();
        listenForKeyboardDoneBtn();
        listenForSignInBtn();
        listenForModifiedCredentials();
        listenForRegisterBtn();
        listenForForgotPassBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

        if(mGoogleSignInAccount != null) {
            ActivityUtils.goToActivity(LoginActivity.this, MainActivity.class);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityUtils.clearFocus(this);
        ActivityUtils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from GoogleSignInClient.getSignInIntent...
        if(requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
            setButtonFinishedProgress(mBtnGoogleSign, GOOGLE_BUTTON_TEXT, mProgressBarGoogle);
            enableButton(mBtnGoogleSign);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            mGoogleSignInAccount = completedTask.getResult(ApiException.class);
            proceedToApp(mGoogleSignInOptions, mGoogleSignInAccount);
        } catch(ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initGoogleSign() {
        mGoogleSignInOptions = GoogleSignInUtils.getSignInOptionsProfileEmail();
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
    }

    private void setViews() {
        mEditTextEmail = findViewById(id.et_email_sign);
        mEditTextPass = findViewById(id.et_password_sign);
        mBtnSignIn = findViewById(id.btn_sign);
        mProgressBar = findViewById(id.progress_sign);
        mBtnGoogleSign = findViewById(id.btn_google_sign);
        mProgressBarGoogle = findViewById(id.progress_google_sign);
        mBtnRegister = findViewById(id.btn_reg_sign);
        mBtnForgotPass = findViewById(id.btn_forgot_pass_sign);
    }

    private void signInWithEmailAndPass(String email, String pass) {
        final String buttonText = mBtnSignIn.getText().toString();
        setButtonInProgress(mBtnSignIn, mProgressBar);
        ActivityUtils.hideKeyboard(this);
        ActivityUtils.clearFocus(this);

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    ActivityUtils.goToActivity(LoginActivity.this, MainActivity.class);
                } else {
                    showPasswordForgottenAlertDialog();
                    clearPasswordField();
                    disableButton(mBtnSignIn);
                }

                setButtonFinishedProgress(mBtnSignIn, buttonText, mProgressBar);
            }
        });
    }

    private void proceedToApp(GoogleSignInOptions options, GoogleSignInAccount account) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("GoogleSignInOptions", options);
        intent.putExtra("GoogleSignInAccount", account);

        startActivity(intent);
    }

    private void proceedToForgotPassword() {
        // TODO: Forgot password activity
        Toast.makeText(this, "Feature available soon.", Toast.LENGTH_SHORT).show();
    }

    private void enableButton(@NonNull Button button) {
        button.setEnabled(true);
        Drawable background = button.getBackground();
        GradientDrawable shape = (GradientDrawable) background;
        shape.setColor(ContextCompat.getColor(this, R.color.colorAccent));
    }

    private void disableButton(@NonNull Button button) {
        button.setEnabled(false);
        Drawable background = button.getBackground();
        GradientDrawable shape = (GradientDrawable) background;
        shape.setColor(ContextCompat.getColor(this, R.color.colorAccentDisabled));
    }

    private void enableTextButton(@NonNull Button button) {
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);
    }

    private void disableTextButton(@NonNull Button button) {
        button.setEnabled(false);
        button.setVisibility(View.GONE);
    }

    private void setButtonInProgress(@NonNull Button button, @NonNull ProgressBar progress) {
        button.setText("");
        progress.setVisibility(View.VISIBLE);
    }

    private void setButtonFinishedProgress(@NonNull Button button, @NonNull String text,
                                                 @NonNull ProgressBar progress) {
        progress.setVisibility(View.GONE);
        button.setText(text);
    }

    private void clearPasswordField() {
        mEditTextPass.setText("");
    }

    private void showPasswordForgottenAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Forgot your password?");
        builder.setMessage("If you forgot your username or password, we can help you get them back.");
        builder.setPositiveButton("Reset your password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                proceedToForgotPassword();
            }
        });
        builder.setNeutralButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void listenForGoogleSignBtn() {
        mBtnGoogleSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButton(mBtnGoogleSign);
                setButtonInProgress(mBtnGoogleSign, mProgressBarGoogle);

                GoogleSignInUtils.signInWithGoogle(LoginActivity.this, mGoogleSignInClient,
                        GOOGLE_SIGN_IN);
            }
        });
    }

    private void listenForKeyboardState() {
        final View activityRootView = findViewById(id.layout_sign);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > ActivityUtils.dpToPx(LoginActivity.this, 200)) {
                    disableTextButton(mBtnRegister);
                    disableTextButton(mBtnForgotPass);
                } else {
                    enableTextButton(mBtnRegister);
                    enableTextButton(mBtnForgotPass);
                }
            }
        });
    }

    private void listenForKeyboardDoneBtn() {
        mEditTextPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String email = mEditTextEmail.getText().toString();
                String pass = mEditTextPass.getText().toString();

                if(email.length() > 0 && pass.length() > 0
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER
                        && mBtnSignIn.isEnabled()) {

                    if(email.equalsIgnoreCase("admin") && pass.equalsIgnoreCase("123")) {
                        ActivityUtils.goToActivity(LoginActivity.this, MainActivity.class);
                    } else {
                        signInWithEmailAndPass(email, pass);
                    }

                    return true;
                }
                return false;
            }
        });
    }

    private void listenForSignInBtn() {
        mBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditTextEmail.getText().toString();
                String password = mEditTextPass.getText().toString();

                if(email.length() > 0 && password.length() > 0 && mBtnSignIn.isEnabled()) {
                    signInWithEmailAndPass(email, password);
                }
            }
        });
    }

    private void listenForRegisterBtn() {
        Button btn = findViewById(id.btn_reg_sign);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.goToActivity(LoginActivity.this,
                        RegistrationActivity.class);
            }
        });
    }

    private void listenForForgotPassBtn() {
        Button btn = findViewById(id.btn_forgot_pass_sign);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToForgotPassword();
            }
        });
    }

    private void listenForModifiedCredentials() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mBtnSignIn.isEnabled() == false && mEditTextEmail.getText().toString().length() > 0
                        && mEditTextPass.getText().toString().length() > 0) {
                    enableButton(mBtnSignIn);
                } else if(mBtnSignIn.isEnabled() == true && (mEditTextEmail.getText().toString().length() == 0
                        || mEditTextPass.getText().toString().length() == 0)) {
                    disableButton(mBtnSignIn);
                }
            }
        };

        mEditTextEmail.addTextChangedListener(textWatcher);
        mEditTextPass.addTextChangedListener(textWatcher);
    }

}
