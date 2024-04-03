package com.lifeissues.lifeissues.ui.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.InputValidator;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.viewmodels.UserProfileActivityViewModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends DialogFragment implements View.OnClickListener{
    private static final String TAG = EditProfileFragment.class.getSimpleName();
    private static final String USER_ID = "userId";
    private static final String FULL_NAME = "fullname";
    private static final String EMAIL = "email";
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 50;
    private UserProfileActivityViewModel userProfileActivityViewModel;
    private ProgressBar progressBar;
    private String imageFilePath = "non";
    private Button saveProfileButton;
    private MaterialButton addSkillButton, addWorkButton, addLangButton, addEduButton;
    private int mPosition = RecyclerView.NO_POSITION;
    private TextInputEditText firstName, lastName, email, userLocation;
    private Toolbar toolbar;
    private SessionManager session;
    private EditProfileDialogListener dialogListener;
    private EditText inputFirstName, inputLastName, inputEmail;
    private Calendar myCalendar = Calendar.getInstance();
    private DatePickerDialog mDatePickerDialog;
    private int mUserId;
    private String mFullName, mEmail, fullNameEdited, emailEdited, genderEdited;
    public static EditProfileFragment editProfileFragment;
    private ProgressDialog pDialog;
    private Boolean isCamera = false, isNINCamera = false;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userId Parameter 1.
     * @param fullname Parameter 2.
     * @return A new instance of fragment EditProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditProfileFragment newInstance(int userId, String fullname,
                                                  String email) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putInt(USER_ID, userId);
        args.putString(FULL_NAME, fullname);
        args.putString(EMAIL, email);
        fragment.setArguments(args);
        editProfileFragment = fragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
        // session manager
        session = new SessionManager(getActivity());

        //UserProfileActivityViewModelFactory factory = InjectorUtils.provideUserProfileViewModelFactory(getActivity().getApplicationContext());
        userProfileActivityViewModel = new ViewModelProvider(this).get(UserProfileActivityViewModel.class);

        if (getArguments() != null) {
            mUserId = getArguments().getInt(USER_ID);
            mFullName = getArguments().getString(FULL_NAME);
            mEmail = getArguments().getString(EMAIL);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        getAllWidgets(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        toolbar.setTitle("Edit Profile");

        /*toolbar.inflateMenu(R.menu.save_article_dialog);
        toolbar.setOnMenuItemClickListener(item -> {
            getHeritageDetails();
            return true;
        });*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EditProfileDialogListener) {
            dialogListener = (EditProfileDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EditProfileDialogListener");
        }
    }

    private void getAllWidgets(View view){
        // Progress dialog
        pDialog = new ProgressDialog(getActivity());
        pDialog.setCancelable(false);

        toolbar = view.findViewById(R.id.edit_profile_toolbar);
        firstName = view.findViewById(R.id.edit_profile_firstname_et);
        lastName = view.findViewById(R.id.edit_profile_lastname_et);
        email = view.findViewById(R.id.edit_profile_email_et);
        saveProfileButton = view.findViewById(R.id.profile_save_details);
        saveProfileButton.setOnClickListener(this);

        // Progress bar
        progressBar = view.findViewById(R.id.edit_profile_progress_bar);

        email.setText(mEmail);
        String[]fNameArray = mFullName.trim().split("\\s");
        firstName.setText(fNameArray[0]);
        lastName.setText(fNameArray[1]);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.profile_save_details) {
            getProfileDetails();
        }
    }

    //when save profile button is clicked, get the details from the fields
    private void getProfileDetails(){
        // Reset errors.
        firstName.setError(null);
        lastName.setError(null);
        email.setError(null);

        boolean cancel = false;
        View focusView = null;

        InputValidator inputValidator = new InputValidator();

        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        emailEdited = email.getText().toString().trim();

        //check if first name field is empty
        if (TextUtils.isEmpty(fName)) {

            Toast toast = Toast.makeText(getActivity(), "First name is required",Toast.LENGTH_LONG);
            toast.show();

            focusView = firstName;
            cancel = true;
        }

        //check if last name field is empty
        if (TextUtils.isEmpty(lName)) {

            Toast toast = Toast.makeText(getActivity(), "Last name is required",Toast.LENGTH_LONG);
            toast.show();

            focusView = lastName;
            cancel = true;
        }

        //check if email field is empty
        if (TextUtils.isEmpty(emailEdited)) {
            Toast toast = Toast.makeText(getActivity(), "Email is required",Toast.LENGTH_LONG);

            toast.show();
            focusView = email;
            cancel = true;
            // Check for a valid email address.
        } else if (!inputValidator.isEmailValid(emailEdited)) {
            Toast toast = Toast.makeText(getActivity(), "This email address is invalid",Toast.LENGTH_LONG);

            toast.show();
            focusView = email;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            if (isNetworkAvailable(getActivity())) {
                showBar();
                fullNameEdited = fName + " " + lName;
                dialogListener.editedProfileDetails(fullNameEdited, emailEdited);

            } else {
                Toast.makeText(getActivity(), "Please check your internet connection",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    //interface implemented in User Profile Activity to set edited details
    public interface EditProfileDialogListener {
        void editedProfileDetails(String username, String email);
    }

    //called in UserProfileActivity to return status of profile save
    public void saveProfileResponse(Boolean isProfileSaved, String message){
        hideBar();
        if (isProfileSaved) {
            //update prefs and sqlite db with details
            User mUser = new User();
            mUser.setUser_id(mUserId);
            mUser.setName(fullNameEdited);
            mUser.setEmail(emailEdited);

            //update user login sessions
            session.updateLoginSession(mUser.getUser_id(), mUser.getName(),
                    mUser.getEmail());

            //loginRegisterActivityViewModel.updateUser(mUser);
            dismiss();
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(getActivity(), new String[] { permission }, requestCode);
            return false;
        }
        else {
            //Toast.makeText(getActivity(), "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    //check if we have permission to camera and write external storage
    private boolean checkAndRequestPermissions() {

        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            int permisionWriteExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permisionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissionCamera != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            //if the permissions list is not empty, then request for the permission
            if (!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray
                        (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
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
}