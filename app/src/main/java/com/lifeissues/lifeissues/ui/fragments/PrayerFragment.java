package com.lifeissues.lifeissues.ui.fragments;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.ui.adapters.BrowsePrayerRequestAdapter;
import com.lifeissues.lifeissues.ui.viewmodels.TestimonyPrayerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Emo on 8/31/2017.
 */

public class PrayerFragment extends Fragment implements View.OnClickListener,
        BrowsePrayerRequestAdapter.BrowseRequestsListAdapterListener{
    private static final String TAG = PrayerFragment.class.getSimpleName();
    View rootView;
    private RecyclerView recyclerView;
    private List<PrayerRequest> prayerRequestList = new ArrayList<>();
    private AppExecutors mExecutors;
    public PrayerRequestClickListener prayerRequestClickListener;
    public OnCreateNewRequestClick onCreateNewPrayerRequest;
    private BrowsePrayerRequestAdapter browsePrayerRequestAdapter;
    private TextView emptyView;
    private PagedList<PrayerRequest> prayerRequestPagedList;
    private int mPosition = RecyclerView.NO_POSITION;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TestimonyPrayerViewModel viewModel;
    private Context context;
    private InterstitialAd mInterstitialAd;
    private FloatingActionButton floatingActionButton;
    public static PrayerFragment prayerFragment;

    public PrayerFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static PrayerFragment newInstance() {
        PrayerFragment fragment = new PrayerFragment();
        prayerFragment = fragment;
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
        rootView = inflater.inflate(R.layout.fragment_prayer, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle("Prayer Requests");

        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(TestimonyPrayerViewModel.class);
        mExecutors = AppExecutors.getInstance();

        getAllWidgets(rootView);
        setAdapter();

        if (prayerRequestPagedList == null || prayerRequestList.size() == 0){
            if (AppController.isNetworkAvailable(getActivity().getApplicationContext())){
                fetchRequestsList();
            }
        }
        return rootView;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.prayer_fab) {
            //add new prayer request
            onCreateNewPrayerRequest.onCreateNewPrayerRequest();
        }
    }

    @Override
    public void onRequestRowClicked(int position) {
        PrayerRequest prayerRequest = prayerRequestList.get(position);
        //issue.setRead(true);
        prayerRequestClickListener.onPrayerRequestClicked(position,prayerRequest.getPrayer_id(),
                prayerRequest.getPrayer_title().toLowerCase(Locale.US),
                prayerRequest.getDescription(),
                prayerRequest.getIs_prayed_for(),prayerRequest.getPosted_on(),
                prayerRequest.getIs_reported(),prayerRequest.getPrayersReceived(),
                prayerRequest.getUser_name(),prayerRequest.getProfile_pic(),
                prayerRequest.getPosted_by());
        /*if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            doActionAfterAd("bibleVerses",position);
        } else {
            Log.e(TAG,"Ad did not load");
            openActivityAfterAd(position);
        }*/
    }

    //MasterDetailMainActivity must implement this interface
    public interface PrayerRequestClickListener {
        void onPrayerRequestClicked(int position, int prayerID, String title,
                                    String description,int isPrayedFor,
                                    String datePosted,int isReported,
                                    int prayersReceivedNumber, String posterName,
                                    String profilePic, int posterId);

    }

    public interface OnCreateNewRequestClick{
        void onCreateNewPrayerRequest();
    }

    @Override
    public void onAttach(Context context) { //Try Context context as the parameter. It is not deprecated
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            prayerRequestClickListener = (PrayerRequestClickListener) context;
            onCreateNewPrayerRequest = (OnCreateNewRequestClick) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement PrayerRequestClickListener & onCreateNewPrayerRequest");
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (prayerRequestPagedList != null || prayerRequestList.size() > 0){
            browsePrayerRequestAdapter.submitList(prayerRequestPagedList);
            hideBar();
        }
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
        recyclerView = view.findViewById(R.id.prayer_recycler_view);
        floatingActionButton = view.findViewById(R.id.prayer_fab);
        floatingActionButton.setOnClickListener(this);
        swipeRefreshLayout = view.findViewById(R.id.requests_list_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!AppController.isNetworkAvailable(getActivity().getApplicationContext())){
                    Toast.makeText(getActivity().getApplicationContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
                    hideBar();
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    if (viewModel.browsePrayerRequestsDataFactory.getBrowsedPrayerRequestsLiveDataSource() != null) {
                        viewModel.refreshRequestsList();
                    } else {
                        fetchRequestsList();
                    }
                }
            }
        });
        emptyView = view.findViewById(R.id.empty_requests_view);
        // Progress bar
        progressBar = view.findViewById(R.id.browse_requests_progress_bar);
        showBar();
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), LinearLayoutManager.VERTICAL));
        browsePrayerRequestAdapter = new BrowsePrayerRequestAdapter(getActivity(), this);
        recyclerView.setAdapter(browsePrayerRequestAdapter);
    }

    public void isListEmpty(boolean isEmpty){
        //Log.e(LOG_TAG, "Calling isListEmpty value = "+isEmpty);
        if (isEmpty) {
            Log.e(TAG, "List is empty");
            emptyView.setText(R.string.empty_requests_list);
            emptyView.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.GONE);
        }else{
            emptyView.setVisibility(View.INVISIBLE);
            Log.e(TAG, "List not empty");
        }
        hideBar();
        swipeRefreshLayout.setRefreshing(false);
    }

    //get the list of requests to browse
    private void fetchRequestsList(){

        viewModel.getBrowsedRequestsLiveData().observe(getViewLifecycleOwner(), requestsList -> {
            prayerRequestPagedList = requestsList;
            prayerRequestList = requestsList;
            //Log.e(LOG_TAG, "Browsed jobs list size is " + browsedJobsList.size());
            browsePrayerRequestAdapter.submitList(prayerRequestPagedList);

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);

        });

        //Log.e(LOG_TAG, "Jobs list size is " +jobList.size());
        if (prayerRequestList.size() > 0){
            hideBar();
        }
    }

    private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        /*getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    //set up interstitial ad
    private void setUpInterstitial(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(getActivity(),getString(R.string.interstitial_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                PrayerFragment.this.mInterstitialAd = interstitialAd;
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
