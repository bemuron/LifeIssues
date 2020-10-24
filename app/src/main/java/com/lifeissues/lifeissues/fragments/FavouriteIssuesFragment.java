package com.lifeissues.lifeissues.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.BibleVerses;
import com.lifeissues.lifeissues.activities.FavouritesActivity;
import com.lifeissues.lifeissues.adapters.IssueListAdapter;
import com.lifeissues.lifeissues.models.LifeIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import database.DatabaseTable;

/**
 * Created by Emo on 8/31/2017.
 */

public class FavouriteIssuesFragment extends Fragment implements IssueListAdapter.IssueListAdapterListener{
    View rootView;
    private RecyclerView recyclerView;
    private List<LifeIssue> issues = new ArrayList<>();
    private LifeIssue lifeIssue;
    private IssueListAdapter issuesAdapter;
    private Cursor cursor,c,c1;
    private DatabaseTable dbhelper;
    private TextView emptyView;
    private Random rand;

    public FavouriteIssuesFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static FavouriteIssuesFragment newInstance() {
        FavouriteIssuesFragment fragment = new FavouriteIssuesFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_issues, container, false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        dbhelper = new DatabaseTable(FavouritesActivity.getInstance());

        //async to do stuff in background
        new getLifeIssues().execute();

        getAllWidgets(rootView);
        setAdapter();
        return rootView;
    }
/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(false);
    }
*/
    public void getAllWidgets(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.issues_recycler_view);
        emptyView = (TextView) view.findViewById(R.id.empty_fav_issues_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        issuesAdapter = new IssueListAdapter(FavouritesActivity.getInstance(), issues,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(FavouritesActivity.getInstance());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(FavouritesActivity.getInstance(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(issuesAdapter);
    }

    //async task to get stuff from db
    private class getLifeIssues extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = dbhelper.getAllFavouriteIssues();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            displayLifeIssues();
            //check if there is data to show otherwise display the empty view
            if (issues.isEmpty()){
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }

        }
    }

    public void displayLifeIssues() {
        if (cursor != null){
            cursor.moveToFirst();
            if (issues != null) {
                issues.clear();
            }
            while (!cursor.isAfterLast()){

                lifeIssue = new LifeIssue();
                lifeIssue.setId(cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID)));
                lifeIssue.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_FAV_ISSUE_NAME)));
                lifeIssue.setVerses(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_FAV_ISSUE_VERSES)));
                //get all verses associated with this issue and count them
                c1 = dbhelper.getBibleVerses(lifeIssue.getId());
                lifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
                //check if the issue is favourite
                //checkFavourites(lifeIssue.getIssueName());
                c = dbhelper.getFavouriteIssue(lifeIssue.getIssueName());
                if (c.getCount() == 1) {
                    //lifeIssue.setImportant(true);
                    lifeIssue.setImportant(!lifeIssue.isImportant());
                    //Toast.makeText(getActivity(), "fav ="+c.getCount(), Toast.LENGTH_SHORT).show();
                }


                issues.add(lifeIssue);
                cursor.moveToNext();
            }
            //cursor.close();
        }
        issuesAdapter.notifyDataSetChanged();
    }

    private void checkFavourites(String issueName){
        c = dbhelper.getFavouriteIssue(issueName);
        if (c.getCount() > 1){
            lifeIssue.setImportant(true);
        }
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (issuesAdapter.getSelectedItemCount() > 0) {
            //enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            LifeIssue issue = issues.get(position);
            //issue.setRead(true);
            issues.set(position, issue);
            Intent intent = new Intent(FavouritesActivity.getInstance(), BibleVerses.class);
            //Toast.makeText(getActivity(), "id = "+ issue.getId(), Toast.LENGTH_SHORT).show();
            intent.putExtra("issue_ID", issue.getId());
            intent.putExtra("issue_name", issue.getIssueName().toLowerCase(Locale.US));
            startActivity(intent);

        }
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        LifeIssue issue = issues.get(position);
        if (issue.isImportant()){//issue already a fav
            issue.setImportant(!issue.isImportant());
            issues.set(position, issue);
            dbhelper.deleteFavouriteIssue(issue.getIssueName());
        }else {
            issue.setImportant(!issue.isImportant());
            issues.set(position, issue);
            dbhelper.addFavouriteIssue(issue.getIssueName(), issue.getVerses());
        }
        issuesAdapter.notifyDataSetChanged();
    }

}
