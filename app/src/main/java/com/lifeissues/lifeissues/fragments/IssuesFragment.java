package com.lifeissues.lifeissues.fragments;

import android.content.Context;
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
import android.widget.ListView;

import com.lifeissues.lifeissues.activities.BibleVerses;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.MainActivity;
import com.lifeissues.lifeissues.adapters.IssueListAdapter;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.models.LifeIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import database.DatabaseTable;

/**
 * Created by Emo on 8/31/2017.
 */

public class IssuesFragment extends Fragment implements IssueListAdapter.IssueListAdapterListener{
    View rootView;
    private RecyclerView recyclerView;
    private List<LifeIssue> issues = new ArrayList<>();
    private LifeIssue lifeIssue;
    public IssueSelectedListener issueSelectedListener;
    private IssueListAdapter issuesAdapter;
    private Cursor cursor,c,c1;
    private DatabaseTable dbhelper;
    private ListView mListView;
    private Context context;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_issues, container, false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        dbhelper = new DatabaseTable(MainActivity.getInstance());

        //async to do stuff in background
        new getLifeIssues().execute();

        getAllWidgets(rootView);
        setAdapter();
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
        issuesAdapter = new IssueListAdapter(MainActivity.getInstance(), issues,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.getInstance());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.getInstance(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(issuesAdapter);
    }

    //async task to get stuff from db
    private class getLifeIssues extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            cursor = dbhelper.getIssues();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            displayLifeIssues();

        }
    }

    //async task to get verses for a specific issue from db
    private class getVersesAsync extends AsyncTask<Void, Void, Void> {
        private LifeIssue mLifeIssue;
        private String mIssueName;

        public getVersesAsync(LifeIssue issue, String issueName){
            this.mLifeIssue = issue;
            this.mIssueName = issueName;
        }

        @Override
        protected Void doInBackground(Void... arg) {
            c1 = dbhelper.getBibleVerses(mIssueName);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mLifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
            //Toast.makeText(getActivity(), "num = "+ c1.getCount(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setNumVerses(LifeIssue lifeIssue, String issue){
        //issue.setNum_of_verses(Integer.toString(c1.getCount()));
        new getVersesAsync(lifeIssue,issue).execute();
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
                //lifeIssue.setVerses(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_ISSUE_VERSES)));
                //get all verses associated with this issue and count them
                c1 = dbhelper.getBibleVerses(lifeIssue.getIssueName().toLowerCase(Locale.US));
                c1.moveToFirst();
                lifeIssue.setNum_of_verses(Integer.toString(c1.getCount()));
                    lifeIssue.setVerses(c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE)));
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
            //master detail flow callback
            issueSelectedListener.onIssueSelection(position,issue.getId(),
                    issue.getIssueName().toLowerCase(Locale.US));

            Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
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
