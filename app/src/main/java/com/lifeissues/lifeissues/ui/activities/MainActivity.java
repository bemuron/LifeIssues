package com.lifeissues.lifeissues.ui.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.lifeissues.lifeissues.ui.fragments.HomeFragment;
import com.lifeissues.lifeissues.ui.fragments.PrayerFragment;
import com.lifeissues.lifeissues.ui.fragments.SettingsFragment;
import com.lifeissues.lifeissues.ui.fragments.TestimonyFragment;
import com.lifeissues.lifeissues.ui.viewmodels.MainActivityViewModel;
import com.lifeissues.lifeissues.ui.viewmodels.UserProfileActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnViewAllTestimoniesClickListener ,
        HomeFragment.OnIssueListClickListener, HomeFragment.OnViewAllPrayerClickListener,
        HomeFragment.OnPrayerListClickListener, HomeFragment.OnTestimonyListClickListener,
        TestimonyFragment.TestimonyClickedListener, PrayerFragment.PrayerRequestClickListener,
        PrayerFragment.OnCreateNewRequestClick, TestimonyFragment.OnCreateNewTestimonyClick,
        HomeFragment.OnViewAllIssuesClickListener, SettingsFragment.OnSettingsItemClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    public static MainActivity mainActivity;
    private MainActivityViewModel mainActivityViewModel;
    private UserProfileActivityViewModel userProfileActivityViewModel;
    private AdView mAdView;
    private Cursor cursor,c;
    private ProgressDialog pDialog;
    private SessionManager session;
    private String query;
    private SharedPreferences prefs;
    private int post,counter = 0, userId, max,random_articleID,min=1, currentFragment;
    //private ListView mListView;
    private SearchView searchView;
    private static boolean isAppThemeChange;
    private BottomNavigationView bottomNavigationView;
    private static final String TAG_FRAGMENT_HOME = "tag_frag_home";

    private static final String TAG_FRAGMENT_ISSUES = "tag_frag_issues";

    private static final String TAG_FRAGMENT_TESTIMONY = "tag_frag_testimonies";

    private static final String TAG_FRAGMENT_PRAYER = "tag_frag_prayer";
    private static final String TAG_FRAGMENT_ACCOUNT = "tag_frag_account";
    private List<Fragment> fragments = new ArrayList<>(5);
    private SearchManager searchManager;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private SimpleCursorAdapter words;
    private String privacy_policy_link = "https://www.emtechint.com/life-issues-app", issueID;
    private String app_link = "https://play.google.com/store/apps/details?id=com.lifeissues.lifeissues&hl=en&gl=US";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mainActivity = this;
        createNotificationChannel();
        checkAndRequestPermissions();

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                //.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        //request for the ad
        adRequest = new AdRequest.Builder().build();

        mAdView = findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() {
            private void showToast(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                Log.e(TAG, "Ad loaded");
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                //showToast(String.format("Ad failed to load with error code %d.", errorCode));
                String error =
                        String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());

                Log.e(TAG, "onAdFailedToLoad() with error: "+error);
            }

            @Override
            public void onAdOpened() {
                //showToast("Ad opened.");
                Log.e(TAG, "Ad opened");
            }

            @Override
            public void onAdClosed() {
                Log.e(TAG, "Ad closed");
            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e(TAG, "is receiving ads "+adRequest.isTestDevice (this));
        mAdView.loadAd(adRequest);

        setUpInterstitial();

        // session manager
        session = new SessionManager(getApplicationContext());

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //check for login status if user
        //session.checkLogin();

        if (session.isLoggedIn()) {
            userId = session.getUserId();
        }

        /*TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Today's Verse"));
        tabLayout.addTab(tabLayout.newTab().setText("Issues"));*/
        //tabLayout.setupWithViewPager(mViewPager);
        /*
        Calling this during onCreate() ensures that your application is properly
        initialized with default settings, which your application might need to read
        in order to determine some behaviors
        * When false, the system sets the default values only
        * if this method has never been called in the past (or the KEY_HAS_SET_DEFAULT_VALUES
        * in the default value shared preferences file is false).
        * As long as you set the third argument to false, you can safely call this method
        * every time your activity starts without overriding the user's saved preferences
        * by resetting them to the defaults. However, if you set it to true, you will override
        * any previous values with the defaults
        * */
        //PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //check the theme the user is using
        String defaultThemeKey = getString(R.string.pref_app_theme);
        String defaultTheme = prefs.getString(defaultThemeKey, null);

        if (defaultTheme != null) {
            if (defaultTheme.equals("System")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }else if(defaultTheme.equals("Light")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }else if(defaultTheme.equals("Dark")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            //Log.e(TAG, "defaultTheme: "+defaultTheme);
            //Toast.makeText(getApplicationContext(), "defaultTheme: "+defaultTheme, Toast.LENGTH_SHORT).show();

            //this.recreate();
        }

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        userProfileActivityViewModel = new ViewModelProvider(this).get(UserProfileActivityViewModel.class);

        setUpBottomNavigation();

        buildFragmentList();

        if (isAppThemeChange){
            currentFragment = 3;
            switchFragment(3, TAG_FRAGMENT_ACCOUNT);
            isAppThemeChange = false;
        }else{
            //set the 0th Fragment to be displayed by default
            currentFragment = 0;
            switchFragment(0, TAG_FRAGMENT_HOME);
        }

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.contentHomeFrame) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel( "10002", "DAILY_VERSE_NOTIFICATION", importance);
            channel.setDescription("Daily Verse Notification");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean checkAndRequestPermissions(){

        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS}, 22);

                return false;
            }else{
                return true;
            }

        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 22) {
            if (grantResults.length > 0)
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"Permission granted");
                    // permission granted, perform required code

                } else {
                    Log.e(TAG,"Permission not granted");
                    Toast.makeText(getApplicationContext(), "Please accept notifications to get a Daily Fresh Verse",
                            Toast.LENGTH_LONG).show();
                }
        }
    }

    public void onPrayerRequestClicked(int position, int issueID, String issueName){

    }

    //save the current fragment position of the bottom nav
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentFragPos", currentFragment);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentFragment = savedInstanceState.getInt("currentFragPos");
        switch (currentFragment){
            case 0:
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                //switchFragment(0,TAG_FRAGMENT_HOME);
                break;
            case 1:
                bottomNavigationView.setSelectedItemId(R.id.navigation_testimonies);
                //switchFragment(2,TAG_FRAGMENT_MY_TASKS);
                break;
            case 2:
                bottomNavigationView.setSelectedItemId(R.id.navigation_prayer);
                //switchFragment(3,TAG_FRAGMENT_MESSAGES);
                break;
            case 3:
                bottomNavigationView.setSelectedItemId(R.id.navigation_account);
                //switchFragment(4,TAG_FRAGMENT_ACCOUNT);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session.isLoggedIn()) {
            userId = session.getUserId();
        }

        String defaultThemeKey = getString(R.string.pref_app_theme);
        String defaultTheme = prefs.getString(defaultThemeKey, null);

        if (defaultTheme != null) {
            if (defaultTheme.equals("System")) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }else if(defaultTheme.equals("Light")){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            //this.recreate();
        }
    }

    @Override
    public void issueListClick(int issueId, String issueName, int is_favorite) {
        Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
        intent.putExtra("issue_ID", issueId);
        intent.putExtra("favouriteIssues", is_favorite);
        intent.putExtra("issue_name", issueName.toLowerCase(Locale.US));
        startActivity(intent);
    }

    @Override
    public void onTestimonyListItemClick(int position, int testimonyID, String title,
                                         String description,int isLiked,
                                         String datePosted,int isReported,
                                         int likesNumber, String posterName,
                                         String profilePic, int posterId) {
        Intent intent = new Intent(MainActivity.this, TestimonyDetailsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("testimony_id", testimonyID);
        intent.putExtra("is_liked", isLiked);
        intent.putExtra("date_posted", datePosted);
        intent.putExtra("is_reported", isReported);
        intent.putExtra("likes_number", likesNumber);
        intent.putExtra("poster_name", posterName);
        intent.putExtra("poster_pic", profilePic);
        intent.putExtra("poster_id", posterId);
        startActivity(intent);

    }

    @Override
    public void onPrayerListItemClick(int position, int prayerID, String title, String description,
                                      int isPrayedFor, String datePosted, int isReported,
                                      int prayersReceivedNumber, String posterName,
                                      String profilePic, int posterId) {
        Intent intent = new Intent(MainActivity.this, PrayerRequestDetailsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("prayer_id", prayerID);
        intent.putExtra("is_prayed_for", isPrayedFor);
        intent.putExtra("date_posted", datePosted);
        intent.putExtra("is_reported", isReported);
        intent.putExtra("prayers_got_number", prayersReceivedNumber);
        intent.putExtra("poster_name", posterName);
        intent.putExtra("poster_pic", profilePic);
        intent.putExtra("poster_id", posterId);
        startActivity(intent);

    }

    @Override
    public void allIssuesListClick() {
        Intent intent = new Intent(MainActivity.getInstance(), IssuesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSettingsItemClick(String clickedItem) {
        Intent intent;
        if (clickedItem.equals(getString(R.string.key_view_profile))){
            String noticeMsg = "Login or Register to view your profile. ";
            if (!session.isLoggedIn()) {
                showLoginNoticeDialog(noticeMsg);
            }else{
                intent = new Intent(this, MyProfileActivity.class);//MyProfileActivity
                intent.putExtra("userId", userId);
                startActivity(intent);
            }

        }else if (clickedItem.equals(getString(R.string.key_dev_support))){
            intent = new Intent(this, DeveloperSupportActivity.class);
            startActivity(intent);
        }else if (clickedItem.equals(getString(R.string.key_share_lifeissues))){

            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            String shareBody = "\nTopical Bible Verses, Prayer Requests and Testimonies." +
                    "\nDownload the Life Issues app now: " +
                    "\n "+app_link;
            //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            sharingIntent.setType("text/plain");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        }else if (clickedItem.equals(getString(R.string.key_privacy_policy))){
            openPrivacyPolicyLink();

        }else if (clickedItem.equals(getString(R.string.key_logout))){
            if (session.isLoggedIn()) {
                if (AppController.isNetworkAvailable(this)){
                    confirmLogoutDialog();
                }else{
                    Toast.makeText(this, "Please check your internet connection",
                            Toast.LENGTH_SHORT).show();
                }
            }

        }else if (clickedItem.equals(getString(R.string.pref_app_theme))){
            isAppThemeChange = true;
        }else if (clickedItem.equals(getString(R.string.key_about_life_issues))){
            AboutLifeIssues.Show(this);
            //intent = new Intent(this, AboutFixAppActivity.class);
            //startActivity(intent);

        }else if (clickedItem.equals(getString(R.string.notifications_new_message))){
            //Log.e(TAG, "Notifications clicked");
            if(!checkAndRequestPermissions()){
                Toast.makeText(getApplicationContext(), "Please accept notifications to get a Daily Fresh Verse",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onTestimonyClicked(int position, int testimonyID, String title,
                                   String description,int isLiked,
                                   String datePosted,int isReported,
                                   int likesNumber, String posterName,
                                   String profilePic, int posterId) {
        Intent intent = new Intent(MainActivity.this, TestimonyDetailsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("testimony_id", testimonyID);
        intent.putExtra("is_liked", isLiked);
        intent.putExtra("date_posted", datePosted);
        intent.putExtra("is_reported", isReported);
        intent.putExtra("likes_number", likesNumber);
        intent.putExtra("poster_name", posterName);
        intent.putExtra("poster_pic", profilePic);
        intent.putExtra("poster_id", posterId);
        startActivity(intent);

    }

    @Override
    public void onCreateNewPrayerRequest() {
        String noticeMsg = "Login or Register to post your Prayer Request. ";
        if (!session.isLoggedIn()) {
            showLoginNoticeDialog(noticeMsg);
        }else {
            Intent intent = new Intent(MainActivity.this, PostPrayerRequestActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateNewTestimony() {
        String noticeMsg = "Login or Register to post your Testimony. ";
        if (!session.isLoggedIn()) {
            showLoginNoticeDialog(noticeMsg);
        }else {
            Intent intent = new Intent(MainActivity.this, PostTestimonyActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onPrayerRequestClicked(int position, int prayerID, String title, String description,
                                       int isPrayedFor, String datePosted, int isReported,
                                       int prayersReceivedNumber, String posterName,
                                       String profilePic, int posterId) {
        Intent intent = new Intent(MainActivity.this, PrayerRequestDetailsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("prayer_id", prayerID);
        intent.putExtra("is_prayed_for", isPrayedFor);
        intent.putExtra("date_posted", datePosted);
        intent.putExtra("is_reported", isReported);
        intent.putExtra("prayers_got_number", prayersReceivedNumber);
        intent.putExtra("poster_name", posterName);
        intent.putExtra("poster_pic", profilePic);
        intent.putExtra("poster_id", posterId);
        startActivity(intent);

    }

    @Override
    public void viewAllPrayerClick() {
        currentFragment = 2;
        bottomNavigationView.setSelectedItemId(R.id.navigation_prayer);
        switchFragment(2, TAG_FRAGMENT_PRAYER);
    }

    @Override
    public void viewAllTestimoniesClick() {
        currentFragment = 1;
        bottomNavigationView.setSelectedItemId(R.id.navigation_testimonies);
        switchFragment(1, TAG_FRAGMENT_TESTIMONY);
    }

    //async task to get random verse from db
    private class getRandomVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = mainActivityViewModel.getRandomVerseContent();
            if (cursor.moveToFirst()) {
                random_articleID = cursor.getInt(cursor.getColumnIndex("_id"));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent(MainActivity.this, BibleVerses.class);
            intent.putExtra("V-ID", random_articleID);
            startActivity(intent);

        }
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.nav_drawer_fragments_container);

        if(bottomNavigationView.getSelectedItemId () != R.id.navigation_home){
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        } /*else if(f instanceof BrowseAdvertsFragment){
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }*/
        else {
            super.onBackPressed();
        }
    }

    public static MainActivity getInstance() {
        return mainActivity;
    }

    private class getWordMatchesAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            //searchView.setIconifiedByDefault(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorites) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
                //startActivityAfterAd("favs");
            } else {
                Log.e(TAG,"Ad did not load");

            }
            Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
            intent.putExtra("favourite_verses", "favourites");
            startActivity(intent);
        }
        else if (id == R.id.action_random_verse) {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                Log.e(TAG,"Ad did not load");
            }
            new getRandomVerse().execute();
        }
        else if (id == R.id.action_about) {
            AboutLifeIssues.Show(this);
        }
        else if (id == R.id.action_settings) {
            currentFragment = 0;
            bottomNavigationView.setSelectedItemId(R.id.navigation_account);
            switchFragment(3, TAG_FRAGMENT_ACCOUNT);

        }
        else if (id == R.id.action_privacy_policy){
            openPrivacyPolicyLink();
        }
        else if (id == R.id.action_search){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //method to setup bottom navigation
    private void setUpBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setUpNavigationContent(bottomNavigationView);
    }

    private void setUpNavigationContent(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemId = item.getItemId();

                        Intent intent;
                        if (itemId == R.id.navigation_home) {
                            currentFragment = 0;
                            switchFragment(0, TAG_FRAGMENT_HOME);
                            //mTextMessage.setText(R.string.title_home);
                            return true;
                        }  else if (itemId == R.id.navigation_testimonies) {
                            currentFragment = 1;
                            switchFragment(1, TAG_FRAGMENT_TESTIMONY);

                            return true;
                        } else if (itemId == R.id.navigation_prayer) {
                            currentFragment = 2;
                            switchFragment(2, TAG_FRAGMENT_PRAYER);

                            return true;
                        } else if (itemId == R.id.navigation_account) {
                            currentFragment = 3;
                            switchFragment(3, TAG_FRAGMENT_ACCOUNT);
                            return true;
                        }
                        return false;
                    }
                });
    }

    //method to switch correctly between the bottom navigation fragments
    private void switchFragment(int pos, String tag) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentHomeFrame, fragments.get(pos), tag)
                .commit();
    }

    private void buildFragmentList() {
        HomeFragment homeFragment = buildHomeFragment();
        TestimonyFragment testimonyFragment = buildTestimonyFragment();
        PrayerFragment prayerFragment = buildPrayerFragment();
        SettingsFragment settingsFragment = buildSettingsFragment();

        fragments.add(homeFragment);
        fragments.add(testimonyFragment);
        fragments.add(prayerFragment);
        fragments.add(settingsFragment);
    }

    private HomeFragment buildHomeFragment() {
        HomeFragment fragment;
        fragment = HomeFragment.newInstance();

        return fragment;
    }

    private PrayerFragment buildPrayerFragment() {

        return PrayerFragment.newInstance();
    }

    private TestimonyFragment buildTestimonyFragment() {

        return TestimonyFragment.newInstance();
    }

    private SettingsFragment buildSettingsFragment() {
        return new  SettingsFragment();
    }

    //user confirm log out
    private void confirmLogoutDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //alertDialog.setCancelable(false);
        alertDialog.setTitle("Log Out");
        alertDialog.setMessage("Are you sure you want to log out?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //clear from prefs
                //delete user from db
                //start main activity
                pDialog.setMessage("Logging Out ...");
                showDialog();
                //deletes user from sqlite db - room
                userProfileActivityViewModel.delete();

                //delete the user fcm and access token
                userProfileActivityViewModel.logOutUser(userId);


            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();
    }

    //called in LoginUser when the user has been logged out
    public void logOutUser(boolean isLogout, String msg){
        hideDialog();
        if(isLogout){
            session.clearPrefs();

            Toast.makeText(this, msg,
                    Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, MainActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Add new Flag to start new Activity
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Starting Login Activity
            startActivity(i);
            finish();
        }else{
            Toast.makeText(this, msg,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoginNoticeDialog(String noticeMsg){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Login or Register");
        //alertDialog.setMessage("Login to post your task or make offers to posted tasks.");
        alertDialog.setMessage(noticeMsg);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //session.logoutUser();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                // Starting Login Activity
                startActivity(i);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //HomeActivity.this.finish();
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    //method to go to the website where the privacy policy resides
    private void openPrivacyPolicyLink(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacy_policy_link));
        startActivity(browserIntent);
    }

    //set up interstitial ad
    private void setUpInterstitial(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                MainActivity.this.mInterstitialAd = interstitialAd;
                //Log.i(TAG, "onAdLoaded");
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        //Log.d("TAG", "The ad was dismissed.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        //Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        //Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                //Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
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