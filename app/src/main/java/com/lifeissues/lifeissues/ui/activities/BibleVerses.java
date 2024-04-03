package com.lifeissues.lifeissues.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.BibleVerseResult;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.ui.adapters.BibleVersesPagerAdapter;
import com.lifeissues.lifeissues.ui.fragments.BibleVersesFragment;
import com.lifeissues.lifeissues.helpers.ZoomOutPageTransformer;

import java.util.ArrayList;
import java.util.List;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;

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
    private List<BibleVerseResult> bibleVerseList, favVersesList;
    private ProgressDialog pDialog;
    private TextView lblCount;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private String issueName,issueNameUri, favouriteVerses, favouriteIssueName, randomVerse;
    private int selectedPosition = 0, pageCount,currentPage,favoriteIssue,
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
                //.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        setUpInterstitial();
        showInterstitialAd();

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
        });
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);

        bibleVersesFragment = new BibleVersesFragment();

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);

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
        favoriteIssue = intent.getIntExtra("favouriteIssues",0);
        issueName = intent.getStringExtra("issue_name");//list click
        issueId = intent.getIntExtra("issue_ID",0);//list click / random verse
        verseID = intent.getIntExtra("V-ID",0);//random verse
        bibleVerseList = new ArrayList<BibleVerseResult>();
        favVersesList = new ArrayList<BibleVerseResult>();
        //Toast.makeText(getApplication(), "fav pos = " + favVersePos, Toast.LENGTH_SHORT).show();
        /*check whether the user is coming from a click on the list
        * or a click on the search list view
        * or a click on the random verse*/
        if ((issueId > 0) && (verseID == 0)){
            //user clicked on issue on the list from home screen
            //get content from db by passing id of the category
            Log.e(TAG,"Issue ID in Bible verses activity = "+issueId);
            Log.e(TAG,"Verse ID in Bible verses activity = "+verseID);
            pDialog.setMessage("Fetching Verses ...");
            showDialog();
            //new getBibleVersesAsync(issueId).execute();
            if (bibleVerseList != null) {
                bibleVerseList.clear();
            }
            viewModel.getIssueBibleVerses(issueId).observe(this, versesList -> {
                pageCount = versesList.size();
                if (bibleVerseList.size() == 0) {
                    bibleVerseList = versesList;
                    setUpAdapter(bibleVerseList,prefs);
                }
                hideDialog();
            });
            //mViewPager.setCurrentItem(currentPage, false);
        }
        else if ((verseID > 0) && (favouriteVerses == null)){
            //user has clicked on random issue/verse
            pDialog.setMessage("Fetching Verses ...");
            showDialog();
            //new getRandomVerseAsync(verseID).execute();
            viewModel.getSingleBibleVerse(verseID).observe(this, bibleVerses -> {
                pageCount = bibleVerses.size();
                setUpAdapter(bibleVerses, prefs);
                hideDialog();
            });

        } else if ((favouriteVerses != null) && (favouriteVerses.equals("favourites"))){
            /*if (favVersesList != null) {
                favVersesList.clear();
            }*/
            //showing the favorite verses
            pDialog.setMessage("Fetching Verses ...");
            showDialog();
            //new getFavouriteVersesAsync(prefs).execute();
            viewModel.getAllFavoriteVerses().observe(this, bibleVerses -> {
                pageCount = bibleVerses.size();
                if (favVersesList.size() == 0) {
                    favVersesList = bibleVerses;
                    setUpAdapter(favVersesList,prefs);
                }
                //setUpAdapter(bibleVerses, prefs);
                hideDialog();
            });
        }/*else if ((issueId > 0) && (verseID == 0)){
            //user clicked on issue on the list from favorites list
            new getBibleVersesAsync(issueId).execute();
        }*/
        else {//user is coming from a search query
            pDialog.setMessage("Fetching Verses ...");
            showDialog();
            //new getBibleVersesFromSearchAsync().execute();
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

    private void setUpAdapter(List<BibleVerseResult> bibleVerseList, SharedPreferences preferences){
        mViewPager.setOffscreenPageLimit(pageCount);
        final PagerAdapter adapter = new BibleVersesPagerAdapter
                (getSupportFragmentManager(), bibleVerseList, preferences, null);
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
    private void setAdapter(List<BibleVerseResult> bibleVerseList, SharedPreferences preferences,String version){
        mViewPager.setOffscreenPageLimit(pageCount);
        final PagerAdapter adapter = new BibleVersesPagerAdapter
                (getSupportFragmentManager(), bibleVerseList, preferences, version);
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
//        if (!(mViewPager.getAdapter() == null)) {
//            mViewPager.getAdapter().notifyDataSetChanged();
//        }

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

            if (bibleVerseList != null) {
                bibleVerseList.clear();
            }
            //new getVersesAsync(versionSelected, prefs, issueId).execute();
            viewModel.getIssueBibleVerses(issueId).observe(this, versesList -> {
                pageCount = versesList.size();
                if (bibleVerseList.size() == 0) {
                    bibleVerseList = versesList;
                    setAdapter(bibleVerseList, prefs, versionSelected);
                }
                //setAdapter(versesList, prefs, versionSelected);
                hideDialog();
            });
        }
        else if ((verseID > 0) && (favouriteVerses == null)){//user has clicked on random issue/verse
            viewModel.getSingleBibleVerse(verseID).observe(this, bibleVerses -> {
                pageCount = bibleVerses.size();
                setAdapter(bibleVerses, prefs, versionSelected);
                hideDialog();
            });

            //new getSpinnerRandomVerseAsync(versionSelected, prefs, verseID).execute();

        } else if ((favouriteVerses != null) && (favouriteVerses.equals("favourites"))){
            viewModel.getAllFavoriteVerses().observe(this, bibleVerses -> {
                pageCount = bibleVerses.size();
                setAdapter(bibleVerses, prefs, versionSelected);
                hideDialog();
            });
            //new getSpinnerFavouriteVersesAsync(versionSelected,prefs).execute();
        }
        else {//user is coming from a search query
            //new getSpinnerVersesAsyncFromSearch(versionSelected, prefs).execute();
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

    //show the ad
    private void showInterstitialAd(){
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    //set up interstitial ad
    private void setUpInterstitial(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3075330085087679/1767119183", adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d("TAG", "The ad was dismissed.");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                        mInterstitialAd = null;
                        Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

}
