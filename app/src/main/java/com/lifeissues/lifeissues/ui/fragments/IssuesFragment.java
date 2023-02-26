package com.lifeissues.lifeissues.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.ui.activities.BibleVerses;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.activities.FavouritesActivity;
import com.lifeissues.lifeissues.ui.activities.MainActivity;
import com.lifeissues.lifeissues.ui.adapters.IssueListAdapter;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.models.LifeIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

/**
 * Created by Emo on 8/31/2017.
 */

public class IssuesFragment extends Fragment implements IssueListAdapter.IssueListAdapterListener{
    private static final String TAG = IssuesFragment.class.getSimpleName();
    View rootView;
    private RecyclerView recyclerView;
    private List<LifeIssue> issues = new ArrayList<>();
    private AppExecutors mExecutors;
    private LifeIssue lifeIssue;
    public IssueSelectedListener issueSelectedListener;
    private IssueListAdapter issuesAdapter;
    private Cursor cursor,c,c1;
    private DatabaseTable dbhelper;
    private BibleVersesActivityViewModel viewModel;
    private ListView mListView;
    private Context context;
    private InterstitialAd mInterstitialAd;

    public IssuesFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static IssuesFragment newInstance() {
        IssuesFragment fragment = new IssuesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = AppController.getContext();

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                //.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_issues, container, false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        dbhelper = new DatabaseTable(MainActivity.getInstance());
        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);
        mExecutors = AppExecutors.getInstance();

        getAllWidgets(rootView);
        setAdapter();
        //async to do stuff in background
        new getLifeIssues().execute();
        return rootView;
    }

    //MasterDetailMainActivity must implement this interface
    public interface IssueSelectedListener {
        void onIssueSelection(int position, int issueID, String issueName);
    }

    @Override
    public void onAttach(Context context) { //Try Context context as the parameter. It is not deprecated
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            issueSelectedListener = (IssueSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IssueSelectedListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        //SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //searchView.setSuggestionsAdapter(getActivity().geta);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
    }
*/
    public void getAllWidgets(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.issues_recycler_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.getInstance());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), LinearLayoutManager.VERTICAL));
        issuesAdapter = new IssueListAdapter(MainActivity.getInstance(), issues,this);
        recyclerView.setAdapter(issuesAdapter);
    }

    //async task to get stuff from db
    private class getLifeIssues extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //cursor = dbhelper.getIssues();
            cursor = viewModel.getIssues();
            if (cursor != null) {
                cursor.moveToFirst();
                if (issues != null) {
                    issues.clear();
                }
                while (!cursor.isAfterLast()) {
                    lifeIssue = new LifeIssue();
                    lifeIssue.setId(cursor.getInt(cursor.getColumnIndex("rowid")));
                    lifeIssue.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME)));
                    Log.e(TAG, "issue name "+cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME))+ " ID = " +lifeIssue.getId());
                    //get the number of verses in this issue
                    c1 = viewModel.getBibleVersesForIssue(cursor.getInt(cursor.getColumnIndex("rowid")));
                    c1.moveToFirst();
                    lifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
                    lifeIssue.setVerses(c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE)));

                    //check if this verse is a favorite
                    c = viewModel.getFavoriteIssue(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME)));
                    if (c.getCount() == 1) {
                        //lifeIssue.setImportant(true);
                        lifeIssue.setImportant(!lifeIssue.isImportant());
                        //Toast.makeText(getActivity(), "fav ="+c.getCount(), Toast.LENGTH_SHORT).show();
                    }
                    issues.add(lifeIssue);
                    cursor.moveToNext();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            /*while (!cursor.isAfterLast()) {
                //get the number of verses in this issue
                new getVersesAsync(lifeIssue.getId()).execute();
                //check if this verse is a favorite
                new checkIfFavoriteAsync(lifeIssue.getIssueName()).execute();
            }
            cursor.moveToNext();*/
            issuesAdapter.notifyDataSetChanged();
            //displayLifeIssues();
        }
    }

    //async task to get verses for a specific issue from db
    private class getVersesAsync extends AsyncTask<Void, Void, Void> {
        private LifeIssue mLifeIssue;
        private int mIssueId;

        public getVersesAsync(int issueId){
            this.mIssueId = issueId;
        }

        @Override
        protected Void doInBackground(Void... arg) {
            //c1 = dbhelper.getBibleVerses(mIssueId);
            c1 = viewModel.getBibleVersesForIssue(mIssueId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG,"Verses cursor = "+c1);
            c1.moveToFirst();
            lifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
            lifeIssue.setVerses(c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE)));
            //Toast.makeText(getActivity(), "num = "+ c1.getCount(), Toast.LENGTH_SHORT).show();
            //issuesAdapter.notifyDataSetChanged();
        }
    }

    //async task to get verses for a specific issue from db
    private class checkIfFavoriteAsync extends AsyncTask<Void, Void, Void> {
        private String mIssueName;

        public checkIfFavoriteAsync(String issueName){
            this.mIssueName = issueName;
        }

        @Override
        protected Void doInBackground(Void... arg) {
            c = viewModel.getFavoriteIssue(mIssueName);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (c.getCount() == 1) {
                //lifeIssue.setImportant(true);
                lifeIssue.setImportant(!lifeIssue.isImportant());
                //Toast.makeText(getActivity(), "fav ="+c.getCount(), Toast.LENGTH_SHORT).show();
                //issuesAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setNumVerses(LifeIssue lifeIssue, int id){
        //issue.setNum_of_verses(Integer.toString(c1.getCount()));
        //new getVersesAsync(lifeIssue,id).execute();
    }

    public void displayLifeIssues() {
        if (cursor != null){
            cursor.moveToFirst();
            if (issues != null) {
                issues.clear();
            }
            while (!cursor.isAfterLast()){

                lifeIssue = new LifeIssue();
                lifeIssue.setId(cursor.getInt(cursor.getColumnIndex("rowid")));
                lifeIssue.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME)));
                Log.e(TAG, "issue name "+cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME))+ "ID =" +lifeIssue.getId());
                //lifeIssue.setVerses(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_VERSES)));
                //get all verses associated with this issue and count them
                //c1 = dbhelper.getBibleVerses(lifeIssue.getIssueName().toLowerCase(Locale.US));
                //new getVersesAsync(lifeIssue.getId()).execute();
                //c1 = viewModel.getBibleVersesForIssue(lifeIssue.getId());
                /*c1.moveToFirst();
                lifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
                lifeIssue.setVerses(c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE)));*/
                //check if the issue is favourite
                //checkFavourites(lifeIssue.getIssueName());
                //new checkIfFavoriteAsync(lifeIssue.getIssueName()).execute();
                //c = viewModel.getFavoriteIssue(lifeIssue.getIssueName());
                /*if (c.getCount() == 1) {
                    //lifeIssue.setImportant(true);
                    lifeIssue.setImportant(!lifeIssue.isImportant());
                    //Toast.makeText(getActivity(), "fav ="+c.getCount(), Toast.LENGTH_SHORT).show();
                }*/
                /*issues.add(lifeIssue);
                issuesAdapter.notifyDataSetChanged();*/
                cursor.moveToNext();
            }
        }
        //issuesAdapter.notifyDataSetChanged();
    }

    private void checkFavourites(String issueName){
        c = viewModel.getFavoriteIssue(issueName);
        if (c.getCount() > 1){
            lifeIssue.setImportant(true);
        }
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        /*if (issuesAdapter.getSelectedItemCount() > 0) {
            //enableActionMode(position);
        } else {*/

        // read the message which removes bold from the row
        LifeIssue issue = issues.get(position);
        //issue.setRead(true);
        issues.set(position, issue);
        //master detail flow callback
        issueSelectedListener.onIssueSelection(position,issue.getId(),
                issue.getIssueName().toLowerCase(Locale.US));

        Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
        //Toast.makeText(getActivity(), "id = "+ issue.getId(), Toast.LENGTH_SHORT).show();
        intent.putExtra("issue_ID", issue.getId());
        Log.e(TAG,"Issue ID to Bible Verses activity = "+issue.getId());
        intent.putExtra("favourite_issues", "no");
        intent.putExtra("issue_name", issue.getIssueName().toLowerCase(Locale.US));
        startActivity(intent);

        /*if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            doActionAfterAd("bibleVerses",position);
        } else {
            Log.e(TAG,"Ad did not load");
            openActivityAfterAd(position);
        }*/

        //}
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        LifeIssue issue = issues.get(position);
        if (issue.isImportant()){//issue already a fav
            issue.setImportant(!issue.isImportant());
            issues.set(position, issue);
            viewModel.deleteFavIssue(issue.getIssueName());
        }else {
            issue.setImportant(!issue.isImportant());
            issues.set(position, issue);
            viewModel.addFavIssue(issue.getId(), issue.getIssueName(), issue.getVerses());
        }
        issuesAdapter.notifyDataSetChanged();
    }

    private void openActivityAfterAd(int position){
        // read the message which removes bold from the row
        LifeIssue issue = issues.get(position);
        //issue.setRead(true);
        issues.set(position, issue);
        //master detail flow callback
        issueSelectedListener.onIssueSelection(position,issue.getId(),
                issue.getIssueName().toLowerCase(Locale.US));

        Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
        //Toast.makeText(getActivity(), "id = "+ issue.getId(), Toast.LENGTH_SHORT).show();
        intent.putExtra("issue_ID", issue.getId());
        Log.e(TAG,"Issue ID to Bible Verses activity = "+issue.getId());
        intent.putExtra("issue_name", issue.getIssueName().toLowerCase(Locale.US));
        startActivity(intent);
    }

    //set up interstitial ad
    private void setUpInterstitial(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(),getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                IssuesFragment.this.mInterstitialAd = interstitialAd;
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
