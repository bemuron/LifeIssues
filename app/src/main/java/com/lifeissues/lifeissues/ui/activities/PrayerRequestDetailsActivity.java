package com.lifeissues.lifeissues.ui.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.ImageUpload;
import com.lifeissues.lifeissues.ui.adapters.UploadImagesAdapter;
import com.lifeissues.lifeissues.ui.fragments.StatisticsDialogFragment;
import com.lifeissues.lifeissues.ui.viewmodels.TestimonyPrayerViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PrayerRequestDetailsActivity extends AppCompatActivity implements View.OnClickListener,
        UploadImagesAdapter.UploadImagesAdapterListener, StatisticsDialogFragment.OnStatsDialogListener {
    private static final String LOG = PrayerRequestDetailsActivity.class.getSimpleName();
    private String category, requestTitle, requestContent,posterName,datePosted,posterPic,requestImageName;
    private ImageView requestImageView, shareRequestIv, likeRequestIv, unLikeRequestIv;
    private TextView requestTitleTv, requestContentTv, requestPosterTv, requestDateTv;
    private TestimonyPrayerViewModel mViewModel;
    private ProgressBar progressBar;
    private ProgressDialog pDialog;
    private int articleId;
    private RecyclerView mRecyclerView;
    private UploadImagesAdapter imagesAdapter;
    private ArrayList<ImageUpload> imageUploads;
    private List<ImageUpload> imagesList;
    private int userId, prayer_request_id,isLiked,likesNumber,posterId,isReported;
    private SessionManager session;
    private AlertDialog dialog;
    public static PrayerRequestDetailsActivity prayerRequestDetailsActivity;
    private String app_link = "https://play.google.com/store/apps/details?id=com.learnateso.learn_ateso";
    private StatisticsDialogFragment dialogFragment;
    private RewardedAd mRewardedAd;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayer_request_details);
        setupActionBar();
        prayerRequestDetailsActivity = this;

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);
//        pDialog.setMessage("Fetching Prayer Request details ...");
//        showDialog();

        mViewModel = new ViewModelProvider(this).get(TestimonyPrayerViewModel.class);

        // session manager
        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            userId = session.getUserId();
            Log.e(LOG,"User id is "+userId);
        }

        getWidgets();
        setUpRequestPicsAdapter();

        setUpBannerAds();

        //get intent from which this activity is called
        Intent intent = getIntent();
        prayer_request_id = intent.getIntExtra("prayer_id",0);
        if (intent.getIntExtra("prayer_id",0) > 0){
            requestTitle = intent.getStringExtra("title");
            requestContent = intent.getStringExtra("description");
            posterName = intent.getStringExtra("poster_name");
            datePosted = intent.getStringExtra("date_posted");
            isLiked = intent.getIntExtra("is_prayed_for",0);
            likesNumber = intent.getIntExtra("prayers_got_number",0);
            posterPic = intent.getStringExtra("poster_pic");
            posterId = intent.getIntExtra("poster_id",0);
            isReported = intent.getIntExtra("is_reported",0);
            displayContent();
        }

        if (isNetworkAvailable(this)){
            //get the testimony images
            //mViewModel.getContentPics(testimonyId, 1);
            mViewModel.getContentPicsDetails(prayer_request_id).observe(this,requestPics -> {
                if (requestPics.size() > 0){
                    imagesList = requestPics;
                    imagesAdapter.refreshImageList(requestPics);
                }

            });
        }else{
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
        }
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

        setUpBannerAds();
    }

    public static PrayerRequestDetailsActivity getActivityInstance(){
        return prayerRequestDetailsActivity;
    }

    private void getWidgets(){
        requestTitleTv = findViewById(R.id.request_title);
        requestImageView = findViewById(R.id.request_details_image);
        requestImageView.setOnClickListener(this);
        requestContentTv = findViewById(R.id.request_content);
        requestPosterTv = findViewById(R.id.request_poster);
        requestDateTv = findViewById(R.id.request_date);
        shareRequestIv = findViewById(R.id.share_request_icon);
        shareRequestIv.setOnClickListener(this);
        likeRequestIv = findViewById(R.id.request_fav_yellow);
        likeRequestIv.setOnClickListener(this);
        unLikeRequestIv = findViewById(R.id.request_fav_black_border);
        unLikeRequestIv.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.request_pics_recyclerview);
        // Progress bar
        progressBar = findViewById(R.id.request_details_progress_bar);
        showBar();
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
    }

    private void setUpRequestPicsAdapter(){
        RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, 4);
        mRecyclerView.setLayoutManager(layoutManager);
        imagesAdapter = new UploadImagesAdapter(this, imageUploads,this,"request_details");
        mRecyclerView.setAdapter(imagesAdapter);
    }

    //display the article details
    private void displayContent(){
        hideBar();
        hideDialog();

        requestTitleTv.setText(requestTitle);
        updateLikeIcon(isLiked);
        //set title of this activity
        setTitle(requestTitle);
        requestContentTv.setText(requestContent);
        //justify article content for versions O and above
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            articleContentTv.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }*/
        requestPosterTv.setText("By: "+posterName);
        formatDate();

        if (imagesList != null && imagesList.size() > 0){
            Glide.with(this).load("https://vottademo.emtechint.com/public/assets/images/content/"+imagesList.get(0).getImage_name())
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter())
                    .into(requestImageView);
        }else{
            requestImageView.setVisibility(View.GONE);
        }

        invalidateOptionsMenu();
        //Log.e(LOG,"Prefs userid = "+userId+" User id from network = "+article.getPosted_by());
    }

    //set the menu options normally
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.content_details_menu, menu);
        if (userId == posterId){
            menu.findItem(R.id.action_delete_ad).setVisible(true);
            menu.findItem(R.id.action_edit_ad).setVisible(true);
            menu.findItem(R.id.action_report).setVisible(false);
            menu.findItem(R.id.action_stats).setVisible(true);
        }else{
            menu.findItem(R.id.action_report).setVisible(true);
        }

        return true;
    }

    //show user dialog to report issue
    private void showReportRequestDialog(){
        if (!session.isLoggedIn()) {
            showLoginNoticeDialog();
        }else {
            MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
            alertDialog.setTitle("Help us keep the content clean");
            alertDialog.setView(R.layout.dialog_report_issue);
            //alertDialog.setMessage("Login or register to be able to see your profile");
            alertDialog.setPositiveButton("Report", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog = alertDialog.create();
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pDialog.setMessage("Reporting ...");
                    showDialog();
                    TextInputEditText inputComments = (dialog).findViewById(R.id.edit_text_report_issue);
                    String comments = inputComments.getText().toString().trim();
                    if (TextUtils.isEmpty(comments)) {
                        inputComments.setError("Please enter your comments");
                        Toast.makeText(PrayerRequestDetailsActivity.this, "Please enter your comments", Toast.LENGTH_SHORT).show();
                    }else{
                        mViewModel.reportContent(userId, prayer_request_id,
                                inputComments.getText().toString().trim(),2);
                    }
                }
            });
        }
    }

    //prompt user to login/register if not yet so that they can be able to like or report a posting
    private void showLoginNoticeDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        alertDialog.setCancelable(true);
        alertDialog.setTitle("Login or Register");
        alertDialog.setMessage("Login or register to be able to report or like this article");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(PrayerRequestDetailsActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.show();
    }

    //receives if the article has been successfully reported or not
    public void prayerReportedResponse(boolean isPrayerReported, String msg){
        hideDialog();
        if (isPrayerReported) {
            //reportIssueResponseNotice("Successful", msg);
            dialog.dismiss();
        }
        Toast.makeText(PrayerRequestDetailsActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    //alert user on success or failure of article reporting
    private void reportIssueResponseNotice(String title, String msg){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
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
        }else if (id == R.id.action_stats){
            showStatsDialog();
        }else if (id == R.id.action_delete_ad){
            if (userId == posterId) {
                deletePrayer();
            }
        }else if (id == R.id.action_report){
            showReportRequestDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void formatDate(){
        String postedOn = null;

        //date format for dates coming from server
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        try{
            Date d = mysqlDateTimeFormat.parse(datePosted);
            d.setTime(d.getTime());
            postedOn = myFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        requestDateTv.setText(postedOn);
    }

    private void deletePrayer(){
        String msg = "Deleting your Prayer Request is not reversible. " +
                "Do you wish to continue?";
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        alertDialog.setTitle("Delete this Prayer Request?");
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pDialog.setMessage("Deleting Prayer Request ...");
                showDialog();
                //delete the testimony
                mViewModel.deleteContent(userId, prayer_request_id, 2);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    //receives if the request has been successfully deleted or not
    public void requestDeletedResponse(boolean isRequestDeleted, String msg){
        //hideDialog();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if (isRequestDeleted) {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.share_request_icon) {
            shareRequest();
        } else if (id == R.id.request_fav_yellow) {
            if (!session.isLoggedIn()) {
                showLoginNoticeDialog();
            } else {
                mViewModel.unLikeContent(userId, prayer_request_id, 1);
                updateLikeIcon(0);
            }
        } else if (id == R.id.request_fav_black_border) {
            if (session.isLoggedIn()) {
                mViewModel.likeContent(userId, prayer_request_id, 2);
                updateLikeIcon(1);
            } else {
                showLoginNoticeDialog();
            }
        } else if (id == R.id.request_details_image) {
            Intent intent = new Intent(this, ImagePreviewActivity.class);
            intent.putExtra("imageURL", "https://vottademo.emtechint.com/public/assets/images/content/" + imagesList.get(0).getImage_name());
            startActivity(intent);
        }
    }

    public void onPrayerRequestLike(Boolean isRequestLiked, String message) {
        if (isRequestLiked){
            updateLikeIcon(1);
        }else {
            updateLikeIcon(0);
        }
    }

    //code for sharing this article with other apps
    public void shareRequest(){
        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        String shareBody = requestTitle+
                "\n Read this Prayer Request in the Life Issues app"+
                "\n "+app_link;
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Check this out");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sharingIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void updateLikeIcon(int value){
        if(value == 1){
            likeRequestIv.setVisibility(View.VISIBLE);
            unLikeRequestIv.setVisibility(View.INVISIBLE);
        } else if (value == 0) {
            unLikeRequestIv.setVisibility(View.VISIBLE);
            likeRequestIv.setVisibility(View.INVISIBLE);
        }
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        /*try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }*/
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        /*try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(LOG_TAG, e.getMessage());
        }*/
    }

    //show dialog to ask user to login or register
    private void showStatsDialog(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("statsDialogFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment =
                StatisticsDialogFragment.newInstance(likesNumber,
                        "The number of prayers for your Prayer Request.");
        dialogFragment.show(getSupportFragmentManager(), "statsDialogFragment");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    //method to check for internet connection
    public static boolean isNetworkAvailable(Context context) {
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void setUpBannerAds(){
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                //.setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)//ad id 2C021A0AD7564E12BD6FC30F72B15E38
                //.setTestDeviceIds(Arrays.asList("2C021A0AD7564E12BD6FC30F72B15E38"))
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        mAdView = findViewById(R.id.requestAdView);
        mAdView.setAdListener(new AdListener() {
            private void showToast(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                Log.e(LOG, "Ad loaded");
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                //showToast(String.format("Ad failed to load with error code %d.", errorCode));
                String error =
                        String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());

                Log.e(LOG, "onAdFailedToLoad() with error: "+error);
            }

            @Override
            public void onAdOpened() {
                //showToast("Ad opened.");
                Log.e(LOG, "Ad opened");
            }

            @Override
            public void onAdClosed() {
                Log.e(LOG, "Ad closed");
            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e(LOG, "is receiving ads "+adRequest.isTestDevice (this));
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onDeleteImageClicked(int position) {

    }

    @Override
    public void onImageClicked(int position, String imagePath, String imageName) {

    }

    @Override
    public void onImageLongClicked(int position) {

    }
}