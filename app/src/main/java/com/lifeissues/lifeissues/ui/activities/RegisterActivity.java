package com.lifeissues.lifeissues.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.network.Result;
import com.lifeissues.lifeissues.data.network.api.APIService;
import com.lifeissues.lifeissues.data.network.api.LocalRetrofitApi;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.ui.viewmodels.LoginRegisterActivityViewModel;
import com.lifeissues.lifeissues.ui.viewmodels.LoginRegistrationViewModelFactory;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private LoginRegisterActivityViewModel loginRegisterActivityViewModel;
    private Button btnRegister;
    private Button btnLinkToLogin;
    private EditText inputFirstName;
    private EditText inputLastName;
    private EditText inputEmail;
    //private RadioGroup radioGender;
    private int RESOLVE_HINT = 2;
    private EditText inputPassword, confirmPassword;
    private ProgressDialog pDialog;
    private SessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //LoginRegistrationViewModelFactory factory = InjectorUtils.provideLoginRegistrationViewModelFactory(this.getApplicationContext());
        loginRegisterActivityViewModel = new ViewModelProvider(this).get(LoginRegisterActivityViewModel.class);

        inputFirstName = findViewById(R.id.firstName);
        inputLastName = findViewById(R.id.lastName);
        inputEmail = findViewById(R.id.edit_text_register_email);
        inputPassword = findViewById(R.id.edit_text_register_password);
        confirmPassword = findViewById(R.id.edit_text_confirm_password);
        btnRegister = findViewById(R.id.btnRegister);
        btnLinkToLogin = findViewById(R.id.btnLinkToLoginScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        //get the info the user has typed in displaying errors where necessary
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //getting the user values
                //int radioButtonId = radioGender.getCheckedRadioButtonId();
                //RadioButton userGender = radioGender.findViewById(radioButtonId);

                String firstName = inputFirstName.getText().toString().trim();

                if (TextUtils.isEmpty(firstName)) {
                    inputFirstName.setError("Please enter your first name");
                }

                String lastName = inputLastName.getText().toString().trim();
                if (TextUtils.isEmpty(lastName)) {
                    inputLastName.setError("Please enter your last name");
                }

                String email = inputEmail.getText().toString().trim();
                if (TextUtils.isEmpty(email)) {
                    inputEmail.setError("Please enter your email");
                }
                String password = inputPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    inputPassword.setError("Please enter your password");
                }

                String passwordConfirm = confirmPassword.getText().toString().trim();
                if (TextUtils.isEmpty(passwordConfirm)) {
                    confirmPassword.setError("Please enter your password");
                }

                if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                    String fullName = firstName + " " + lastName;
                    if(password.length() < 7){
                        Toast.makeText(RegisterActivity.this, "Password is too short", Toast.LENGTH_LONG).show();
                        inputPassword.setError("Password should be minimum of 7 characters");
                    }else{
                        if (comparePasswords(password, passwordConfirm)){
                            registerUser(fullName, email, password);
                        }else{
                            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                            confirmPassword.setError("Passwords do not match");
                        }
                    }

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter all your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

    }//close onCreate()

    //method to compare user entered passwords
    private boolean comparePasswords(String pass, String comPass){
        return pass.equals(comPass);
    }

    /**
     * Method to call viewmodel method to post user reg details to database
     * */
    private void registerUser(final String fullName, final String email,
                              final String password) {

        //disable clicks on the register button during registration process
        btnRegister.setClickable(false);

        pDialog.setMessage("Registering ...");
        showDialog();

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.createUser(fullName, email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                hideDialog();
                try {
                    if (response.body() != null && !response.body().getError()) {
                        hideDialog();
                        Log.d(TAG, response.body().getMessage());

                        int user_id = response.body().getUser().getUser_id();
                        Log.e(TAG, "Registered user id is " + user_id);

                        Toast toast = Toast.makeText(RegisterActivity.this,
                                "Registration successful. Log in now", Toast.LENGTH_LONG);
                        toast.show();

                        //let the user login now
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        //intent.putExtra("user_id", user_id);
                        startActivity(intent);
                        finish();

                    }
                }catch(Exception e){
                    e.printStackTrace();
                    //Log.e(TAG, "Error msg from inside response body: "+e.getMessage());
                    btnRegister.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                hideDialog();
                //print out any error we may get
                //probably server connection
                //Log.e(TAG, t.getMessage());
                Toast.makeText(RegisterActivity.this, "Failed to register. Please try again", Toast.LENGTH_SHORT).show();

                btnRegister.setClickable(true);
            }
        });

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
