package com.lifeissues.lifeissues.ui.activities;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE;
import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_BIBLE_NAME;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.database.BibleNamesDao;
import com.lifeissues.lifeissues.data.database.IssuesProvider;
import com.lifeissues.lifeissues.data.database.NamesProvider;
import com.lifeissues.lifeissues.models.BibleName;
import com.lifeissues.lifeissues.ui.adapters.BibleNamesAdapter;
import com.lifeissues.lifeissues.ui.utilities.InjectorUtils;
import com.lifeissues.lifeissues.ui.viewmodels.SearchNamesActivityViewModel;
import com.lifeissues.lifeissues.ui.viewmodels.SearchNamesViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class BibleNamesDictionaryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        BibleNamesAdapter.BibleNamesListAdapterListener {
    private static final String LOG_TAG = BibleNamesDictionaryActivity.class.getSimpleName();
    private SearchNamesActivityViewModel viewModel;
    private BibleNamesAdapter bibleNamesAdapter;
    private ProgressBar progressBar;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView recyclerView;
    private List<BibleName> nameList = new ArrayList<BibleName>();
    private static BibleNamesDictionaryActivity activityInstance;
    private String query;
    private SearchView searchView;
    private SearchManager searchManager;
    private CursorAdapter suggestionAdapter;
    private ListView mListView;
    private SimpleCursorAdapter names;
    private AdView mAdView;
    private AdRequest adRequest;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_names_dictionary);
        setupActionBar();

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE)
                //.setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
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
                Log.e(LOG_TAG, "Ad loaded");
                mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                //showToast(String.format("Ad failed to load with error code %d.", errorCode));
                String error =
                        String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());

                Log.e(LOG_TAG, "onAdFailedToLoad() with error: "+error);
            }

            @Override
            public void onAdOpened() {
                //showToast("Ad opened.");
                Log.e(LOG_TAG, "Ad opened");
            }

            @Override
            public void onAdClosed() {
                Log.e(LOG_TAG, "Ad closed");
            }

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e(LOG_TAG, "is receiving ads "+adRequest.isTestDevice (this));
        mAdView.loadAd(adRequest);

        getAllWidgets();
        SearchNamesViewModelFactory searchFactory = InjectorUtils.provideSearchJobsViewModelFactory(getApplicationContext());
        viewModel = new ViewModelProvider
                (this, searchFactory).get(SearchNamesActivityViewModel.class);
        //viewModel = new ViewModelProvider(this).get(SearchNamesActivityViewModel.class);

        setAdapter();
        getNamesList();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //get the widgets
    public void getAllWidgets(){
        mListView = findViewById(R.id.list);
        recyclerView = findViewById(R.id.bible_names_recycler_view);
        progressBar = findViewById(R.id.bible_names_progress_bar);
        hideBar();
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        bibleNamesAdapter = new BibleNamesAdapter(this,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(bibleNamesAdapter);
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
            Log.e(LOG_TAG, "Search query ACTION_VIEW = "+intent.getData());
            Intent wordIntent = new Intent(this, NameDetailsActivity.class);
            wordIntent.setData(intent.getData());
            startActivity(wordIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            //Log.e(LOG_TAG, "Search query = "+query);
            showResults(query);
        }
    }

    /**
     * Searches the phrases and displays results for the given query.
     * //@param query The search query
     */

    private void showResults(String query) {

        //async task to get results from the db in background
        new getSearchResultsAsyncTask().execute(query);
    }

    private void getNamesList(){
        viewModel.getNamesList().observe(BibleNamesDictionaryActivity.this, searchResultsList -> {
            bibleNamesAdapter.submitList(searchResultsList);
            //jobList = searchResultsList;
            Log.e(LOG_TAG, "search results list size is " +searchResultsList.size());
            //hideBar();

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.names_search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();

        final CursorAdapter suggestionAdapter = new SimpleCursorAdapter(BibleNamesDictionaryActivity.this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1},
                new int[]{android.R.id.text1},
                0);

        searchView.setSuggestionsAdapter(suggestionAdapter);

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchView.setQuery(nameList.get(position).getName(), true);
                searchView.clearFocus();
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; //false if you want implicit call to searchable activity
                // or true if you want to handle submit yourself
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //newText.length();
                Log.e(LOG_TAG, "Char length "+newText.length());
                //showResults(newText);
                //if(newText.length() > 3){
                    // Hit the network and take all the suggestions and store them in List 'suggestions'
                    *//*viewModel.searchForNames(newText).observe(BibleNamesDictionaryActivity.this, searchResultsList -> {
                        nameList = searchResultsList;
                        Log.e(LOG_TAG, "search results list size is " +searchResultsList.size());

                    });*//*
                //}
                String[] columns = { BaseColumns._ID,
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA,
                };

                MatrixCursor cursor = new MatrixCursor(columns);
                for (int i = 0; i < nameList.size(); i++) {
                    String[] tmp = {Integer.toString(nameList.get(i).getNameId()),
                            nameList.get(i).getName(), nameList.get(i).getMeaning()};
                    cursor.addRow(tmp);
                }
                suggestionAdapter.swapCursor(cursor);

                return true;
            }
        });

        return true;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.names_search_menu, menu);
        //async task to get word matches from the db in background
        new getWordMatchesAsyncTask().execute();
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        return true;
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

    private class getSearchResultsAsyncTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(final String... params) {
            query = params[0];
            return getContentResolver().query(NamesProvider.CONTENT_URI,
                    null, null, new String[]{params[0]}, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor == null) {
                // There are no results
                Toast.makeText(BibleNamesDictionaryActivity.this, "No results found", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "Search returned cursor null");
            } else {
                // Display the number of results
                int count = cursor.getCount();
                String countString = getResources().getQuantityString(R.plurals.search_results,
                        count, new Object[] {count, query});
                //mTextView.setText(countString);
                Log.e(LOG_TAG, "Search returned cursor size"+cursor.getCount());
                Toast.makeText(BibleNamesDictionaryActivity.this, countString + " results found", Toast.LENGTH_LONG).show();

                // Specify the columns we want to display in the result
                String[] from = new String[] {BibleNamesDao.KEY_MEANING,
                        KEY_BIBLE_NAME };

                // Specify the corresponding layout elements where we want the columns to go
                int[] to = new int[] {android.R.id.text1,
                        android.R.id.text2 };

                // Create a simple cursor adapter for the definitions and apply them to the ListView
                names = new SimpleCursorAdapter(BibleNamesDictionaryActivity.this,
                        R.layout.result_two, cursor, from, to, 0);
                mListView.setAdapter(names);

                // Define the on-click listener for the list items
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Build the Intent used to open WordActivity with a specific word Uri
                        Intent wordIntent = new Intent(getApplicationContext(), NameDetailsActivity.class);
                        Uri data = Uri.withAppendedPath(NamesProvider.CONTENT_URI,
                                String.valueOf(id));
                        wordIntent.setData(data);
                        startActivity(wordIntent);
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_search){
            return true;
        }else if (id == R.id.action_bible_names_intro){
            NamesIntroDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onNameRowClicked(int position) {
        BibleName bibleName = bibleNamesAdapter.getCurrentList().get(position);
        //bibleName.getName();
        Intent wordIntent = new Intent(getApplicationContext(), NameDetailsActivity.class);
        wordIntent.putExtra("name", bibleName.getName());
        wordIntent.putExtra("meaning", bibleName.getMeaning());
        startActivity(wordIntent);
    }

    private void NamesIntroDialog(){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this);
        alertDialog.setView(R.layout.bible_names_intro);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog = alertDialog.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.cancel();
            }
        });
    }
}