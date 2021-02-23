package com.lifeissues.lifeissues.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.adapters.BibleVersesPagerAdapter;
import com.lifeissues.lifeissues.ui.fragments.BibleVersesFragment;
import com.lifeissues.lifeissues.helpers.ZoomOutPageTransformer;

import java.util.Locale;
import java.util.Random;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

/**
 * Created by Emo on 5/5/2017.
 */

public class BibleVerses extends AppCompatActivity implements BibleVersesFragment.VersionSelectedListener{
    private static final String TAG = BibleVerses.class.getSimpleName();
    private AdView mAdView;
    private BibleVersesActivityViewModel viewModel;
    Cursor c,cursor;
    private BibleVersesFragment bibleVersesFragment;
    private SharedPreferences prefs;
    public ViewPager mViewPager;
    private TextView lblCount;
    private InterstitialAd interstitialAd;
    private AdRequest adRequest;
    private String issueName,issueNameUri, favouriteVerses, favouriteIssueName, randomVerse, favoriteIssue;
    private int selectedPosition = 0, pageCount,currentPage,
            favVersePos, max,random_articleID,min=1, verseID = 0, issueId = 0, favIssueId = 0;


    //saving the page number
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count", currentPage);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPage = savedInstanceState.getInt("count");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_bible_view);

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        //setUpInterstitialAd();

        mAdView = findViewById(R.id.adView);
        mAdView.setAdListener(new AdListener() {
            private void showToast(String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                //showToast("Ad loaded.");
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
            }

            @Override
            public void onAdClosed() {
                //showToast("Ad closed.");
            }

            @Override
            public void onAdLeftApplication() {
                //showToast("Ad left application.");
            }
        });
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);

        bibleVersesFragment = new BibleVersesFragment();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
/*
        if (savedInstanceState != null) {
            bibleVersesFragment = (BibleVersesFragment) getSupportFragmentManager().getFragment(
                    savedInstanceState, BibleVersesFragment.class.getName());
        }
*/
        // Set up the ViewPager.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        lblCount = (TextView) findViewById(R.id.lbl_count);

        Intent intent = getIntent();
        favouriteVerses = intent.getStringExtra("favourite_verses");
        favouriteIssueName = intent.getStringExtra("fav_issue_name");
        favVersePos = intent.getIntExtra("cursor_position", 0);
        favoriteIssue = intent.getStringExtra("favouriteIssues");
        issueName = intent.getStringExtra("issue_name");//list click
        issueId = intent.getIntExtra("issue_ID",0);//list click / random verse
        verseID = intent.getIntExtra("V-ID",0);//random verse
        //Toast.makeText(getApplication(), "fav pos = " + favVersePos, Toast.LENGTH_SHORT).show();
        /*check whether the user is coming from a click on the list
        * or a click on the search list view
        * or a click on the random verse*/
        if ((issueId > 0) && (verseID == 0)){
            //user clicked on issue on the list from home screen
            //get content from db by passing id of the category
            Log.e(TAG,"Issue ID in Bible verses activity = "+issueId);
            Log.e(TAG,"Verse ID in Bible verses activity = "+verseID);
            new getBibleVersesAsync(issueId).execute();
        }
        else if ((verseID > 0) && (favouriteVerses == null)){
            //user has clicked on random issue/verse
            new getRandomVerseAsync(verseID).execute();

        } else if ((favouriteVerses != null) && (favouriteVerses.equals("favourites"))){
            //showing the favorite verses
            new getFavouriteVersesAsync(prefs).execute();
        }/*else if ((issueId > 0) && (verseID == 0)){
            //user clicked on issue on the list from favorites list
            new getBibleVersesAsync(issueId).execute();
        }*/
        else {//user is coming from a search query
            new getBibleVersesFromSearchAsync().execute();
        }

        //async to do stuff in background
        //new loadBibleVerses().execute();

    }// end of onCreate

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setUpAdapter(Cursor c, SharedPreferences preferences){
        //mViewPager.setOffscreenPageLimit(3);
        final PagerAdapter adapter = new BibleVersesPagerAdapter
                (getSupportFragmentManager(), c, preferences, null);
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.addOnPageChangeListener(pageChangeListener);

        if (favouriteVerses != null){
            //selectedPosition = favVersePos;
            //setCurrentItem(selectedPosition);
            mViewPager.setCurrentItem(favVersePos, false);
        }else

        setCurrentItem(currentPage);
    }

    //set up the adapter using version selected
    private void setAdapter(Cursor c, SharedPreferences preferences,String version){
        //mViewPager.setOffscreenPageLimit(c.getCount());
        final PagerAdapter adapter = new BibleVersesPagerAdapter
                (getSupportFragmentManager(), c, preferences, version);
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.addOnPageChangeListener(pageChangeListener);
        mViewPager.setCurrentItem(currentPage);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!(mViewPager.getAdapter() == null)) {

            mViewPager.getAdapter().notifyDataSetChanged();

        }

    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bible_verses_menu, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.versionsMenu, R.layout.version_spinner_dropdown_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setSelection(adapter.getPosition("MSG"));

        spinner.setAdapter(adapter);
        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        int parentId = parent.getId();
        versionSelected = (String)parent.getItemAtPosition(pos);
        Toast.makeText(this, versionSelected, Toast.LENGTH_SHORT).show();
        //versionSelectedListener.onSpinnerSelection(versionSelected);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
        }
*/
        return super.onOptionsItemSelected(item);
    }

    private void setCurrentItem(int position) {
        //mViewPager.setCurrentItem(currentPage, false);
        displayMetaInfo(currentPage);
    }

    //	page change listener
    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
            currentPage = position;
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public void displayMetaInfo(int position) {
        lblCount.setText((position + 1) + " of " + pageCount);

        //Image image = images.get(position);
        //lblTitle.setText(image.getName());
        //lblDate.setText(image.getTimestamp());
    }

    //bible verses fragment verse selection callback
    @Override
    public void onSpinnerSelection(String versionSelected){
        mViewPager.setAdapter(null);
        //recreate();
        if ((issueId > 0) && (verseID == 0)){

            new getVersesAsync(versionSelected, prefs, issueId).execute();
        }
        else if ((verseID > 0) && (favouriteVerses == null)){//user has clicked on random issue/verse

            new getSpinnerRandomVerseAsync(versionSelected, prefs, verseID).execute();

        } else if ((favouriteVerses != null) && (favouriteVerses.equals("favourites"))){
            new getSpinnerFavouriteVersesAsync(versionSelected,prefs).execute();
        }
        else {//user is coming from a search query
            new getSpinnerVersesAsyncFromSearch(versionSelected, prefs).execute();
        }
    }

    //async task to get verses for a specific issue from db
    private class getBibleVersesAsync extends AsyncTask<Void, Void, Void> {
        private int mIssueId;

        public getBibleVersesAsync(int issueId){
            this.mIssueId = issueId;
        }

        @Override
        protected Void doInBackground(Void... argo) {
            c = viewModel.getBibleVersesForIssue(mIssueId);
            Log.e(TAG,"Inside bg async cursor "+c.getCount() );
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            if(c.moveToFirst()){
                setUpAdapter(c, prefs);
            }

        }
    }

    //async task to get verses for a specific issue from db
    //when user is coming from search query
    private class getBibleVersesFromSearchAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... argo) {
            Uri uri = getIntent().getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int iIndex = cursor.getColumnIndexOrThrow("rowid");
                issueId = cursor.getInt(iIndex);
                Log.e(TAG, "rowid " + cursor.getColumnIndexOrThrow("rowid"));
                Log.e(TAG, "issue name index is " + cursor.getColumnIndexOrThrow(DatabaseTable.KEY_ISSUE_NAME));
                Log.e(TAG, "value at _id index is " + cursor.getString(0).toLowerCase(Locale.US));
                c = viewModel.getBibleVersesForIssue(issueId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            if(c.moveToFirst()){
                setUpAdapter(c, prefs);
            }
        }
    }

    //async task to get random verse
    private class getRandomVerseAsync extends AsyncTask<Void, Void, Void> {
        private int verseID;

        public getRandomVerseAsync(int verseId){
            this.verseID = verseId;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = viewModel.getSingleVerse(verseID);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = cursor.getCount();
            if (cursor.moveToFirst()) {
                issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
                setUpAdapter(cursor, prefs);
            }
            //Toast.makeText(getBaseContext(), "ID= "+ intent.getIntExtra("V-ID",0) , Toast.LENGTH_SHORT).show();
        }
    }

    //async task to get content from db from uri
    private class getVersesUriAsync extends AsyncTask<Uri, Void, Void> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(BibleVerses.this, "",
                    "Switching version...", true, false);
        }

        @Override
        protected Void doInBackground(Uri... uri) {
            cursor = getContentResolver().query(uri[0], null, null, null, null);
            pd.dismiss();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            cursor.moveToFirst();
            int iIndex = cursor.getColumnIndexOrThrow(DatabaseTable.KEY_ISSUE_NAME);
            issueNameUri = cursor.getString(iIndex).toLowerCase(Locale.US);
            //Toast.makeText(getApplication(), "issue = " + issueNameUri, Toast.LENGTH_SHORT).show();

        }
    }

    //async task to switch bible verse version from spinner callback
    private class getVersesAsync extends AsyncTask<Void, Void, Void> {
        private String mVersion;
        private SharedPreferences mPreferences;
        private int mIssueId;
        ProgressDialog pd;

        public getVersesAsync(String versionSelected, SharedPreferences prefs, int issueId){
            this.mVersion = versionSelected;
            this.mPreferences = prefs;
            this.mIssueId = issueId;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(BibleVerses.this, "",
                    "Switching version...", true, false);
        }

        @Override
        protected Void doInBackground(Void... arg) {
            c = viewModel.getBibleVersesForIssue(mIssueId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            c.moveToFirst();
            setAdapter(c, mPreferences, mVersion);
            pd.dismiss();
        }
    }

    //async task to switch bible verse version from spinner callback
    //if user initially came from a search query
    private class getSpinnerVersesAsyncFromSearch extends AsyncTask<Void, Void, Void> {
        private String mVersion;
        private SharedPreferences mPreferences;
        ProgressDialog pd;

        public getSpinnerVersesAsyncFromSearch(String versionSelected, SharedPreferences prefs){
            this.mVersion = versionSelected;
            this.mPreferences = prefs;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(BibleVerses.this, "",
                    "Switching version...", true, false);
        }

        @Override
        protected Void doInBackground(Void... arg) {
            Uri uri = getIntent().getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int iIndex = cursor.getColumnIndexOrThrow("rowid");
                issueId = cursor.getInt(iIndex);
                Log.e(TAG, "rowid " + cursor.getColumnIndexOrThrow("rowid"));
                Log.e(TAG, "issue name index is " + cursor.getColumnIndexOrThrow(DatabaseTable.KEY_ISSUE_NAME));
                Log.e(TAG, "value at _id index is " + cursor.getString(0).toLowerCase(Locale.US));
                c = viewModel.getBibleVersesForIssue(issueId);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            if(c.moveToFirst()) {
                setAdapter(c, mPreferences, mVersion);
            }
            pd.dismiss();
        }
    }

    //async task to get favourite verses from db
    private class getFavouriteVersesAsync extends AsyncTask<Void, Void, Void> {
        private SharedPreferences mPreferences;

        public getFavouriteVersesAsync(SharedPreferences prefs){
            this.mPreferences = prefs;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            c = viewModel.getAllFavouriteVerses();
            //c = dbhelper.getAllFavouriteVerses();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            c.moveToFirst();
            setUpAdapter(c, mPreferences);

        }
    }

    //async task to switch bible verse version from spinner callback
    private class getSpinnerRandomVerseAsync extends AsyncTask<Void, Void, Void> {
        private String mVersion;
        private SharedPreferences mPreferences;
        private int mVerseId;
        ProgressDialog pd;

        public getSpinnerRandomVerseAsync(String versionSelected, SharedPreferences prefs, int verseID){
            this.mVersion = versionSelected;
            this.mPreferences = prefs;
            this.mVerseId = verseID;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(BibleVerses.this, "",
                    "Switching version...", true, false);
        }

        @Override
        protected Void doInBackground(Void... arg) {
            cursor = viewModel.getSingleVerse(mVerseId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = cursor.getCount();
            if (cursor.moveToFirst()) {
                issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
                setAdapter(cursor, mPreferences, mVersion);
            }
            pd.dismiss();
        }
    }

    //async task to get favourite verses from db
    private class getSpinnerFavouriteVersesAsync extends AsyncTask<Void, Void, Void> {
        private String mVersion;
        private SharedPreferences mPreferences;

        public getSpinnerFavouriteVersesAsync(String versionSelected, SharedPreferences prefs){
            this.mVersion = versionSelected;
            this.mPreferences = prefs;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            c = viewModel.getAllFavouriteVerses();
            //c = dbhelper.getAllFavouriteVerses();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            c.moveToFirst();
            //issueName = favouriteIssueName;
            setAdapter(c, mPreferences, mVersion);

        }
    }

    //show the ad
    private void showInterstitial() {
        // Show the ad if it's ready.
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.e(TAG,"Ad did not load");
        }
    }

    //set up the interstitial ad
    private void setUpInterstitialAd(){
        // Create the InterstitialAd and set the adUnitId.
        interstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        //request for the add
        adRequest = new AdRequest.Builder().build();
        //load it into the object
        interstitialAd.loadAd(adRequest);

        interstitialAd.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        interstitialAd.show();
                        Log.i(TAG,"onAdLoaded()");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Log.e(TAG,"onAdFailedToLoad() with error: " + error);
                    }

                    @Override
                    public void onAdClosed() {
                        Log.e(TAG,"Interstitial Ad closed");
                    }
                });
    }

}
