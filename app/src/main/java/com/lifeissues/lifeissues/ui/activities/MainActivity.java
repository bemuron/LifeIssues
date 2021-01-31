package com.lifeissues.lifeissues.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.lifeissues.lifeissues.data.database.IssuesDao;
import com.lifeissues.lifeissues.ui.adapters.MainTabsPagerAdapter;
import com.lifeissues.lifeissues.R;

import java.util.Random;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.data.database.IssuesProvider;
import com.lifeissues.lifeissues.ui.fragments.IssuesFragment;
import com.lifeissues.lifeissues.ui.viewmodels.MainActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IssuesFragment.IssueSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static MainActivity instance;
    private MainActivityViewModel mainActivityViewModel;
    private AdView mAdView;
    private Random rand;
    private Cursor cursor,c;
    private String query;
    private int post,counter = 0,max,random_articleID,min=1;
    private ListView mListView;
    private SearchView searchView;
    private SearchManager searchManager;
    private InterstitialAd interstitialAd;
    private AdRequest adRequest;
    private SimpleCursorAdapter words;
    private String privacy_policy_link = "http://www.emtechint.com/life_issues.html", issueID;

    /**
     * The {PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link androidx.fragment.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory.
     */
    private MainTabsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

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

        //setup and initialize the interstitial ads
        // Create the InterstitialAd and set the adUnitId.
        interstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        //request for the ad
        adRequest = new AdRequest.Builder().build();
        //load it into the object
        interstitialAd.loadAd(adRequest);

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

            @Override
            public void onAdLeftApplication() {
                Log.e(TAG, "Ad left application");
            }
        });
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Today's Verse"));
        tabLayout.addTab(tabLayout.newTab().setText("Issues"));
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
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionsPagerAdapter = new MainTabsPagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener (new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (mAdView == null){

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mListView = (ListView) findViewById(R.id.list);

        handleIntent(getIntent());
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void onIssueSelection(int position, int issueID, String issueName){

    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)
        setIntent(intent);
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // handles a click on a search suggestion; launches activity to show word
            Intent wordIntent = new Intent(this, BibleVerses.class);
            //intent.putExtra("issue_ID", issue.getId());
            //intent.putExtra("issue_name", issue.getIssueName());
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            showResults(query);
        }
    }

    /**
     * Searches the issues and displays results for the given query.
     * @param query The search query
     */

    private void showResults(String query) {
        //async task to get results from the db in background
        new getSearchResultsAsyncTask().execute(query);
    }

    private class getSearchResultsAsyncTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(final String... params) {
            query = params[0];
            return getContentResolver().query(IssuesProvider.CONTENT_URI,
                    null, null, new String[]{params[0]}, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null) {
                // There are no results
                Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Search returned cursor null");
            } else {
                // Display the number of results
                int count = cursor.getCount();
                String countString = getResources().getQuantityString(R.plurals.search_results,
                        count, new Object[] {count, query});
                //mTextView.setText(countString);
                Log.e(TAG, "Search returned cursor size"+cursor.getCount());
                Toast.makeText(MainActivity.this, countString + " results found", Toast.LENGTH_LONG).show();

                // Specify the columns we want to display in the result
                String[] from = new String[] { IssuesDao.KEY_ISSUE_NAME,
                        IssuesDao.KEY_ISSUE_VERSES };

                // Specify the corresponding layout elements where we want the columns to go
                int[] to = new int[] {android.R.id.text1,
                        android.R.id.text2 };

                // Create a simple cursor adapter for the definitions and apply them to the ListView
                words = new SimpleCursorAdapter(MainActivity.this,
                        R.layout.result, cursor, from, to, 0);
                mListView.setAdapter(words);

                // Define the on-click listener for the list items
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Build the Intent used to open WordActivity with a specific word Uri
                        Intent wordIntent = new Intent(getApplicationContext(), BibleVerses.class);
                        Uri data = Uri.withAppendedPath(IssuesProvider.CONTENT_URI,
                                String.valueOf(id));
                        wordIntent.setData(data);
                        startActivity(wordIntent);
                    }
                });
            }
        }
    }

    //async task to get random verse from db
    private class getRandomVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //max = mainActivityViewModel.countAllBibleVerses();
            //rand = new Random();
            //random_articleID = rand.nextInt((max - min) + 1) + min;
            cursor = mainActivityViewModel.getRandomVerseContent();
            if (cursor.moveToFirst()) {
                random_articleID = cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID));
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static MainActivity getInstance() {
        return instance;
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

        //async task to get word matches from the db in background
        new getWordMatchesAsyncTask().execute();
        //searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //searchView.setSuggestionsAdapter(words);
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false);
        //searchView.setOnQueryTextListener(onQueryTextListener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notes) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
                startActivityAfterAd("notes");
            } else {
                Log.e(TAG,"Ad did not load");
                Intent intent = new Intent(this, NotesListActivity.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.action_favorites) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
                //startActivityAfterAd("favs");
            } else {
                Log.e(TAG,"Ad did not load");

            }
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_random_verse) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
            } else {
                Log.e(TAG,"Ad did not load");
            }
            new getRandomVerse().execute();
        }
        else if (id == R.id.action_about) {
            AboutLifeIssues.Show(this);
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_privacy_policy){
            openPrivacyPolicyLink();
        }
        else if (id == R.id.action_search){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
                startActivityAfterAd("notes");
            } else {
                Log.e(TAG,"Ad did not load");
                Intent intent = new Intent(this, NotesListActivity.class);
                startActivity(intent);
            }

        }else if (id == R.id.nav_favourites) {
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show();
                startActivityAfterAd("favs");
            } else {
                Log.e(TAG,"Ad did not load");
                Intent intent = new Intent(this, FavouritesActivity.class);
                startActivity(intent);
            }

        } else if (id == R.id.nav_random) {
            new getRandomVerse().execute();

        } else if (id == R.id.nav_about) {
            AboutLifeIssues.Show(this);

        } else if (id == R.id.nav_privacy_policy) {
            openPrivacyPolicyLink();

        }else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //method to go to the website where the privacy policy resides
    private void openPrivacyPolicyLink(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacy_policy_link));
        startActivity(browserIntent);
    }

    //show the ad
    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.e(TAG,"Ad did not load");
        }
    }


    //set up the interstitial ad
    private void startActivityAfterAd(String activityName){

        interstitialAd.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
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
                        if (activityName.equals("favs")){
                            Intent intent = new Intent(MainActivity.this, FavouritesActivity.class);
                            startActivity(intent);
                        }else if (activityName.equals("notes")){
                            Intent intent = new Intent(MainActivity.this, NotesListActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

}