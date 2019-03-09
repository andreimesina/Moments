package com.andreimesina.moments;

import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.andreimesina.moments.utils.ActivityUtils;
import com.andreimesina.moments.utils.GoogleSignInUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";
    private static final Integer GOOGLE_SIGN_IN = 1;
    private static final String GOOGLE_BUTTON_TEXT = "Continue with Google";

    private FirebaseAuth mAuth;

    private GoogleSignInOptions mGoogleSignInOptions;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;

    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private EditText mEditTextPasswordConfirm;
    private Button mBtnGoogleSign;
    private ProgressBar mProgressBarGoogle;
    private Button mBtnConfirm;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // init views class members
        initFirebase();
        initGoogleSign();
        setViews();

        // listen for screen actions
        listenForGoogleSignBtn();
        //listenForKeyboardState();
        listenForKeyboardDoneBtn();
        listenForConfirmBtn();
        listenForModifiedCredentials();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // check for logged in state
        if(isLoggedIn()) {
            ActivityUtils.goToActivity(this, MainActivity.class);
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
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            mGoogleSignInAccount = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(mGoogleSignInAccount);
        } catch(ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        } finally {
            setButtonFinishedProgress(mBtnGoogleSign, GOOGLE_BUTTON_TEXT, mProgressBarGoogle);
            enableButton(mBtnGoogleSign);
        }
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void initGoogleSign() {
        mGoogleSignInOptions = GoogleSignInUtils.getSignInOptionsProfileEmail(getString(R.string.web_client_id));
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions);
    }

    private void setViews() {
        mAuth = FirebaseAuth.getInstance();

        mEditTextEmail = findViewById(R.id.et_email_reg);
        mEditTextPassword = findViewById(R.id.et_pass_reg);
        mEditTextPasswordConfirm = findViewById(R.id.et_pass_confirm_reg);
        mBtnGoogleSign = findViewById(R.id.btn_google_reg);
        mProgressBarGoogle = findViewById(R.id.progress_google_reg);
        mBtnConfirm = findViewById(R.id.btn_confirm_reg);
        mProgressBar = findViewById(R.id.progress_reg);
    }

    private boolean isLoggedIn() {
        if(mAuth.getCurrentUser() != null || mGoogleSignInAccount != null) {
            return true;
        }

        return false;
    }

    private void registerWithEmailAndPass(String email, String pass) {
        final String buttonText = mBtnConfirm.getText().toString();
        setButtonInProgress(mBtnConfirm, mProgressBar);
        ActivityUtils.hideKeyboard(this);
        ActivityUtils.clearFocus(this);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            ActivityUtils.goToActivity(RegistrationActivity.this,
                                    MainActivity.class);
                        } else {
                            clearPasswordFields();
                            disableButton(mBtnConfirm);
                        }

                        setButtonFinishedProgress(mBtnConfirm, buttonText, mProgressBar);
                    }
                });
    }

    private boolean credentialsAreGood(String email, String pass, String passConfirm) {
        if(email.length() < 6 || email.contains("@") == false
                || email.indexOf("@") != email.lastIndexOf("@")
                || email.startsWith("@") || email.endsWith("@")) {
            showBadEmailAlertDialog();
            return false;
        } else if(pass.length() < 8) {
            showBadPassAlertDialog();
            return false;
        } else if(pass.equals(passConfirm) == false) {
            showPassNoMatchAlertDialog();
            return false;
        }

        return true;
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider
                .getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            ActivityUtils.goToActivity(RegistrationActivity.this,
                                    MainActivity.class);
                        } else {
                            showBadEmailAlertDialog();
                        }
                    }
                });
    }

    private void proceedToApp(GoogleSignInOptions options, GoogleSignInAccount account) {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        intent.putExtra("GoogleSignInOptions", options);
        intent.putExtra("GoogleSignInAccount", account);

        startActivity(intent);
    }

    private void hideGoogleSection() {
        Button btn = findViewById(R.id.btn_google_reg);
        View separatorLeft = findViewById(R.id.view_separator_L_reg);
        View textOr = findViewById(R.id.text_or_reg);
        View separatorRight = findViewById(R.id.view_separator_R_reg);

        hideButton(btn);
        hideView(separatorLeft);
        hideView(textOr);
        hideView(separatorRight);
    }

    private void unhideGoogleSection() {
        Button btn = findViewById(R.id.btn_google_reg);
        View separatorLeft = findViewById(R.id.view_separator_L_reg);
        View textOr = findViewById(R.id.text_or_reg);
        View separatorRight = findViewById(R.id.view_separator_R_reg);

        unhideButton(btn);
        unhideView(separatorLeft);
        unhideView(textOr);
        unhideView(separatorRight);
    }

    private void disableButton(@NonNull Button button) {
        button.setEnabled(false);
        Drawable background = button.getBackground();
        GradientDrawable shape = (GradientDrawable) background;
        shape.setColor(ContextCompat.getColor(this, R.color.colorButtonDisabled));
    }

    private void enableButton(@NonNull Button button) {
        button.setEnabled(true);
        Drawable background = button.getBackground();
        GradientDrawable shape = (GradientDrawable) background;
        shape.setColor(ContextCompat.getColor(this, R.color.colorButtonEnabled));
    }

    private void hideButton(@NonNull Button button) {
        button.setEnabled(false);
        button.setVisibility(View.GONE);
    }

    private void unhideButton(@NonNull Button button) {
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);
    }

    private void hideView(@NonNull View view) {
        view.setVisibility(View.GONE);
    }

    private void unhideView(@NonNull View view) {
        view.setVisibility(View.VISIBLE);
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

    private void clearPasswordFields() {
        mEditTextPassword.setText("");
        mEditTextPasswordConfirm.setText("");
    }

    private void showBadEmailAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Registration failed");
        builder.setMessage("Please enter a valid email address.");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showBadPassAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Registration failed");
        builder.setMessage("Your password must have at least 8 characters.");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showPassNoMatchAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Registration failed");
        builder.setMessage("The passwords you entered don't match.");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
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

                GoogleSignInUtils.signInWithGoogle(RegistrationActivity.this, mGoogleSignInClient,
                        GOOGLE_SIGN_IN);
            }
        });
    }

    private void listenForKeyboardState() {
        final View activityRootView = findViewById(R.id.layout_reg);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > dpToPx(RegistrationActivity.this, 200)) {
                    hideGoogleSection();
                } else {
                    unhideGoogleSection();
                }
            }
        });
    }

    private void listenForKeyboardDoneBtn() {
        mEditTextPasswordConfirm.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String email = ((EditText) findViewById(R.id.et_email_reg)).getText().toString();
                String pass = ((EditText) findViewById(R.id.et_pass_reg)).getText().toString();
                String passConfirm = ((EditText) findViewById(R.id.et_pass_confirm_reg)).getText().toString();

                if(email.length() > 0 && pass.length() > 0 && passConfirm.length() > 0
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER
                        && mBtnConfirm.isEnabled()) {

                    if(credentialsAreGood(email, pass, passConfirm)) {
                        registerWithEmailAndPass(email, pass);
                    }

                    return true;
                }
                return false;
            }
        });
    }

    private void listenForConfirmBtn() {
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditTextEmail.getText().toString();
                String pass = mEditTextPassword.getText().toString();
                String passConfirm = mEditTextPasswordConfirm.getText().toString();

                if(credentialsAreGood(email, pass, passConfirm) && mBtnConfirm.isEnabled()) {
                    registerWithEmailAndPass(email, pass);
                }
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
                if (mBtnConfirm.isEnabled() == false
                        && mEditTextEmail.getText().toString().length() > 0
                        && mEditTextPassword.getText().toString().length() > 0
                        && mEditTextPasswordConfirm.getText().toString().length() > 0) {
                    enableButton(mBtnConfirm);
                } else if (mBtnConfirm.isEnabled() == true
                        && (mEditTextEmail.getText().toString().length() == 0
                        || mEditTextPassword.getText().toString().length() == 0
                        || mEditTextPasswordConfirm.getText().toString().length() == 0)) {
                    disableButton(mBtnConfirm);
                }
            }
        };

        mEditTextEmail.addTextChangedListener(textWatcher);
        mEditTextPassword.addTextChangedListener(textWatcher);
        mEditTextPasswordConfirm.addTextChangedListener(textWatcher);
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}
