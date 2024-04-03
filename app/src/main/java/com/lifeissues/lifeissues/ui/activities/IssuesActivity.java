package com.lifeissues.lifeissues.ui.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.lifeissues.lifeissues.R;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.ui.adapters.HomeIssuesGridAdapter;
import com.lifeissues.lifeissues.ui.adapters.IssueListAdapter;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IssuesActivity extends AppCompatActivity implements IssueListAdapter.IssuesGridAdapterOnItemClickHandler,
        AdapterView.OnItemSelectedListener{
    private static final String LOG_TAG = IssuesActivity.class.getSimpleName();
    private IssueListAdapter issuesGridAdapter;
    private int mPosition = RecyclerView.NO_POSITION;
    private RecyclerView mRecyclerView, categoriesSearchRv;
    //private List<Job> jobList = new ArrayList<Job>();
    private List<Issue> issues;

    private BibleVersesActivityViewModel mViewModel;
    private ProgressBar mLoadingIndicator;
    private SessionManager session;
    private int userId, userRole;
    public static IssuesActivity issuesActivity;
    private String query;
    private CursorAdapter suggestionAdapter;
    private ListView mListView;
    private TextView feedbackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);
        setupActionBar();
        setTitle("Issues");
        issuesActivity = this;

        // session manager
        session = new SessionManager(getApplicationContext());

        if (session.isLoggedIn()) {
            userRole = session.getUserRole();
            userId = session.getUserId();
        }

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = findViewById(R.id.recyclerview_categories);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. In our case, we want a vertical list, so we pass in the constant from the
         * LinearLayoutManager class for vertical lists, LinearLayoutManager.VERTICAL.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         *
         * The third parameter (shouldReverseLayout) should be true if you want to reverse your
         * layout. Generally, this is only true with horizontal lists that need to support a
         * right-to-left layout.
         */
        /*RecyclerView.LayoutManager layoutManager =
                new GridLayoutManager(this, 2);*/
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        //StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //layoutParams.setFullSpan(true);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        //mRecyclerView.setHasFixedSize(true);

        //mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(10), true));
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it is actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * PostJobActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        issuesGridAdapter = new IssueListAdapter(issues, this,this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(issuesGridAdapter);

        //view model set up
        /*
        Use ViewModelProviders to associate your ViewModel with your UI controller.
        When your app first starts, the ViewModelProviders will create the ViewModel.
        When the activity is destroyed, for example through a configuration change,
        the ViewModel persists. When the activity is re-created, the ViewModelProviders
        return the existing ViewModel
         */
        //mViewModel = ViewModelProviders.of
        //      (this, factory).get(HomeActivityViewModel.class);
        // With ViewModelFactory
        mViewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);
        issues = new ArrayList<Issue>();

        mViewModel.getIssues().observe(this,
                new Observer<List<Issue>>() {
                    @Override
                    public void onChanged(@Nullable final List<Issue> issuesList) {
                        // Update the cached copy of the categories in the adapter.
                        issues = issuesList;
                        issuesGridAdapter.setList(issuesList);

                        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
                        mRecyclerView.smoothScrollToPosition(mPosition);

                        // Show the weather list or the loading screen based on whether the forecast data exists
                        // and is loaded
                        if (issuesList != null && issuesList.size() != 0) showCategoryDataView();
                        else showLoading();
                    }
                });

        //getAllWidgets();
        //setAdapter();

        //show the keyboard
        /*((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);*/

        //handleIntent(getIntent());
    }

    public static IssuesActivity getActivityInstance(){
        return issuesActivity;
    }

    //onResume is called when the activity is relaunched again from the back stack
    @Override
    public void onResume(){
        super.onResume();

        if (session.isLoggedIn()) {
            userRole = session.getUserRole();
            userId = session.getUserId();
        }
    }

    //onPause is called when another activity takes foreground
    @Override
    public void onPause(){
        super.onPause();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //get the widgets
    /*public void getAllWidgets(){
        mListView = findViewById(R.id.list);
        categoriesSearchRv = findViewById(R.id.search_categories_recycler_view);
        feedbackView = findViewById(R.id.search_jobs_feedback_title);
    }*/

    /**
     * This method is for responding to clicks from our grid list.
     *
     * @param position position of clicked issue
     */

    @Override
    public void onItemClick(int position) {
        Issue issue = issues.get(position);
        Intent intent = new Intent(MainActivity.getInstance(), BibleVerses.class);
        intent.putExtra("issue_ID", issue.getIssue_id());
        intent.putExtra("favourite_issues", "no");
        intent.putExtra("issue_name", issue.getIssueName().toLowerCase(Locale.US));
        startActivity(intent);
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showCategoryDataView() {
        // First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // Finally, make sure the category data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the loading indicator visible and hide the category View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each view is currently visible or invisible.
     */
    private void showLoading() {
        // Then, hide the category data
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.issues_search_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_issues_search_activity).getActionView();
        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                filter(newText);
                return false;
            }
        });

        /*searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.requestFocus();*/

        return true;
    }

    private void filter(String text) {
        // creating a new array list to filter our data.
        List<Issue> filteredList = new ArrayList<Issue>();

        // running a for loop to compare elements.
        for (Issue item : issues) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getIssueName().toLowerCase().contains(text.toLowerCase())) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "We couldn't find that category", Toast.LENGTH_LONG).show();
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            issuesGridAdapter.setList(filteredList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_issues_search_activity){
            return true;
        }else if (item.getItemId() == android.R.id.home) {
            onBackPressed();    //Call the back button's method
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    //returned from ArticlesListDataSource of the data set is empty or not
    public void isListEmpty(boolean isEmpty){
        Log.e(LOG_TAG, "Inside is empty method value" +isEmpty);
        if (isEmpty) {
            Log.e(LOG_TAG, "List is empty");
            feedbackView.setText(getString(R.string.search_issues_no_results_feedback,query));
            feedbackView.setVisibility(View.VISIBLE);
            //recyclerView.setVisibility(View.GONE);
        }else{
            feedbackView.setText(getString(R.string.search_issues_feedback,query));
            feedbackView.setVisibility(View.VISIBLE);
            Log.e(LOG_TAG, "List not empty");
        }
        //hideBar();
    }

    //prompt user to login/register if not yet
    private void showLoginNoticeDialog(String noticeMsg){
        MaterialAlertDialogBuilder alertDialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        //AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Login or Register");
        //alertDialog.setMessage("Login to post your task or make offers to posted tasks.");
        alertDialog.setMessage(noticeMsg);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //session.logoutUser();
                Intent i = new Intent(IssuesActivity.this, MainActivity.class);
                // Closing all the Activities
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                // Add new Flag to start new Activity
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Starting Login Activity
                startActivity(i);
                finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //HomeActivity.this.finish();
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    /*private void showBar() {
        progressBar.setVisibility(View.VISIBLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        //      WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideBar() {
        progressBar.setVisibility(View.INVISIBLE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }*/
}
