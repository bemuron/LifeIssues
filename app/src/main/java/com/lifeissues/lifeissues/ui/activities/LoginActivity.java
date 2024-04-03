package com.lifeissues.lifeissues.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.lifecycle.ViewModelProvider;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.network.AuthenticateUser;
import com.lifeissues.lifeissues.helpers.InputValidator;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.viewmodels.LoginRegisterActivityViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements AuthenticateUser.SuccessfulLoginCallBack {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    public static LoginActivity loginActivityInstance;
    private Button btnLogin;
    private Button btnLinkToRegister, forgotPassBtn;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginActivityInstance = this;

        //LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider(this).get(LoginRegisterActivityViewModel.class);

        /*AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        appSignatureHelper.getAppSignatures();*/

        inputEmail = (EditText) findViewById(R.id.edit_text_email);
        inputPassword = (EditText) findViewById(R.id.edit_text_password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        forgotPassBtn = findViewById(R.id.forgot_password_btn);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                //first check if we have an internet connection
                if (isNetworkAvailable()) {
                    //validate user input details and
                    //attempt to login
                    attemptLogin();
                }else {
                    // show user that they may not be having an internet connection
                    Toast.makeText(getApplicationContext(),
                            "Something is not right, try checking your internet connection.", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        //link to password reset url
        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPasswordResetLink();
            }
        });

    }//close

    public static LoginActivity getInstance() {
        return loginActivityInstance;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        inputEmail.setError(null);
        inputPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();
        // Check for a valid password, if the user entered one.
        if ( !inputValidator.isPasswordValid(password)) {

            Toast toast = Toast.makeText(this, "This password is too short",Toast.LENGTH_LONG);

            toast.show();

            focusView = inputPassword;
            cancel = true;
        }
        //check if password field is empty
        if (TextUtils.isEmpty(password)) {

            Toast toast = Toast.makeText(this, "Password is required",Toast.LENGTH_LONG);

            toast.show();
            // mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = inputPassword;
            cancel = true;
        }

        //check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast toast = Toast.makeText(this, "Email is required",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isEmailValid(email)) {
            Toast toast = Toast.makeText(this, "This email address is invalid",Toast.LENGTH_LONG);

            toast.show();
            focusView = inputEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            pDialog.setMessage("Logging in ...");
            showDialog();

            //login the user
            //function to verify login details in mysql db
            //Calls viewmodel method
            loginRegisterActivityViewModel.loginUser(email,password);
            //checkLogin(email,password);

            /*pDialog.setMessage("Logging in ...");
            showDialog();*/

        }
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //method to check for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //callback from the login user service
    @Override
    public void onLoginSuccessful(Boolean isLoginSuccessful, User user, String responseMessage) {
        hideDialog();
        if (isLoginSuccessful){
            Log.d(TAG, "Successful login");
            //insert user to the local db
            loginRegisterActivityViewModel.insert(user);

            // user successfully logged in
            // Create login session
            session.createLoginSession(user.getUser_id(), user.getName(),
                    user.getEmail(),user.getProfile_pic(),
                    user.getCreated_at(), user.getAccess_token());

            //Log.e(TAG, "Token = "+user.getAccess_token());

            Toast.makeText(LoginActivity.this, "Welcome "+user.getName(), Toast.LENGTH_LONG).show();

            /**
             * Always check for google play services availability before
             * proceeding further with FCM
             * */

//            if (checkPlayServices()) {
//                registerFCM(user.getUser_id());
//            }
            hideDialog();

            //start home activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }else{
            this.hideDialog();
            Log.d(TAG, "login not successful");
            //display any error msg that may be received
            //Toast.makeText(LoginActivity.this, "Invalid username or password",
              //      Toast.LENGTH_LONG).show();
            Toast.makeText(LoginActivity.this, responseMessage,
                    Toast.LENGTH_LONG).show();
            btnLogin.setClickable(true);
        }

    }

    //load the custom chrome tabs which opens the password reset ui on the websit
    //so a user can reset their password
    private void loadPasswordResetLink(){
        //String url = "https://fixappug.com/password/reset";
        String url = "https://vottademo.emtechint.com/forgot-password";

        // initializing object for custom chrome tabs.
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();


        int colorInt = Color.parseColor("#212121"); //black
        CustomTabColorSchemeParams defaultColors = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(colorInt)
                .build();
        builder.setDefaultColorSchemeParams(defaultColors);

        customTabsIntent.launchUrl(this, Uri.parse(url));
    }
}
