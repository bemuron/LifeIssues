package com.lifeissues.lifeissues.ui.activities;

import static android.os.Environment.DIRECTORY_PICTURES;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.lifeissues.lifeissues.ui.fragments.EditProfileFragment.editProfileFragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.network.AuthenticateUser;
import com.lifeissues.lifeissues.helpers.CircleTransform;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.fragments.EditProfileFragment;
import com.lifeissues.lifeissues.ui.viewmodels.UserProfileActivityViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener,
        EditProfileFragment.EditProfileDialogListener, AuthenticateUser.ProfileUpdatedCallBack {
    private static final String TAG = MyProfileActivity.class.getSimpleName();
    private static final int SELECT_IMAGE_REQUEST_CODE =25;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 55;
    private static final int CAMERA_PERMISSION_CODE = 56;
    private static final int STORAGE_PERMISSION_CODE = 57;

    private static final int READ_MEDIA_CODE = 58;
    private ImageView userProfilePicIv;
    private TextView userNameTv, userEmailTv;
    private User mUserDetails, user;
    private String name, email, profile_pic, created_on, imageFilePath = "non", profilePicEdited;
    private int userId;
    public static MyProfileActivity myProfileActivity;
    private UserProfileActivityViewModel userProfileActivityViewModel;
    private ProgressBar pBar;
    private SessionManager session;
    String[] projection = {MediaStore.MediaColumns.DATA};
    private File imageFile, mPhotoFile;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setupActionBar();
        myProfileActivity = this;

        // Progress bar
        //pBar = findViewById(R.id.forFixer_progress_bar);
        //showBar();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage("Fetching details ...");
        showDialog();

        //UserProfileActivityViewModelFactory factory = InjectorUtils.provideUserProfileViewModelFactory(this.getApplicationContext());
        userProfileActivityViewModel = new ViewModelProvider(this).get(UserProfileActivityViewModel.class);

        // session manager
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            showLoginNoticeDialog();
        }else{
            userId = session.getUserId();

            //Log.e(TAG,"Inside user clause");
            //it is the session user that is here
            HashMap<String, String> user = session.getUserDetails();
            name = user.get("name");
            email = user.get("email");
            profile_pic = user.get("profile_pic");
            created_on = user.get("created_on");
        }

        //initialise the views
        setUpWidgets();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.e(TAG,"vis id inside on resume is "+visitor_id+" user id is "+userId);
    }

    public static MyProfileActivity getInstance(){
        return myProfileActivity;
    }

    //initialise the view widgets
    private void setUpWidgets(){
        userProfilePicIv = findViewById(R.id.profile_pic);
        userNameTv = findViewById(R.id.profile_user_name);
        userEmailTv = findViewById(R.id.profile_user_email);
        userProfilePicIv.setOnClickListener(this);
        populateViews();
    }

    //method to handle population of the views with the content
    private void populateViews(){
        hideDialog();
        userNameTv.setText(name);
        userEmailTv.setText(email);

        //set user profile pic
        try {
            if (profile_pic != null) {
                userProfilePicIv.setImageDrawable(null);
                Glide.with(this)
                        .load("https://vottademo.emtechint.com/public/assets/images/profile_pics/" + profile_pic)
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .apply(new RequestOptions().fitCenter().timeout(6000)
                                .transform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(userProfilePicIv);
                userProfilePicIv.setColorFilter(null);
            }else{
                userProfilePicIv.setImageResource(R.drawable.img_profile_layer);
            }
        }catch (Exception e){
            Log.e(TAG, "Could not load image");
            e.printStackTrace();
        }
    }

    //handles user selection of profile image from gallery
    private void selectProfileImage(){
        Intent intent = new Intent();
        //intent.putExtra("image_pos", jobImage);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

    //this code instantiates the edit profile dialog fragment and shows it
    public void showEditProfileDialog(int userId, String name, String email){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("EditProfileFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment dialogFragment = EditProfileFragment.newInstance(userId,name,email);
        dialogFragment.show(getSupportFragmentManager(), "EditProfileFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        else if (id == R.id.action_edit_profile){
            showEditProfileDialog(userId,name,email);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.profile_pic){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(checkPermission(Manifest.permission.READ_MEDIA_IMAGES, READ_MEDIA_CODE)){
                    selectProfileImage();
                }
            }else{
                if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                    selectProfileImage();
                }
            }
        }
    }

    //prompt user to login/register if not yet
    private void showLoginNoticeDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Login or Register");
        alertDialog.setMessage("Login or register to be able to see your profile");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //session.logoutUser();
                Intent i = new Intent(MyProfileActivity.this, LoginActivity.class);
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
                myProfileActivity.finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public void editedProfileDetails(String username, String email) {
        if (isNetworkAvailable(getApplicationContext())) {
            userProfileActivityViewModel.updateUserProfile(userId, username,
                    email, myProfileActivity);
        }else{
            Toast.makeText(MyProfileActivity.this, "Please check your internet connection",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //save profile pic that the user has selected
    private void saveProfilePic(File profilePic){
        if (isNetworkAvailable(getApplicationContext())) {
            userProfileActivityViewModel.saveProfilePic(userId, profilePic, myProfileActivity);
        }else{
            Toast.makeText(MyProfileActivity.this, "Please check your internet connection",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //response received whether details have been updated or not
    @Override
    public void onProfilePosted(Boolean isProfileSaved, String message) {
        editProfileFragment.saveProfileResponse(isProfileSaved, message);
    }

    //response on whether the profile pic has been saved or not
    @Override
    public void onProfilePicSaved(Boolean isPicSaved, String message) {
        if(isPicSaved){
            session.saveProfilePicName(profilePicEdited);
            //set the new profile pic
            try {
                if (profilePicEdited != null) {
                    userProfilePicIv.setImageDrawable(null);
                    Glide.with(this)
                            .load("https://vottademo.emtechint.com/public/assets/images/profile_pics/" + profilePicEdited)
                            .thumbnail(0.5f)
                            .transition(withCrossFade())
                            .apply(new RequestOptions().fitCenter()
                                    .transform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(userProfilePicIv);
                }
            }catch (Exception e){
                Log.e(TAG, "Could not load image");
                e.printStackTrace();
            }
        }
        Log.e(TAG, "*****"+message);
        Toast.makeText(MyProfileActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }

    //get the pic the user has picked
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check for our request code
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE_REQUEST_CODE) {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.e(TAG, "URI when getClipData not null: " + uri);
                        getImageFilePath(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    Log.e(TAG, "URI when getData not null: " + uri);
                    getImageFilePath(uri);
                }
            }
        }
    }

    // Get image file path
    public void getImageFilePath(Uri uri) {
        Cursor cursor = this.getContentResolver()
                .query(uri, projection, null,    null, null);
        if (cursor != null) {
            while  (cursor.moveToNext()) {
                String absolutePathOfImage = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                if (absolutePathOfImage != null) {
                    //imageFilePath = absolutePathOfImage;
                    checkImage(absolutePathOfImage, uri);
                } else {
                    //imageFilePath = String.valueOf(uri);
                    checkImage(String.valueOf(uri), uri);
                }
            }
        }
    }

    //check if user has already added this image
    private void checkImage(String filePath, Uri uri) {
        //if (imageFilePath)
        if (imageFilePath.equalsIgnoreCase(filePath)) {
            Toast.makeText(this, "This is the current image", Toast.LENGTH_LONG).show();
        }else{
            userProfilePicIv.setImageDrawable(null);
            //show the selected image
            try {
                Glide.with(this).load(uri)
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .apply(new RequestOptions().fitCenter()
                                .transform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(userProfilePicIv);
            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
            //profileImagePreview.setImageURI(uri);
            getFileName(uri);
        }
        imageFilePath = filePath;
    }

    //getting the file name
    private void getFileName(Uri uri){
        Cursor returnCursor = this.getContentResolver().query(uri,null,
                null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        createNewFileName(name, uri);
        Log.e(TAG, "Name of file = "+name);
        //return name;
    }

    //creating a temp file with new name
    private File createNewFileName(String name, Uri uri){
        String formattedDate = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        String imageFileName = "FAIMG_" + formattedDate + "_U"+userId+"_";
        File file = null;
        try {
            File storageDir = this.getExternalFilesDir(DIRECTORY_PICTURES);
            file = File.createTempFile(imageFileName, ".jpg", storageDir);
            profilePicEdited = file.getName();
            //Log.e(TAG, "New file name: "+file.getName());
        }catch (IOException e){
            e.printStackTrace();
        }
        saveContentToFile(uri, file);
        return file;
    }

    //save the temp file using Okio
    private void saveContentToFile(Uri uri, File file){
        try {
            InputStream stream = this.getContentResolver().openInputStream(uri);
            BufferedSource source = Okio.buffer(Okio.source(stream));
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(source);
            sink.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        imageFile = file;
        //save the new pic file
        saveProfilePic(imageFile);
        Log.e(TAG, "Uri added = "+uri);
        Log.e(TAG, "Image file added = "+file);
        //return file;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    /*private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }*/

    // Function to check and request permission.
    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MyProfileActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MyProfileActivity.this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            //Toast.makeText(MyProfileActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    //check if we have permission to write external storage
    private boolean checkAndRequestPermissions() {

        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= Build.VERSION_CODES.M) {
            int permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                int permissionMedia = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_MEDIA_IMAGES);

                if (permissionMedia != PackageManager.PERMISSION_GRANTED){
                    listPermissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
                }
            }
            //if the permissions list is not empty, then request for the permission
            if (!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                        (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MyProfileActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfileActivity.this, Manifest.permission.CAMERA)){
                    new AlertDialog.Builder(MyProfileActivity.this)
                            .setTitle("Permission Request")
                            .setMessage("FixApp requires this permission to access photos taken by your camera.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MyProfileActivity.this,
                                            new String[]{Manifest.permission.CAMERA},
                                            CAMERA_PERMISSION_CODE);
                                }
                            })
                            .show();
                }
                Toast.makeText(MyProfileActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }else if (requestCode == READ_MEDIA_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MyProfileActivity.this, "Media Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(MyProfileActivity.this, Manifest.permission.READ_MEDIA_IMAGES)){
                    new AlertDialog.Builder(MyProfileActivity.this)
                            .setTitle("Add Task Images")
                            .setMessage("Easily add task related images from your gallery by granting this permission.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MyProfileActivity.this,
                                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                            READ_MEDIA_CODE);
                                }
                            })
                            .show();
                }
                Toast.makeText(MyProfileActivity.this, "Media Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MyProfileActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    new AlertDialog.Builder(MyProfileActivity.this)
                            .setTitle("Permission Request")
                            .setMessage("FixApp requires this permission to access images you select from your gallery")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MyProfileActivity.this,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            STORAGE_PERMISSION_CODE);
                                }
                            })
                            .show();
                }
                Toast.makeText(MyProfileActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
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
