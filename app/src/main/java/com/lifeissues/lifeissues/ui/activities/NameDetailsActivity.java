package com.lifeissues.lifeissues.ui.activities;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;
import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_BIBLE_NAME;
import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_MEANING;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.data.database.DatabaseTable;

import java.util.Locale;

public class NameDetailsActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = NameDetailsActivity.class.getSimpleName();
    private AdView mAdView;
    private AdRequest adRequest;
    private String Bible_name, meaning, nameFromSearch;
    private TextView BibleNameTv, meaningTv;
    private ImageView shareArticleIv;
    private String app_link = "https://play.google.com/store/apps/details?id=com.lifeissues.lifeissues";
    private Cursor c,cursor;
    int iIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_details);
        setupActionBar();

        //get intent from which this activity is called
        Intent intent = getIntent();
        Bible_name = intent.getStringExtra("name");
        meaning = intent.getStringExtra("meaning");
        //set activity name
        setTitle(Bible_name);

        getAllWidgets();

        if(Bible_name == null){
            //user is coming from click on search item
            new getNameFromSearchAsync().execute();
        }else{
            //user clicked name in the list
            displayName();
        }

        //set up the ads
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

        //request for the ad
        adRequest = new AdRequest.Builder().build();

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

        });
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.e(TAG, "is receiving ads "+adRequest.isTestDevice (this));
        mAdView.loadAd(adRequest);
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
        BibleNameTv = findViewById(R.id.Bible_name);
        meaningTv = findViewById(R.id.name_details);
        shareArticleIv = findViewById(R.id.share_name_icon);
        shareArticleIv.setOnClickListener(this);
    }

    //display the name and meaning
    private void displayName(){
        BibleNameTv.setText(Bible_name);
        meaningTv.setText(meaning);
    }

    //when user is coming from search query
    private class getNameFromSearchAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... argo) {
            Uri uri = getIntent().getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                iIndex = cursor.getColumnIndexOrThrow(KEY_BIBLE_NAME);
                //issueId = cursor.getString(iIndex);
                nameFromSearch = cursor.getString(iIndex);
                Log.e(TAG, "rowid " + cursor.getColumnIndexOrThrow(KEY_BIBLE_NAME));
                Log.e(TAG, "issue name index is " + cursor.getColumnIndexOrThrow(KEY_MEANING));
                Log.e(TAG, "value at _id index is " + cursor.getString(0).toLowerCase(Locale.US));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Uri uri = getIntent().getData();
            Log.e(TAG, "URI from search " + uri);
            //Toast.makeText(NameDetailsActivity.this, "Name clicked search "+nameFromSearch, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.share_name_icon:
                shareName();
                break;

        }
    }

    //code for sharing this name with other apps
    public void shareName(){
        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        String shareBody = Bible_name+
                "\n "+meaning+
                "\n More in the Life Issues App: "+app_link;
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check this out");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        sharingIntent.setType("text/plain");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}