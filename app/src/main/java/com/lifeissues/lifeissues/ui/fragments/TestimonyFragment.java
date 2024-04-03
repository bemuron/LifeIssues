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
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.ui.adapters.BrowseTestimoniesAdapter;
import com.lifeissues.lifeissues.ui.viewmodels.TestimonyPrayerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Emo on 8/31/2017.
 */

public class TestimonyFragment extends Fragment implements View.OnClickListener,
        BrowseTestimoniesAdapter.BrowseTestimoniesListAdapterListener{
    private static final String TAG = TestimonyFragment.class.getSimpleName();
    View rootView;
    private RecyclerView recyclerView;
    private List<Testimony> testimoniesList = new ArrayList<>();
    private AppExecutors mExecutors;
    public TestimonyClickedListener testimonyClickedListener;
    public OnCreateNewTestimonyClick onCreateNewTestimonyClick;
    private BrowseTestimoniesAdapter browseTestimoniesAdapter;
    private TestimonyPrayerViewModel viewModel;
    private TextView emptyView;
    private PagedList<Testimony> testimonyPagedList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private int mPosition = RecyclerView.NO_POSITION;
    private Context context;
    private InterstitialAd mInterstitialAd;
    private FloatingActionButton floatingActionButton;
    public static TestimonyFragment testimonyFragment;

    public TestimonyFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static TestimonyFragment newInstance() {
        TestimonyFragment fragment = new TestimonyFragment();
        testimonyFragment = fragment;
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
        rootView = inflater.inflate(R.layout.fragment_testimony, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle("Testimonies");

        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(TestimonyPrayerViewModel.class);
        mExecutors = AppExecutors.getInstance();

        getAllWidgets(rootView);
        setAdapter();
        //async to do stuff in background
        if (testimonyPagedList == null || testimoniesList.size() == 0){
            if (AppController.isNetworkAvailable(getActivity().getApplicationContext())){
                fetchTestimoniesList();
            }
        }
        return rootView;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.testimony_fab) {
            //add new testimony
            onCreateNewTestimonyClick.onCreateNewTestimony();
        }
    }

    //MasterDetailMainActivity must implement this interface
    public interface TestimonyClickedListener {
        void onTestimonyClicked(int position, int testimonyID, String title,
                                String description,int isLiked,
                                String datePosted,int isReported,
                                int likesNumber, String posterName,
                                String profilePic, int posterId);
    }

    public interface OnCreateNewTestimonyClick{
        void onCreateNewTestimony();
    }

    @Override
    public void onAttach(Context context) { //Try Context context as the parameter. It is not deprecated
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            testimonyClickedListener = (TestimonyClickedListener) context;
            onCreateNewTestimonyClick = (OnCreateNewTestimonyClick) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TestimonyClickedListener & onCreateNewTestimonyClick");
        }
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (testimonyPagedList != null || testimoniesList.size() > 0){
            browseTestimoniesAdapter.submitList(testimonyPagedList);
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
        recyclerView = (RecyclerView) view.findViewById(R.id.testimony_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.testimonies_list_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!AppController.isNetworkAvailable(getActivity().getApplicationContext())){
                    Toast.makeText(getActivity().getApplicationContext(), "Please check your internet connection", Toast.LENGTH_LONG).show();
                    hideBar();
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    if (viewModel.browseTestimoniesDataFactory.getBrowsedTestimoniesLiveDataSource() != null) {
                        viewModel.refreshTestimoniesList();
                    } else {
                        fetchTestimoniesList();
                    }
                }
            }
        });
        emptyView = view.findViewById(R.id.empty_testimonies_view);
        // Progress bar
        progressBar = view.findViewById(R.id.browse_testimonies_progress_bar);
        showBar();
        floatingActionButton = view.findViewById(R.id.testimony_fab);
        floatingActionButton.setOnClickListener(this);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), LinearLayoutManager.VERTICAL));
        browseTestimoniesAdapter = new BrowseTestimoniesAdapter(getActivity(), this,1);
        recyclerView.setAdapter(browseTestimoniesAdapter);
    }

    public void isListEmpty(boolean isEmpty){
        Log.e(TAG, "Calling isListEmpty value = "+isEmpty);
        if (isEmpty) {
            Log.e(TAG, "List is empty");
            emptyView.setText(R.string.empty_testimonies_list);
            emptyView.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.GONE);
        }else{
            emptyView.setVisibility(View.INVISIBLE);
            Log.e(TAG, "List not empty");
        }
        hideBar();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onTestimonyRowClicked(int position) {
        Testimony testimony = testimoniesList.get(position);
        //issue.setRead(true);
        //master detail flow callback
        testimonyClickedListener.onTestimonyClicked(position,testimony.getTestimony_id(),
                testimony.getTestimony_name().toLowerCase(Locale.US),
                testimony.getContent(),
                testimony.getIs_liked(),testimony.getPosted_on(),
                testimony.getIs_reported(),testimony.getLikes_number(),
                testimony.getUser_name(),testimony.getProfile_pic(),
                testimony.getPosted_by());

        /*if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            doActionAfterAd("bibleVerses",position);
        } else {
            Log.e(TAG,"Ad did not load");
            openActivityAfterAd(position);
        }*/
    }

    private void fetchTestimoniesList(){
        Log.e(TAG, "Getting Testimonies ");

        viewModel.getBrowsedTestimoniesLiveData().observe(getViewLifecycleOwner(), testimonyList -> {
            testimonyPagedList = testimonyList;
            testimoniesList = testimonyList;
            //Log.e(LOG_TAG, "Browsed jobs list size is " + browsedJobsList.size());
            browseTestimoniesAdapter.submitList(testimonyPagedList);

            if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
            recyclerView.smoothScrollToPosition(mPosition);

        });

        if (testimoniesList.size() > 0){
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
                TestimonyFragment.this.mInterstitialAd = interstitialAd;
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
