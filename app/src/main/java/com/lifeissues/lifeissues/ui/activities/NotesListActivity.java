package com.lifeissues.lifeissues.ui.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeissues.lifeissues.ui.adapters.NoteListAdapter;

import java.util.ArrayList;
import java.util.List;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.DividerItemDecoration;
import com.lifeissues.lifeissues.helpers.Note;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.NoteActivityViewModel;

import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

public class NotesListActivity extends AppCompatActivity implements
        NoteListAdapter.NoteListAdapterListener {
    private static final String TAG = NotesListActivity.class.getSimpleName();
    private ListView listView;
    private AdView mAdView;
    private DatabaseTable dbhelper;
    private NoteActivityViewModel viewModel;
    private Cursor c,cursor;
    private List<Note> notes = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteListAdapter mAdapter;
    private TextView emptyView;
    private AdRequest adRequest;
    private InterstitialAd interstitialAd;
    private SwipeRefreshLayout swipeRefreshLayout;
    //private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;

    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_note_main);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        setupActionBar();

        viewModel = new ViewModelProvider(this).get(NoteActivityViewModel.class);
        //async to do stuff in background
        new getNoteTitles().execute();

        recyclerView = (RecyclerView) findViewById(R.id.notesListRecyclerView);
        emptyView = (TextView)findViewById(R.id.empty_notes_view);

        mAdapter = new NoteListAdapter(this, notes, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        //actionModeCallback = new ActionModeCallback();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            //intent.putExtras(dataBundle);
            intent.putExtra("note-ID", 0);
            startActivity(intent);
            finish();

            }
        });
/*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        */


    }//closing onCreate

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //async task to get stuff from db
    private class getNoteTitles extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //cursor = null;
            //cursor = dbhelper.getNotes();
            cursor = viewModel.getNotes();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getAllGoalNotes();
            //check if there is data to show otherwise display the empty view
            if (notes.isEmpty()){
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void getAllGoalNotes(){
        //swipeRefreshLayout.setRefreshing(true);
        if (cursor != null){
            cursor.moveToFirst();
            if (notes != null) {
                notes.clear();
            }
            while (!cursor.isAfterLast()){

                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_CONTENT)));
                note.setDateCreated(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_DATE_CREATED)));
                note.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_ISSUE)));
                note.setVerse(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_VERSE)));
                String favValue = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_FAVOURITE));
                if (favValue != null){
                    note.setImportant(!note.isImportant());
                }
                note.setColor(getRandomMaterialColor("400"));

                notes.add(note);
                cursor.moveToNext();
            }
            cursor.close();
        }
        mAdapter.notifyDataSetChanged();
        //swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

       if (id == R.id.action_add) {
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            intent.putExtra("note-ID", 0);
            startActivity(intent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onIconClicked(int position) {
        /*
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
*/
        //toggleSelection(position);
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        Note note = notes.get(position);
        if (note.isImportant()){//issue already a fav
            note.setImportant(!note.isImportant());
            notes.set(position, note);
            viewModel.deleteFavNote("no",note.getId());
            //dbhelper.deleteFavouriteNote(note.getId());
        }else {
            note.setImportant(!note.isImportant());
            notes.set(position, note);
            viewModel.addFavNote("yes",note.getId());
            //dbhelper.addFavouriteNote(note.getId());
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        /*
        if (mAdapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            */
            // read the message which removes bold from the row
            Note note = notes.get(position);
            note.setRead(true);
            notes.set(position, note);
            Intent intent = new Intent(NotesListActivity.this, NoteActivity.class);
            intent.putExtra("note-ID", note.getId());
            startActivity(intent);
            mAdapter.notifyDataSetChanged();

            //Toast.makeText(getApplicationContext(), "Read: " + note.getTitle(), Toast.LENGTH_SHORT).show();
        //}
    }

    //set up the interstitial ad
    private void setUpInterstitialAd(){
        // Create the InterstitialAd and set the adUnitId.
        interstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        //request for the ad
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
                        //request for another ad
                        adRequest = new AdRequest.Builder().build();

                        Log.e(TAG,"Interstitial Ad closed");
                    }
                });
    }
/*
    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
           // swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            //swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    // deleting the messages from recycler view
    private void deleteMessages() {
        mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }
    */
/*
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.favs) {

            //Intent intent = new Intent(this, FavouritesActivity.class);
            //startActivity(intent);

            Toast.makeText(getBaseContext(), "Favs",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        else if (id == R.id.randomWord){

            randomPhraseID = rand.nextInt((max - min) + 1) + min;
            Intent intent = new Intent(MainActivity.this, PhraseListActivity.class);
            intent.putExtra("P-ID", randomPhraseID);
            startActivity(intent);

        }
        else if (id == R.id.about_ateso) {
            // AboutAteso.Show(MainActivity.this);
            Toast.makeText(getBaseContext(), "Favs",
                    Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent();
            sharingIntent.setAction(Intent.ACTION_SEND);
            String shareBody = "\n I am learning to speak Ateso using the" +
                    "Learn Ateso app." +
                    "\nDownload the Learn Ateso app from the Google Playstore.";
            //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntent.setType("text/plain");
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        else if (id == R.id.settings) {
            Toast.makeText(getApplicationContext(), "Settings",
                    Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
*/
}
