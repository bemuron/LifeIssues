package com.lifeissues.lifeissues.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.BibleVerses;
import com.lifeissues.lifeissues.activities.FavouritesActivity;
import com.lifeissues.lifeissues.adapters.FavouriteVersesListAdapter;
import com.lifeissues.lifeissues.models.FavouriteVerse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import database.DatabaseTable;

/**
 * Created by Emo on 8/31/2017.
 */

public class FavouriteVersesFragment extends Fragment implements FavouriteVersesListAdapter.FavVersesListAdapterListener{
    View rootView;
    private RecyclerView recyclerView;
    private List<FavouriteVerse> verses = new ArrayList<>();
    private FavouriteVerse favouriteVerse;
    private FavouriteVersesListAdapter versesListAdapter;
    private TextView emptyView;
    private Cursor cursor,c,c1;
    private DatabaseTable dbhelper;
    private Random rand;

    public FavouriteVersesFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static FavouriteVersesFragment newInstance() {
        FavouriteVersesFragment fragment = new FavouriteVersesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_fav_verses, container, false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        dbhelper = new DatabaseTable(FavouritesActivity.getInstance());

        //async to do stuff in background
        new getFavouriteVerses().execute();

        getAllWidgets(rootView);
        setAdapter();
        return rootView;
    }

    public void getAllWidgets(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.fav_verses_recycler_view);
        emptyView = (TextView) view.findViewById(R.id.empty_fav_verses_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        versesListAdapter = new FavouriteVersesListAdapter(FavouritesActivity.getInstance(), verses,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(FavouritesActivity.getInstance());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(FavouritesActivity.getInstance(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(versesListAdapter);
    }

    //async task to get stuff from db
    private class getFavouriteVerses extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = dbhelper.getAllFavouriteVerses();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            displayFavouriteVerses();

            //check if there is data to show otherwise display the empty view
            if (verses.isEmpty()){
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    public void displayFavouriteVerses() {
        if (cursor != null){
            cursor.moveToFirst();
            if(verses != null) {
                verses.clear();
            }
            while (!cursor.isAfterLast()){

                favouriteVerse = new FavouriteVerse();
                favouriteVerse.setId(cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID)));
                favouriteVerse.setIssueId(cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_ID)));
                favouriteVerse.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME)));
                favouriteVerse.setVerse(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_VERSE)));
                favouriteVerse.setCursorPosition(cursor.getPosition());


                verses.add(favouriteVerse);
                cursor.moveToNext();
            }
            //cursor.close();
        }
        versesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (versesListAdapter.getSelectedItemCount() > 0) {
            //enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            FavouriteVerse favouriteVerse = verses.get(position);
            //issue.setRead(true);
            verses.set(position, favouriteVerse);
            Intent intent = new Intent(FavouritesActivity.getInstance(), BibleVerses.class);
            //Toast.makeText(getActivity(), "cursor position = "+ favouriteVerse.getCursorPosition(), Toast.LENGTH_SHORT).show();
            intent.putExtra("favourite_verses", "favourites");
            intent.putExtra("cursor_position", favouriteVerse.getCursorPosition());
            intent.putExtra("verse_ID", favouriteVerse.getId());
            intent.putExtra("issue_ID", favouriteVerse.getIssueId());
            intent.putExtra("fav_issue_name", favouriteVerse.getIssueName().toLowerCase(Locale.US));
            startActivity(intent);

        }
    }
}
