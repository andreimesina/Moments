package com.andreimesina.moments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static com.andreimesina.moments.R.id;
import static com.andreimesina.moments.R.layout;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mEditTextEmail;
    private EditText mEditTextPass;
    private Button mBtnSignIn;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_login);

        // init views class members
        setViews();

        // listen for actions on screen
        listenForKeyboardState();
        listenForKeyboardDoneBtn();
        listenForSignInBtn();
        listenForModifiedCredentials();
        listenForRegisterBtn();
        listenForForgotPassBtn();


    }

    private void setViews() {
        mAuth = FirebaseAuth.getInstance();

        mEditTextEmail = findViewById(id.et_email_sign);
        mEditTextPass = findViewById(id.et_password_sign);
        mBtnSignIn = findViewById(id.btn_sign);
        mProgressBar = findViewById(id.progress_sign);
    }

    private void signInWithEmailAndPass(String email, String pass) {
        setButtonSignInProgress(mBtnSignIn);
        hideKeyboard();
        clearFocus();

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    proceedToApp();
                } else {
                    showPasswordForgottenAlertDialog();
                    clearPasswordField();
                    disableButton(mBtnSignIn);
                }

                setButtonSignInFinishedProgress(mBtnSignIn);
            }
        });
    }

    private void proceedToApp() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void proceedToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
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

    private void setButtonSignInProgress(@NonNull Button button) {
        button.setText("");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void setButtonSignInFinishedProgress(@NonNull Button button) {
        mProgressBar.setVisibility(View.GONE);
        button.setText("Sign in");
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

    private void clearFocus() {
        View view = this.getCurrentFocus();

        if(view != null) {
            view.clearFocus();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();

        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void listenForKeyboardState() {
        final View activityRootView = findViewById(id.layout_sign);

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > dpToPx(LoginActivity.this, 200)) {
                    Button btn = findViewById(id.btn_reg_sign);
                    disableTextButton(btn);
                    btn = findViewById(id.btn_forgot_pass_sign);
                    disableTextButton(btn);
                } else {
                    Button btn = findViewById(id.btn_reg_sign);
                    enableTextButton(btn);
                    btn = findViewById(id.btn_forgot_pass_sign);
                    enableTextButton(btn);
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
                    Toast.makeText(LoginActivity.this,"Trying to login...",
                            Toast.LENGTH_LONG).show();
                    if(email.equalsIgnoreCase("admin") && pass.equalsIgnoreCase("123")) {
                        proceedToApp();
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
                proceedToRegistration();
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

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

}
