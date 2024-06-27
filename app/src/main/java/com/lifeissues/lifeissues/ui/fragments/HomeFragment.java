package com.lifeissues.lifeissues.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.helpers.WorkRequest;
import com.lifeissues.lifeissues.models.BibleVerseResult;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.ui.activities.MainActivity;
import com.lifeissues.lifeissues.ui.adapters.HomeIssuesGridAdapter;
import com.lifeissues.lifeissues.ui.adapters.HomePrayerAdapter;
import com.lifeissues.lifeissues.ui.adapters.HomeTestimonyAdapter;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;
import com.lifeissues.lifeissues.ui.viewmodels.MainActivityViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by BE on 2/12/2018.
 */

public class HomeFragment extends Fragment implements View.OnClickListener,
        HomeIssuesGridAdapter.HomeIssuesListOnItemClickHandler, HomeTestimonyAdapter.TestimonyListAdapterListener,
        HomePrayerAdapter.PrayerListAdapterListener {
    private static final String TAG = HomeFragment.class.getSimpleName();

    public static final String USERNAME = "user_name";

    private String mFragmentName;
    private OnIssueListClickListener onIssueListClickListener;
    private OnViewAllIssuesClickListener onViewAllIssuesClickListener;
    private OnViewAllPrayerClickListener onViewAllPrayerClickListener;
    private OnViewAllTestimoniesClickListener onViewAllTestimoniesClickListener;
    private OnTestimonyListClickListener onTestimonyListClickListener;
    private OnPrayerListClickListener onPrayerListClickListener;
    private List<Testimony> testimoniesList = new ArrayList<>();
    private List<PrayerRequest> prayerRequestList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RelativeLayout relativeLayout;
    private HomeIssuesGridAdapter issuesGridAdapter;
    private BibleVersesActivityViewModel viewModel;
    private List<Issue> issuesList;
    private List<Testimony> testimonyList = new ArrayList<Testimony>();
    private List<BibleVerseResult> verseList;
    private HomeTestimonyAdapter testimonyAdapter;
    private RecyclerView testimoniesRecyclerView, prayerRecyclerView, issuesRv;
    private MainActivityViewModel mainActivityViewModel;
    private HomePrayerAdapter homePrayerAdapter;
    private SessionManager session;
    private SharedPreferences prefs;
    private ImageView notFav, inFav,shareIcon;
    private TextView welcomeMsg, verse, verse_content,issue, todayDate, viewAllIssues;
    private Random rand;
    private int max, min = 1, randomPhraseID;
    private MediaPlayer audioplayer = null;
    private String dateToday;
    private String app_link = "https://play.google.com/store/apps/details?id=com.lifeissues.lifeissues";

    public HomeFragment(){

    }

    public static HomeFragment newInstance() {

//        Bundle arguments = new Bundle();
//        arguments.putString(USERNAME, userName);
        HomeFragment fragment = new HomeFragment();
        //fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //name = getArguments().getString(USERNAME);
        }

        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) MainActivity.getInstance()).getSupportActionBar().setTitle("Life Issues");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //mCategoriesViewModel.start();
        try {
            //set the name of this fragment in the toolbar
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Life Issues");
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // session manager
        session = new SessionManager(MainActivity.getInstance());

        View rootView = inflater.inflate(R.layout.home_fragment,container,false);

        getAllWidgets(rootView);

        /*
        Use ViewModelProviders to associate your ViewModel with your UI controller.
        When your app first starts, the ViewModelProviders will create the ViewModel.
        When the activity is destroyed, for example through a configuration change,
        the ViewModel persists. When the activity is re-created, the ViewModelProviders
        return the existing ViewModel
         */
        viewModel = new ViewModelProvider(getActivity()).get(BibleVersesActivityViewModel.class);

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateToday = df.format(c.getTime());

        //display the daily phrase
        getVerseOfTheDay(dateToday, false);

        //adding one day
        c.add(Calendar.DAY_OF_YEAR, 1);

        //get tomorrow's phrase and set a notification
        //for it
        String dateTomorrow = df.format(c.getTime());
        getVerseOfTheDay(dateTomorrow, true);
        //Log.i(TAG,"Tomorrow time "+dateTomorrow);

        if (session.isLoggedIn()) {
            //async to do stuff in background -  get user name if logged in
            welcomeMsg.setText(getString(R.string.welcome, session.getUserFirstName()));
        }else{
            welcomeMsg.setText(getString(R.string.welcome, ""));
        }

        setupIssuesHorizontalAdapter(rootView);
        setTestimonyAdapter(rootView);
        setPrayerAdapter(rootView);
        //setEventsAdapter(rootView);

        viewModel.getIssues().observe(getActivity(),
                new Observer<List<Issue>>() {
                    @Override
                    public void onChanged(@Nullable final List<Issue> issues) {
                        // Update the cached copy of the categories in the adapter.
                        issuesList = issues;
                        issuesGridAdapter.setList(issues);
                    }
                });

        fetchTestimonyList();
        fetchPrayerList();

        return  rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            onTestimonyListClickListener = (OnTestimonyListClickListener) context;
            onPrayerListClickListener = (OnPrayerListClickListener) context;
            onIssueListClickListener = (OnIssueListClickListener) context;
            onViewAllIssuesClickListener = (OnViewAllIssuesClickListener) context;
            onViewAllPrayerClickListener = (OnViewAllPrayerClickListener) context;
            onViewAllTestimoniesClickListener = (OnViewAllTestimoniesClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnTestimonyListClickListener, " +
                    "OnPrayerListClickListener, OnCategoryRowListClickListener, onViewAllIssuesClickListener");
        }
    }

    private void getAllWidgets(View view){
        //prayerRecyclerView = view.findViewById(R.id.home_prayer_rv);
        //testimoniesRecyclerView = view.findViewById(R.id.home_testimonies_rv);
        issuesRv = view.findViewById(R.id.home_issues_rv);
        welcomeMsg = view.findViewById(R.id.welcome_msg);
        relativeLayout = view.findViewById(R.id.daily_verse_rv);

        viewAllIssues = view.findViewById(R.id.home_issues_view_all_title_tv);
        viewAllIssues.setOnClickListener(this);

        TextView viewAllPrayer = view.findViewById(R.id.home_prayer_view_all_tv);
        viewAllPrayer.setOnClickListener(this);

        TextView viewAllTestimonies = view.findViewById(R.id.home_testimony_view_all_tv);
        viewAllTestimonies.setOnClickListener(this);

        verse = (TextView) view.findViewById(R.id.verse);
        //shareImageButton.setClickable(false);
        //saveImageButton.setClickable(false);

        verse_content = (TextView) view.findViewById(R.id.verse_content);
        issue = (TextView) view.findViewById(R.id.issue);
        todayDate = (TextView) view.findViewById(R.id.date);
        notFav = (ImageView) view.findViewById(R.id.notFav_icon);
        inFav = (ImageView) view.findViewById(R.id.yesFav_icon);
        //share icon
        shareIcon = (ImageView) view.findViewById(R.id.share_icon);
    }

    //categories horizontal scroll
    private void setupIssuesHorizontalAdapter(View view){
        //gridView = view.findViewById(R.id.gridview);
        mRecyclerView = view.findViewById(R.id.home_issues_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        issuesGridAdapter = new HomeIssuesGridAdapter(issuesList, getActivity(),this);

        mRecyclerView.setAdapter(issuesGridAdapter);
    }

    @Override
    public void onHomeIssuesItemClick(int position){
        Issue issue = issuesList.get(position);
        onIssueListClickListener.issueListClick(issue.getIssue_id(),issue.getIssueName(), issue.getIs_favorite());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.home_issues_view_all_title_tv) {
            onViewAllIssuesClickListener.allIssuesListClick();
        }else if(id == R.id.home_testimony_view_all_tv){
            onViewAllTestimoniesClickListener.viewAllTestimoniesClick();
        } else if (id == R.id.home_prayer_view_all_tv) {
            onViewAllPrayerClickListener.viewAllPrayerClick();
        }
    }

    public interface OnIssueListClickListener {
        void issueListClick(int issueId, String issueName, int isFavorite);
    }

    public interface OnViewAllIssuesClickListener {
        void allIssuesListClick();
    }

    public interface OnViewAllPrayerClickListener {
        void viewAllPrayerClick();
    }

    public interface OnViewAllTestimoniesClickListener {
        void viewAllTestimoniesClick();
    }

    private void fetchTestimonyList(){
        mainActivityViewModel.getHomeTestimonies().observe(getActivity(), testimonies -> {
            testimoniesList = testimonyList = testimonies;
            testimonyAdapter.submitList(testimonies);
        });
    }

    private void setTestimonyAdapter(View view)
    {
        testimoniesRecyclerView = view.findViewById(R.id.home_testimonies_rv);
        testimonyAdapter = new HomeTestimonyAdapter(getActivity(),this);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(),
                        LinearLayoutManager.HORIZONTAL, false);
        testimoniesRecyclerView.setLayoutManager(mLayoutManager);
        testimoniesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        testimoniesRecyclerView.setAdapter(testimonyAdapter);
    }

    @Override
    public void onTestimonyRowClicked(int position) {
        Testimony testimony = testimoniesList.get(position);
        //issue.setRead(true);
        //master detail flow callback
        onTestimonyListClickListener.onTestimonyListItemClick(position,testimony.getTestimony_id(),
                testimony.getTestimony_name().toLowerCase(Locale.US),
                testimony.getContent(),
                testimony.getIs_liked(),testimony.getPosted_on(),
                testimony.getIs_reported(),testimony.getLikes_number(),
                testimony.getUser_name(),testimony.getProfile_pic(),
                testimony.getPosted_by());
    }

    public interface OnTestimonyListClickListener {
        void onTestimonyListItemClick(int position, int testimonyID, String title,
                                      String description,int isLiked,
                                      String datePosted,int isReported,
                                      int likesNumber, String posterName,
                                      String profilePic, int posterId);
    }

    private void fetchPrayerList(){
        mainActivityViewModel.getHomePrayer().observe(getActivity(), prayerList -> {
            prayerRequestList = prayerList;
            homePrayerAdapter.submitList(prayerList);
        });
    }

    private void setPrayerAdapter(View view)
    {
        prayerRecyclerView = view.findViewById(R.id.home_prayer_rv);
        homePrayerAdapter = new HomePrayerAdapter(getActivity(),this);

        LinearLayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        prayerRecyclerView.setLayoutManager(mLayoutManager);
        prayerRecyclerView.setItemAnimator(new DefaultItemAnimator());
        prayerRecyclerView.setAdapter(homePrayerAdapter);
    }

    @Override
    public void onPrayerRowClicked(int position) {
        PrayerRequest prayerRequest = prayerRequestList.get(position);
        //issue.setRead(true);
        onPrayerListClickListener.onPrayerListItemClick(position,prayerRequest.getPrayer_id(),
                prayerRequest.getPrayer_title().toLowerCase(Locale.US),
                prayerRequest.getDescription(),
                prayerRequest.getIs_prayed_for(),prayerRequest.getPosted_on(),
                prayerRequest.getIs_reported(),prayerRequest.getPrayersReceived(),
                prayerRequest.getUser_name(),prayerRequest.getProfile_pic(),
                prayerRequest.getPosted_by());
    }

    public interface OnPrayerListClickListener {
        void onPrayerListItemClick(int position, int prayerID, String title,
                                   String description,int isPrayedFor,
                                   String datePosted,int isReported,
                                   int prayersReceivedNumber, String posterName,
                                   String profilePic, int posterId);
    }

    //get a random phrase id for the verse of the day
    private void getVerseOfTheDay(String date, boolean toNotify) {
        try {
            viewModel.getDailyVerse(date).observe(getActivity(),
                    new Observer<List<BibleVerseResult>>() {
                        @Override
                        public void onChanged(@Nullable final List<BibleVerseResult> verse) {
                            // Update the cached copy of the categories in the adapter.
                            verseList = verse;

                            if (verseList != null && !verseList.isEmpty()){

                                /*String imageName = verseList.get(0).getName().toLowerCase(Locale.US);
                                int imageId = getResources().getIdentifier(imageName,
                                        "drawable", AppController.getContext().getPackageName());
                                relativeLayout.setBackgroundResource(imageId);*/

                                int vId = verseList.get(0).get_id();

                                if (toNotify){
                                    //set up notification
                                    setUpNotification(vId,verseList.get(0).getVerse(),
                                            verseList.get(0).getMsg(), verseList.get(0).getAmp(),
                                            verseList.get(0).getKjv(),verseList.get(0).getName(),
                                            verseList.get(0).getIs_favorite());
                                }else{
                                    //display the daily phrase
                                    displayDailyPhrase(vId,verseList.get(0).getVerse(),
                                            verseList.get(0).getMsg(), verseList.get(0).getAmp(),
                                            verseList.get(0).getKjv(),verseList.get(0).getName(),
                                            verseList.get(0).getIs_favorite(),verseList.get(0).getIssue_id());
                                }

                            }

                        }
                    });
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }

    //display the daily phrase
    private void displayDailyPhrase(int vId, String bibleVerse, String msgVerseContent,
                                    String ampVerseContent, String kjvVerseContent,
                                    String issueName, int isFav, int issueId){
        issue.setText(issueName);
        todayDate.setText(dateToday);
        String defaultVersionKey = "key_daily_verse_version";
        String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
        switch (defaultVersion){
            case "msg":
                verse.setText(bibleVerse);
                verse.append(" (MSG)");
                verse_content.setText(msgVerseContent);
                handleShareIcon(bibleVerse,msgVerseContent);
                break;
            case "amp":
                verse.setText(bibleVerse);
                verse.append(" (AMP)");
                verse_content.setText(ampVerseContent);
                handleShareIcon(bibleVerse,ampVerseContent);
                break;
            case "kjv":
            default:
                verse.setText(bibleVerse);
                verse.append(" (KJV)");
                verse_content.setText(kjvVerseContent);
                handleShareIcon(bibleVerse,ampVerseContent);
        }

        updateStar(isFav);

        handleFavouriteStar(vId, issueId);
    }

    //method to handle clicks on the share icon
    private void handleShareIcon(final String bibleVerse, final String content){
        shareIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent sharingIntent = new Intent();
                sharingIntent.setAction(Intent.ACTION_SEND);
                String shareBody = "\n" + bibleVerse +
                        "\n" + content +
                        "\n"+app_link;
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                sharingIntent.setType("text/plain");
                startActivity(Intent.createChooser(sharingIntent, "Share verse"));

            }
        });
    }

    private void setUpNotification(int vId, String bibleVerse, String msgVerseContent,
                                   String ampVerseContent, String kjvVerseContent,
                                   String issueName, int isFav){
        String notificationEnabledKey = "notifications_new_message";
        boolean isNotify = prefs.getBoolean(notificationEnabledKey, false);
        if (isNotify){
            //Log.e(TAG, "Notify flag is set");

            String verseTimeKey = "key_daily_verse_time";
            String defaultTime = prefs.getString(verseTimeKey, "06:00");
            //Log.e(TAG, "verse defaultTime saved = "+verseTimeKey);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm", Locale.getDefault());
            SimpleDateFormat hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
            SimpleDateFormat minFormat = new SimpleDateFormat("mm", Locale.getDefault());
            String currentDate = dateFormat.format(c.getTime());

            String reminderDateTime;

            reminderDateTime = currentDate + " - " + defaultTime;
            //Log.e(TAG, "Reminder Time saved = "+reminderDateTime);
            //notify user
            Date reminderTime = null;
            try {
                reminderTime = dateTimeFormat.parse(reminderDateTime);
                assert reminderTime != null;
                c.setTime(reminderTime);
            } catch (ParseException e) {
                e.getMessage();
                e.printStackTrace();
            }

            if (c.before(Calendar.getInstance())) {
                //Log.e(TAG,"Time Updated");
                c.add(Calendar.DAY_OF_YEAR, 1);
            }

            //get the default verse version
            String defaultVersionKey = "key_daily_verse_time";
            String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
            switch (defaultVersion){
                case "msg":
                    new WorkRequest().setPhraseNotification(vId,bibleVerse, msgVerseContent, "(MSG)", c);
                    break;
                case "amp":
                    new WorkRequest().setPhraseNotification(vId,bibleVerse, ampVerseContent, "(AMP)", c);
                    break;
                case "kjv":
                default:
                    new WorkRequest().setPhraseNotification(vId,bibleVerse, kjvVerseContent, "(KJV)", c);
            }

        }else{
            WorkManager.getInstance(getActivity()).cancelAllWorkByTag("notificationWork");
        }
    }

    private void handleFavouriteStar(int vID, int issueId){

        notFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewModel.addFavorite(vID,issueId);
                //phraseListViewModel.addFavouritePhrase(pId);
                inFav.setVisibility(View.VISIBLE);
                notFav.setVisibility(View.INVISIBLE);
            }
        });

        inFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //phraseListViewModel.removeFavouritePhrase(pId);
                viewModel.deleteFavourite(vID,issueId);
                inFav.setVisibility(View.INVISIBLE);
                notFav.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateStar(int value){
        if(value == 1){
            inFav.setVisibility(View.VISIBLE);
            notFav.setVisibility(View.INVISIBLE);
        } else if (value == 0) {
            inFav.setVisibility(View.INVISIBLE);
            notFav.setVisibility(View.VISIBLE);
        }
    }

    //method to check for internet connection
    private static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }
}
