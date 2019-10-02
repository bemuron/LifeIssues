package com.lifeissues.lifeissues.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.adapters.BibleVersesPagerAdapter;
import com.lifeissues.lifeissues.fragments.BibleVersesFragment;
import com.lifeissues.lifeissues.helpers.ZoomOutPageTransformer;

import java.util.Locale;
import java.util.Random;

import database.DatabaseTable;

/**
 * Created by Emo on 5/5/2017.
 */

public class BibleVerses extends AppCompatActivity implements BibleVersesFragment.VersionSelectedListener{
    private AdView mAdView;
    DatabaseTable dbhelper;
    Cursor c,cursor;
    Random rand;
    private BibleVersesFragment bibleVersesFragment;
    private SharedPreferences prefs;
    public ViewPager mViewPager;
    private Intent intent;
    private TextView lblCount;
    private String issueName,issueNameUri, favouriteVerses, favouriteIssueName;
    private int selectedPosition = 0, pageCount,currentPage,favVersePos, max,random_articleID,min=1, verseID;


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

        //initialise the ads
        MobileAds.initialize(this, "ca-app-pub-3075330085087679~5350882962");

        mAdView = (AdView) findViewById(R.id.adView);
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
            public void onAdFailedToLoad(int errorCode) {
                //showToast(String.format("Ad failed to load with error code %d.", errorCode));
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
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        bibleVersesFragment = new BibleVersesFragment();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

        dbhelper = new DatabaseTable(this);
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
        issueName = intent.getStringExtra("issue_name");//list click
        verseID = intent.getIntExtra("V-ID",0);//random verse
        //Toast.makeText(getApplication(), "fav pos = " + favVersePos, Toast.LENGTH_SHORT).show();
        /*check whether the user is coming from a click on the list
        * or a click on the search list view
        * or a click on the random verse*/
        if (issueName != null){
            //get content from db by passing name of the category
            new getBibleVersesAsync(issueName).execute();
        }
        else if (verseID != 0){//user has clicked on random issue/verse
            //cursor = dbhelper.getRandomVerse(verseID);
            new getRandomVerseAsync(verseID).execute();

        } else if (favouriteVerses != null){
            new getFavouriteVersesAsync().execute();
        }
        else {//user is coming from a search query
            Uri uri = getIntent().getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int iIndex = cursor.getColumnIndexOrThrow(DatabaseTable.KEY_ISSUE_NAME);
            issueName = cursor.getString(iIndex).toLowerCase(Locale.US);
            //Toast.makeText(getApplication(), "issue = " + issueNameUri, Toast.LENGTH_SHORT).show();
            //new getVersesUriAsync().execute(uri);
            //issue_id = uri.getLastPathSegment();

            //get content from db by passing name of the issue
            //c = dbhelper.getBibleVerses(issueName);
            new getBibleVersesAsync(issueName).execute();

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
        if (issueName != null){
            //issue_id = intent.getStringExtra("issue_ID");
            //issueName = intent.getStringExtra("issue_name");
            //get content from db by passing id of the category
            //c = dbhelper.getBibleVerses(issueName);
            new getVersesAsync(versionSelected, prefs, issueName).execute();
            //pageCount = c.getCount();
            //c.moveToFirst();
            //setAdapter(c, prefs, versionSelected);
        }
        else if (verseID != 0){//user has clicked on random issue/verse
            //cursor = dbhelper.getRandomVerse(verseID);
            new getSpinnerRandomVerseAsync(versionSelected, prefs, verseID).execute();
            //pageCount = cursor.getCount();
            //cursor.moveToFirst();
            //issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            //setAdapter(cursor, prefs, versionSelected);

        } else if (favouriteVerses != null){
            new getSpinnerFavouriteVersesAsync(versionSelected,prefs).execute();
        }
        else {//user is coming from a search query
            Uri uri = getIntent().getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            //issue_id = uri.getLastPathSegment();

            cursor.moveToFirst();
            int iIndex = cursor.getColumnIndexOrThrow(DatabaseTable.KEY_ISSUE_NAME);
            issueName = cursor.getString(iIndex).toLowerCase(Locale.US);
            //Toast.makeText(this, "issue = " + issueName, Toast.LENGTH_SHORT).show();

            //get content from db by passing name of the issue
            //c = dbhelper.getBibleVerses(issueName);
            new getVersesAsync(versionSelected, prefs, issueName).execute();
            //pageCount = c.getCount();
            //c.moveToFirst();
            //Toast.makeText(getBaseContext(), "ID= "+ cat_id , Toast.LENGTH_SHORT).show();
            //setAdapter(c, prefs, versionSelected);
        }
    }

    //async task to get verses for a specific issue from db
    private class getBibleVersesAsync extends AsyncTask<Void, Void, Void> {
        private String mIssue;

        public getBibleVersesAsync(String issue){
            this.mIssue = issue;
        }

        @Override
        protected Void doInBackground(Void... argo) {
            c = dbhelper.getBibleVerses(mIssue);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            c.moveToFirst();
            setUpAdapter(c, prefs);

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
            cursor = dbhelper.getRandomVerse(verseID);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = cursor.getCount();
            cursor.moveToFirst();
            issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            setUpAdapter(cursor, prefs);
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
        private String mIssue;
        ProgressDialog pd;

        public getVersesAsync(String versionSelected, SharedPreferences prefs, String issueName){
            this.mVersion = versionSelected;
            this.mPreferences = prefs;
            this.mIssue = issueName;
        }

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(BibleVerses.this, "",
                    "Switching version...", true, false);
        }

        @Override
        protected Void doInBackground(Void... arg) {
            c = dbhelper.getBibleVerses(mIssue);
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

    //async task to get favourite verses from db
    private class getFavouriteVersesAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            c = dbhelper.getAllFavouriteVerses();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = c.getCount();
            c.moveToFirst();
            setUpAdapter(c, prefs);

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
            cursor = dbhelper.getRandomVerse(mVerseId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pageCount = cursor.getCount();
            cursor.moveToFirst();
            issueName = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            setAdapter(cursor, mPreferences, mVersion);
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
            c = dbhelper.getAllFavouriteVerses();
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

}
