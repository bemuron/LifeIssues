package com.lifeissues.lifeissues.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.flutterwave.raveandroid.rave_presentation.RavePayManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.InputValidator;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.ui.viewmodels.MainActivityViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


public class DeveloperSupportActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = DeveloperSupportActivity.class.getSimpleName();
    private MainActivityViewModel mainActivityViewModel;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Button btnBuySubscription;
    private TextInputEditText amountET,etFirstName,etLastName,etEmail;
    private RadioGroup paymentModeRadioGroup;
    private RadioButton payMode;
    private ProgressDialog pDialog;
    private SessionManager session;
    private static DeveloperSupportActivity developerSupportActivity;
    private String payModeSelected;
    private String name, email, firstName, lastName, firstNameValue, lastNameValue, emailValue;
    private int userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_support);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        developerSupportActivity = this;
        setTitle("Continuous Development");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // session manager
        session = new SessionManager(this);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        getWidgets();
        if (session.isLoggedIn()) {
            getUserDetails();
        }else{
            userId = 1;  //guest id
        }

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    public DeveloperSupportActivity getDeveloperSupportActivityInstance(){
        return developerSupportActivity;
    }

    private void getWidgets(){
        paymentModeRadioGroup = findViewById(R.id.pay_mode_radio_group);
        paymentModeRadioGroup.setOnCheckedChangeListener(this);
        amountET = findViewById(R.id.amountET);
        etFirstName = findViewById(R.id.edit_text_dev_sup_first_name);
        etLastName = findViewById(R.id.edit_text_dev_sup_last_name);
        etEmail = findViewById(R.id.edit_text_dev_sup_email);

        btnBuySubscription = findViewById(R.id.btnSendGift);
        // Login button Click Event
        btnBuySubscription.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                int radioButtonId = paymentModeRadioGroup.getCheckedRadioButtonId();
                payMode = paymentModeRadioGroup.findViewById(radioButtonId);

                String amountToGive = amountET.getText().toString().trim();
                if (TextUtils.isEmpty(amountToGive)) {
                    amountET.setError("Please enter the amount to give");
                }

                if (payMode != null) {
                    payModeSelected = (String) payMode.getText();
                    if (TextUtils.isEmpty(payModeSelected)) {
                        payMode.setError("Please select the payment mode");
                    }
                }

                if (etFirstName != null) {
                    firstNameValue = etFirstName.getText().toString().trim();
                    if (TextUtils.isEmpty(firstNameValue)) {
                        etFirstName.setError("Please enter your first name");
                    }
                }

                if (etLastName != null) {
                    lastNameValue = etLastName.getText().toString().trim();
                    if (TextUtils.isEmpty(lastNameValue)) {
                        etLastName.setError("Please enter your last name");
                    }
                }

                if (etEmail != null) {
                    emailValue = etEmail.getText().toString().trim();
                    if (TextUtils.isEmpty(emailValue)) {
                        etEmail.setError("Please enter your last email");
                    }
                }

                if (!emailValue.isEmpty() && !amountToGive.isEmpty()) {
                    if (Integer.valueOf(amountToGive) > 0){
                        validateInput(firstNameValue, lastNameValue, emailValue, amountToGive, payModeSelected);
                    }else{
                        Toast.makeText(getApplicationContext(),
                                "Please enter an amount greater than 0", Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(),
                                    "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });
    }

    private void getUserDetails(){
        userId = session.getUserId();
        HashMap<String, String> user = session.getUserDetails();
        name = user.get("name");
        email = user.get("email");

        firstName = name.split(" ")[0];
        lastName = name.split(" ")[1];

        etFirstName.setText(firstName);
        etLastName.setText(lastName);
        etEmail.setText(email);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void validateInput(String firstName,String lastName, String email,String amountToGive, String paymentMethod) {

        // Reset errors.
        amountET.setError(null);
        payMode.setError(null);
        etFirstName.setError(null);
        etLastName.setError(null);

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();

        if (TextUtils.isEmpty(amountToGive)) {

            Toast toast = Toast.makeText(this, "Please enter the amount to give",Toast.LENGTH_LONG);
            toast.show();

            focusView = amountET;
            cancel = true;
        }

        //check if email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast toast = Toast.makeText(this, "Your email is required",Toast.LENGTH_LONG);

            toast.show();
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isEmailValid(email)) {
            Toast toast = Toast.makeText(this, "Your email address is invalid",Toast.LENGTH_LONG);

            toast.show();
            cancel = true;
        }

        if (TextUtils.isEmpty(paymentMethod)) {
            Toast toast = Toast.makeText(this, "Please select the payment method",Toast.LENGTH_LONG);

            toast.show();
            focusView = payMode;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName)) {
            Toast toast = Toast.makeText(this, "Please enter your first name",Toast.LENGTH_LONG);

            toast.show();
            focusView = etFirstName;
            cancel = true;
        }

        if (TextUtils.isEmpty(lastName)) {
            Toast toast = Toast.makeText(this, "Please enter your last name",Toast.LENGTH_LONG);

            toast.show();
            focusView = etLastName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (isNetworkAvailable(DeveloperSupportActivity.this)){
                //launch the flutterwave payment
                if (paymentMethod.equals("Card Payment")){
                    setUpCardFlutterPayment(firstName,lastName,email,amountToGive);
                }else if (paymentMethod.equals("Mobile Money (UGX)")){
                    setUpMMFlutterPayment(firstName,lastName,email,amountToGive);
                }
            }else{
                Toast.makeText(getApplicationContext(),
                        "Please check your internet connection", Toast.LENGTH_LONG).show();
            }

        }
    }

    //when user selects mobile money
    //test public key - FLWPUBK_TEST-6f1ceea3440c6d4ed4eec7a2dfd946f8-X
    //test encryption key - FLWSECK_TEST0fcb26dfe728

    //live pub key - FLWPUBK-19aab4ff234522e578a684e79cc75acd-X
    //live enc key - 6facc5d88572ba8506457ff3
    private void setUpMMFlutterPayment(String firstName, String lastName, String email,
                                       String amount){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String date = sdf.format(new Date());

        RavePayManager raveManager;
        raveManager = new RaveUiManager(this)
                .acceptUgMobileMoneyPayments(true)
                .withTheme(R.style.FlutterwaveCustomTheme)
                .setAmount(Double.parseDouble(amount))
                .setCountry("UG")
                .setCurrency("UGX")
                .setEmail(email)
                .setfName(firstName)
                .setlName(lastName)
                .setPhoneNumber("00", true)
                .setNarration("Ongoing Development Support")
                .setPublicKey("FLWPUBK-19aab4ff234522e578a684e79cc75acd-X")
                .setEncryptionKey("6facc5d88572ba8506457ff3")
                .setTxRef(date+"_"+userId)
                .onStagingEnv(false)
                //.setMeta("Developer support gift")
                .shouldDisplayFee(true);

        raveManager.initialize();
    }

    //when user selects card payment
    private void setUpCardFlutterPayment(String firstName, String lastName, String email,
                                         String amount){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String date = sdf.format(new Date());

        RavePayManager raveManager;
        raveManager = new RaveUiManager(this)
                .acceptCardPayments(true)
                .allowSaveCardFeature(true, true)
                .withTheme(R.style.FlutterwaveCustomTheme)
                .setAmount(Double.parseDouble(amount))
                .setCountry("US")
                .setCurrency("USD")
                .setEmail(email)
                .setfName(firstName)
                .setlName(lastName)
                .setPhoneNumber("00", false)
                .setNarration("Ongoing Development Support")
                .setPublicKey("FLWPUBK-19aab4ff234522e578a684e79cc75acd-X")
                .setEncryptionKey("6facc5d88572ba8506457ff3")
                .setTxRef(date+"_"+userId)
                .onStagingEnv(false)
                //.setMeta("Developer support gift")
                .shouldDisplayFee(true);

        raveManager.initialize();
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {

            String message = data.getStringExtra("response");

            if (message != null) {
                Log.d("rave response", message);
            }

            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                //Toast.makeText(this, "SUCCESS " + message, Toast.LENGTH_SHORT).show();
                Log.e("trx success code ", ""+RavePayActivity.RESULT_SUCCESS);

                //save the subscription details
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(DeveloperSupportActivity.this, "ERROR " + message, Toast.LENGTH_SHORT).show();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(DeveloperSupportActivity.this, "CANCELLED " + message, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onSubscriptionSaved(Boolean isSubSaved, String message) {
        hideDialog();
        if (isSubSaved) {
            //Log.e(LOG_TAG, "Ad posted successfully ");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            this.finish();
        }else {
            //Log.e(LOG_TAG, "Ad not posted");
            Toast.makeText(this, "Something went wrong. \n "+message, Toast.LENGTH_LONG).show();
        }
    }

    //prompt user to login/register if not yet
    private void showLoginNoticeDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Login or Register");
        alertDialog.setMessage("Login or register to be able to subscribe");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //session.logoutUser();
                Intent i = new Intent(DeveloperSupportActivity.this, LoginActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Add new Flag to start new Activity
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Starting Login Activity
                startActivity(i);
                finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeveloperSupportActivity.this.finish();
            }
        });
        alertDialog.show();
    }

    //method to check for internet connection
    private static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = radioGroup.findViewById(radioButtonId);
        //payModeSelected = (String)radioButton.getText();
        //Log.i(TAG, "radio selected = "+payModeSelected);
        switch(checkedId){
            case R.id.card_payment_radio_button:
                // do operations specific to this selection
                break;
            case R.id.mm_radio_button:
                // do operations specific to this selection
                break;
        }

    }
}
