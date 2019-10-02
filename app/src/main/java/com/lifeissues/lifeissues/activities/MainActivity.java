package com.lifeissues.lifeissues.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.lifeissues.lifeissues.adapters.MainTabsPagerAdapter;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.fragments.IssuesFragment;

import java.util.Random;

import database.DatabaseTable;
import database.IssuesProvider;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,IssuesFragment.IssueSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static MainActivity instance;
    private AdView mAdView;
    private Random rand;
    private Cursor cursor,c;
    private DatabaseTable dbhelper;
    private int post,counter = 0,max,random_articleID,min=1;
    private ListView mListView;
    private SimpleCursorAdapter words;
    private String privacy_policy_link = "http://www.emtechint.com/life_issues.html";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
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

        //initialise the ads
        MobileAds.initialize(this, "ca-app-pub-3075330085087679~5350882962");

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
            public void onAdFailedToLoad(int errorCode) {
                //showToast(String.format("Ad failed to load with error code %d.", errorCode));
                Log.e(TAG, "Failed to load ad "+errorCode);
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
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
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
        dbhelper = new DatabaseTable(this);


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
     * Searches the dictionary and displays results for the given query.
     * @param query The search query
     */

    private void showResults(String query) {

        Cursor cursor = getContentResolver().query(IssuesProvider.CONTENT_URI,
                null, null, new String[] {query}, null);

        if (cursor == null) {
            // There are no results
            //mTextView.setText(getString(R.string.no_results, new Object[]{query}));
            Toast.makeText(this, "No results found", Toast.LENGTH_LONG).show();
        } else {
            // Display the number of results
            int count = cursor.getCount();
            String countString = getResources().getQuantityString(R.plurals.search_results,
                    count, new Object[] {count, query});
            //mTextView.setText(countString);
            Toast.makeText(this, countString + " results found", Toast.LENGTH_LONG).show();

            // Specify the columns we want to display in the result
            String[] from = new String[] { DatabaseTable.KEY_ISSUE_NAME,
                    DatabaseTable.KEY_ISSUE_VERSES };

            // Specify the corresponding layout elements where we want the columns to go
            int[] to = new int[] {android.R.id.text1,
                    android.R.id.text2 };

            // Create a simple cursor adapter for the definitions and apply them to the ListView
            words = new SimpleCursorAdapter(this,
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

    //async task to get random verse from db
    private class getRandomVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = dbhelper.getAllBibleVerses();
            max = cursor.getCount();
            rand = new Random();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            random_articleID = rand.nextInt((max - min) + 1) + min;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        //SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //searchView.setSuggestionsAdapter(words);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

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
            Intent intent = new Intent(this, NotesListActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_favorites) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_random_verse) {
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
            Intent intent = new Intent(this, NotesListActivity.class);
            startActivity(intent);

        }else if (id == R.id.nav_favourites) {
            Intent intent = new Intent(this, FavouritesActivity.class);
            startActivity(intent);

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

    }