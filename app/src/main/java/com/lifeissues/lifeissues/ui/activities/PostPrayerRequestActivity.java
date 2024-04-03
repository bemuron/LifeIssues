package com.lifeissues.lifeissues.ui.activities;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.lifeissues.lifeissues.BuildConfig;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.ImageUpload;
import com.lifeissues.lifeissues.ui.adapters.UploadImagesAdapter;
import com.lifeissues.lifeissues.ui.viewmodels.TestimonyPrayerViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class PostPrayerRequestActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener, UploadImagesAdapter.UploadImagesAdapterListener {
    private static final String LOG_TAG = PostPrayerRequestActivity.class.getSimpleName();
    private static final int READ_MEDIA_CODE = 58;
    private static final int CAMERA_PERMISSION_CODE = 56;
    private static final int STORAGE_PERMISSION_CODE = 57;
    private static final int SELECT_IMAGE_REQUEST_CODE =25 ;
    public static final int REQUEST_CAMERA_CAPTURE = 4;
    private TestimonyPrayerViewModel mViewModel;
    private ProgressDialog pDialog;
    private Boolean mLocationPermissionGranted = false;
    private RecyclerView recyclerView;
    private TextInputEditText requestDescEditText, requestTitleEditText;
    private Spinner categoriesDropdownView;
    int[] resImg = {R.drawable.ic_camera, R.drawable.ic_menu_gallery};
    String[] title = {"Camera", "Gallery"};
    private ArrayList<File> imageFilesList = new ArrayList<>();
    //private List<ImageUpload> picsToEditList = new ArrayList<>();
    String[] projection = {MediaStore.MediaColumns.DATA};
    private File imageFile, mPhotoFile;
    private Boolean isCamera = false;
    private Uri imageUri;
    private ImageUpload imageModel;
    private UploadImagesAdapter uploadImagesAdapter;
    private SessionManager session;
    private ArrayList<ImageUpload> uploadImageArrayList;
    private Button postPrayerButton;
    private String mCurrentPhotoPath, requestTitle, requestDesc;
    private int prayerId, prayerCategory, positionToDelete, posterId;
    int selectedCategoryId = 0, userId;
    public static PostPrayerRequestActivity postPrayerRequestActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_prayer);
        setupActionBar();
        postPrayerRequestActivity = this;

        //TestimonyPrayerViewModelFactory factory = InjectorUtils.provideBrowseAdsViewModelFactory(this);
        mViewModel = new ViewModelProvider(this).get(TestimonyPrayerViewModel.class);

        if(!AppController.isNetworkAvailable(this)){
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        }

        // session manager
        session = new SessionManager(getApplicationContext());
        userId = session.getUserId();

        Intent intent = getIntent();
        requestTitle = intent.getStringExtra("prayer_title");
        requestDesc = intent.getStringExtra("prayer_desc");
        prayerId = intent.getIntExtra("prayer_id", 0);
        prayerCategory = intent.getIntExtra("prayer_category", 0);
        posterId = intent.getIntExtra("prayer_poster_id", 0);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
        pDialog.setMessage("Posting your Testimony ...");

        //initialise the views
        getAllWidgets();
        setAdapter();
        setCategoriesSpinnerAdapter();
        if (prayerId > 0){
            //populate views for editing
            populateViewsForEdit();
        }
    }

    public PostPrayerRequestActivity getInstance() {
        return postPrayerRequestActivity;
    }

    @Override
    public void onResume(){
        super.onResume();
        //showDialog();
    }

    @Override
    public void onPause(){
        super.onPause();
        //clear the views when the activity is paused (another activity comes on top)
        //clearViews();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    //initialise the view widgets
    private void getAllWidgets(){
        requestDescEditText = findViewById(R.id.edit_text_prayer_desc);
        requestTitleEditText = findViewById(R.id.edit_text_prayer_title);
        categoriesDropdownView = findViewById(R.id.edit_text_prayer_category);
        categoriesDropdownView.setOnItemSelectedListener(this);
        postPrayerButton = findViewById(R.id.post_prayer_button);
        postPrayerButton.setOnClickListener(this);
        // Progress bar
        //progressBar = findViewById(R.id.poster_ads_progress_bar);
        //showBar();
        recyclerView = findViewById(R.id.recyclerview_prayer_images);
        uploadImageArrayList = new ArrayList<>();
    }

    //populate views for user to edit their details
    private void populateViewsForEdit(){
        requestDescEditText.setText(requestDesc);
        requestTitleEditText.setText(requestTitle);
        categoriesDropdownView.setSelection(prayerCategory);

        //get the testimony images;
        mViewModel.getContentPics(prayerId, 2);
        //picsToEditList.clear();
        /*mViewModel.getAdImages(adId).observe(this,adPics -> {
            Log.e(LOG_TAG, "Size of pics list from network "+adPics.size());
            List<ImageUpload> picsToEditList = new ArrayList<>();
            picsToEditList = adPics;
            Log.e(LOG_TAG, "Size of pics list from network "+picsToEditList.size());
            uploadImageArrayList.addAll(picsToEditList);
            uploadImagesAdapter.imageListForEdit(picsToEditList, true);
        });*/
    }

    public void onPrayerRequestImagesGot(Boolean isImagesGot, List<ImageUpload> picsList){
        if (isImagesGot){
            uploadImageArrayList.addAll(picsList);
            uploadImagesAdapter.imageListForEdit(picsList, true);
        }else{
            Toast.makeText(postPrayerRequestActivity, "Failed to get Testimony images. Probably check your internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCategoriesSpinnerAdapter() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> categoriesFilterAdapter = ArrayAdapter.createFromResource(this,
                R.array.issue_categories, android.R.layout.simple_spinner_item);
        categoriesFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesDropdownView.setAdapter(categoriesFilterAdapter);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        uploadImagesAdapter = new UploadImagesAdapter(this, uploadImageArrayList,this,"post_prayer_details");
        recyclerView.setAdapter(uploadImagesAdapter);
        setImagePickerList();
    }

    // Add Camera and Folder in ArrayList
    public void setImagePickerList(){
        for (int i = 0; i < resImg.length; i++) {
            ImageUpload imageModel = new ImageUpload();
            imageModel.setResImg(resImg[i]);
            imageModel.setTitle(title[i]);
            uploadImageArrayList.add(i, imageModel);
        }
        uploadImagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //showBar();
        if ("HandyMan".equals(parent.getItemAtPosition(position))) {
            selectedCategoryId = 1;
        }else if("Document Writer".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 2;
        }else if("Electrician".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 3;
        }else if("Painter".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 4;
        }else if("Laundry Specialist".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 5;
        }else if("Garbage collector".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 6;
        }else if("Car Mechanic".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 7;
        }else if("Cleaner".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 8;
        }else if("IT Technician".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 9;
        }else if("Plumber".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 10;
        }else if("Carpenter".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 11;
        }else if("Relocation".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 12;
        }else if("Other".equals(parent.getItemAtPosition(position))){
            selectedCategoryId = 13;
        }
    }

    public void takePicture(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Continue only if the File was successfully created;
        File photoFile = createImageFile();
        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri imageUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else{
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
            startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);
        }
    }

    public File createImageFile() {
        // Create an image file name
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "PR_" + formattedDate + "_P"+userId+"_";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File mFile = null;

            try {
                //imageFile = createImageFile();
                File storageDir = this.getExternalFilesDir(DIRECTORY_PICTURES);
                mFile = File.createTempFile(imageFileName,".jpg",storageDir);
                //Log.e(LOG_TAG,"Cam image File in try block = "+ mFile);
            }catch (Exception e){
                e.printStackTrace();
            }
            assert mFile != null;
            mPhotoFile = mFile;
            mCurrentPhotoPath = mFile.getAbsolutePath();
            //addImageToUploadList(mCurrentPhotoPath);
            //Log.e(LOG_TAG,"Abs Path = "+ mFile.getAbsolutePath());
            //mCurrentPhotoPath = file.getAbsolutePath();
            //Log.e(LOG_TAG,"Cam image File = "+ mFile);
            return mFile;
        }else {
            File storageDir = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES);
            try {
                imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPhotoFile = imageFile;
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + imageFile.getAbsolutePath();
            //addImageToUploadList(mCurrentPhotoPath);
            //Log.e(LOG_TAG,"Path = "+mCurrentPhotoPath);
            ///Log.e(LOG_TAG,"Cam image File = "+ imageFile);
            return imageFile;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.post_prayer_button){
            postAdDetails();
        }
    }

    public void postAdDetails() {
        String adTitle = requestTitleEditText.getText().toString().trim();
        if (TextUtils.isEmpty(adTitle)) {
            requestTitleEditText.setError("Title is required");
            //return false;
        }

        String adDesc = requestDescEditText.getText().toString().trim();
        if (TextUtils.isEmpty(adDesc)) {
            requestDescEditText.setError("Description is required");
            //return false;
        }

        if (selectedCategoryId == 0) {
            Toast.makeText(this,
                    "Please select a category.", Toast.LENGTH_LONG).show();
            //categoriesDropdownView.setError("Please select a category");
        }

        if (!adTitle.isEmpty() && !adDesc.isEmpty() && selectedCategoryId > 0){
            //if images are more than 6 don't upload
            if (uploadImageArrayList.size() > 6){
                Toast.makeText(this,
                        "Please add only up to 4 images", Toast.LENGTH_LONG).show();
            }else{
                if (uploadImageArrayList.size() > 2 && uploadImageArrayList.size() < 6){
                    //loop through upload image list and get the files
                    for (int i = 0; i < uploadImageArrayList.size(); i++){
                        if (uploadImageArrayList.get(i).getImageFile() != null){
                            //Log.e(LOG_TAG, "image file list in post ad activity "+uploadImageArrayList.get(i).getImageFile());
                            imageFilesList.add(uploadImageArrayList.get(i).getImageFile());
                        }
                    }
                }

                if (prayerId > 0){
                    pDialog.setMessage("Updating your Prayer Request ...");
                    showDialog();
                    //send details to edit
                    mViewModel.editContent(prayerId, adTitle, adDesc, imageFilesList,
                            selectedCategoryId, userId, 2, null, this.getInstance());
                }else{
                    pDialog.setMessage("Posting your Prayer Request ...");
                    showDialog();
                    //send data to parent activity to be posted to the server
                    mViewModel.postNewContent(adTitle, adDesc, imageFilesList,
                            selectedCategoryId, userId, 2, null, this.getInstance());
                }

            }


        }else{
            Toast.makeText(this,
                    "Please provide all necessary details.", Toast.LENGTH_LONG).show();
        }
    }

    /*@Override
    public void onBackPressed(){
        clearViews();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.job_details_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteImageClicked(int position) {
        positionToDelete = position;
        int picId = uploadImageArrayList.get(position).getPic_id();
        if (picId > 0){
            //delete the image stored in the db
            mViewModel.deleteContentPic(picId);
            pDialog.setMessage("Removing ...");
            showDialog();
        }else{
            uploadImageArrayList.remove(position);
            uploadImagesAdapter.notifyItemRemoved(position);
            uploadImagesAdapter.notifyDataSetChanged();
        }
    }

    public void onPrayerRequestPicDeleted(Boolean isPicDeleted, String msg) {
        hideDialog();
        if (isPicDeleted){
            uploadImageArrayList.remove(positionToDelete);
            uploadImagesAdapter.notifyItemRemoved(positionToDelete);
            uploadImagesAdapter.notifyDataSetChanged();
        }
        Toast.makeText(postPrayerRequestActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onImageClicked(int position, String imagePath, String imageName) {
        if (position == 0) {
            if(checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)){
                takePicture();
            }
        } else if (position == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if(checkPermission(Manifest.permission.READ_MEDIA_IMAGES, READ_MEDIA_CODE)){
                    selectAdImage();
                }
            }else{
                if(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)){
                    selectAdImage();
                }
            }
        }else{
            //show full screen image
            Log.e(LOG_TAG, "Image at Position clicked " +position);
            //mImageClickedCallback.onUploadImageClicked(imagePath, imageName);

        }
    }

    //handles user selection of image
    private void selectAdImage(){
        Intent intent = new Intent();
        //intent.putExtra("image_pos", jobImage);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_IMAGE_REQUEST_CODE);
    }

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
                        Log.e(LOG_TAG, "URI when getClipData not null: " + uri);
                        getFileToAdd(uri);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    Log.e(LOG_TAG, "URI when getData not null: " + uri);
                    getFileToAdd(uri);
                }
            } else if (requestCode == REQUEST_CAMERA_CAPTURE) {
                if (mCurrentPhotoPath != null) {
                    addImageToUploadList(mCurrentPhotoPath, mPhotoFile);
                    //saveImageUpload(mPhotoFile);
                    isCamera = true;
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            //the user cancelled the operation
        }
    }

    private void getFileToAdd(Uri uri){
        Cursor returnCursor = this.getContentResolver().query(uri,null,
                null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        createTempFile(name, uri);
        Log.e(LOG_TAG, "Name of file = "+name);
        //return name;
    }

    //creating a temp file
    private File createTempFile(String name,Uri uri){
        String formattedDate = new SimpleDateFormat("yyyyMMdd",Locale.US).format(new Date());
        String imageFileName = "PR_" + formattedDate + "_P"+userId+"_";
        File file = null;
        try {
            File storageDir = this.getExternalFilesDir(DIRECTORY_PICTURES);
            file = File.createTempFile(imageFileName, ".jpg", storageDir);
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
        imageUri = uri;
        addImageToUploadList(String.valueOf(imageUri), file);
        //imageFilesList.add(file);
        //Log.e(LOG_TAG, "Uri added = "+uri);
        //Log.e(LOG_TAG, "Image file added = "+file);
        //return file;
    }

    private void addImageToUploadList(String filePath, File file){
        //imagePos = uploadImageArrayList.size();
        //Log.e(LOG_TAG, "Image list size "+uploadImageArrayList.size());

        imageModel = new ImageUpload();
        imageModel.setImage(filePath);
        imageModel.setImageFile(file);
        imageModel.setSelected(false);
        uploadImageArrayList.add(imageModel);
        //get the actual files for uploading
        //using the uri
        //getFileName(uploadImageArrayList);
        uploadImagesAdapter.refreshImageList(uploadImageArrayList);
    }

    public void onPrayerRequestUpdated(Boolean isRequestPosted, String message) {
        hideDialog();
        if (isRequestPosted) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            this.finish();
        }else {
            //Log.e(LOG_TAG, "Ad not posted");
            Toast.makeText(this, "Something went wrong. \n "+message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onImageLongClicked(int position) {

    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
            return false;
        }
        else {
            //Toast.makeText(getActivity(), "Permission already granted", Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private void showDialog() {
        postPrayerButton.setEnabled(false);
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        postPrayerButton.setEnabled(true);
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
