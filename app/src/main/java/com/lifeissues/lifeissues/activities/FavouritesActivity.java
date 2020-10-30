package com.lifeissues.lifeissues.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.adapters.FavouritesTabsPagerAdapter;
import com.lifeissues.lifeissues.fragments.BibleVersesFragment;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

/**
 * Created by Emo on 7/23/2016.
 */

public class FavouritesActivity extends AppCompatActivity {
    private static final String TAG = FavouritesActivity.class.getSimpleName();
    public static FavouritesActivity instance;
    private AdView mAdView;
    private InterstitialAd interstitialAd;
    private AdRequest adRequest;

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link androidx.fragment.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link  androidx.fragment.app.FragmentStatePagerAdapter }.
     */
    private FavouritesTabsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
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

        //setUpInterstitialAd();

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

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //showInterstitial();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Verses"));
        tabLayout.addTab(tabLayout.newTab().setText("Issues"));
        tabLayout.addTab(tabLayout.newTab().setText("Notes"));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.favs_container);
        mSectionsPagerAdapter = new FavouritesTabsPagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());
        //mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener (new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }



    public static FavouritesActivity getInstance() {
        return instance;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
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
                        //request for another ad
                        adRequest = new AdRequest.Builder().build();
                    }
                });
    }

}
