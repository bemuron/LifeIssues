package com.lifeissues.lifeissues.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import androidx.core.app.Fragment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.MainActivity;
import com.lifeissues.lifeissues.activities.NoteActivity;
import com.lifeissues.lifeissues.helpers.ReminderManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import database.DatabaseTable;

/**
 * Created by Emo on 8/31/2017.
 */

public class TodayVerseFragment extends Fragment {
    private static final String TAG = TodayVerseFragment.class.getSimpleName();
    private View rootView;
    private DatabaseTable dbhelper;
    private SharedPreferences prefs;
    private Random rand;
    private Cursor cursor,c1,c2,c3,c4, c2date;
    private int max,randomVerseID,randomVerseID2, min=1;
    private String versionCheck;
    private boolean notificationSwitch = false;
    private ImageView notFav, inFav,addNoteIcon, shareIcon;
    private TextView verse, verse_content,issue, todayDate;

    public TodayVerseFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static TodayVerseFragment newInstance() {
        TodayVerseFragment fragment = new TodayVerseFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_today_verse, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        dbhelper = new DatabaseTable(getActivity());

        verse = (TextView) rootView.findViewById(R.id.verse);
        verse_content = (TextView) rootView.findViewById(R.id.verse_content);
        issue = (TextView) rootView.findViewById(R.id.issue);
        todayDate = (TextView) rootView.findViewById(R.id.date);
        notFav = (ImageView) rootView.findViewById(R.id.notFav_icon);
        inFav = (ImageView) rootView.findViewById(R.id.yesFav_icon);
        addNoteIcon = (ImageView) rootView.findViewById(R.id.note_icon);
        //share icon
        shareIcon = (ImageView) rootView.findViewById(R.id.share_icon);

        new checkDailyVerse().execute();
        //displayDailyVerse();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        String defaultSwitchValue = getString(R.string.notifications_new_message);
        boolean switchValue = prefs.getBoolean(defaultSwitchValue, false);
        if (notificationSwitch != switchValue){
            //getActivity().recreate();
            //Toast.makeText(getActivity(), "switch = "+ switchValue, Toast.LENGTH_SHORT).show();
            //notificationSwitch = false;
        }
        String defaultVersionKey = getString(R.string.key_daily_verse_version);
        String defaultVersion = prefs.getString(defaultVersionKey, null);
        if (defaultVersion != null & versionCheck != null) {
            if (!versionCheck.equals(defaultVersion)) {
                getActivity().recreate();
            }
        }


    }


    private void handleFavouriteStar(final int vID){
        notFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.VISIBLE);
                notFav.setVisibility(View.INVISIBLE);
                dbhelper.addFavourite(vID);
                //adapter.notifyDataSetChanged();
                // new checkFavourite().execute();
                //favouriteValue = cursor.getString(6);
                //updateStar(favouriteValue);
                Toast.makeText(getActivity(), "Added to favs",
                        Toast.LENGTH_SHORT).show();
                //c1.close();
            }
        });

        inFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.INVISIBLE);
                notFav.setVisibility(View.VISIBLE);

                dbhelper.deleteFavourite(vID);
                //cursor.requery();
                //updateStar(favouriteValue);

                Toast.makeText(getActivity(), "Deleted from favs",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    //async task to get random verse from db
    private class getRandomVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

                cursor = dbhelper.getAllBibleVerses();//get all verses in the db
                if (cursor != null) {
                    max = cursor.getCount();//count them
                    rand = new Random();//create random generator object
                    randomVerseID = rand.nextInt((max - min) + 1) + min;//get a random  verse id
                    c1 = dbhelper.getRandomVerse(randomVerseID);//get the content of that verse id

                    //create another random object to get a verse for tomorrow
                    rand = new Random();//create random generator object
                    randomVerseID2 = rand.nextInt((max - min) + 1) + min;//get a random  verse id
                    c2date = dbhelper.getRandomVerse(randomVerseID2);//get the content of that verse id
                }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
                saveDailyVerse();

        }
    }

    private void inflateViews(Cursor c, String version){
        if (c != null){
            c.moveToFirst();
            String issueName = c.getString(c.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME_ID));
            int verseID = c.getInt(c.getColumnIndex(DatabaseTable.KEY_ID));
            String bibleVerse = c.getString(c.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_AMP));
            String favValue = c.getString(c.getColumnIndex(DatabaseTable.KEY_FAVOURITE));
            String dateToday = c.getString(c.getColumnIndex(DatabaseTable.KEY_DATE_TAKEN));

            updateStar(favValue);
            handleFavouriteStar(verseID);

            issue.setText("Life Issue: " + issueName.substring(0, 1).toUpperCase() + issueName.substring(1));
            todayDate.setText(dateToday);
            switch (version){
                //dailyVerseNotification(verseID,bibleVerse,kjvVerseContent);
                case "msg":
                    verse.setText(bibleVerse);
                    verse.append(" (MSG)");
                    verse_content.setText(msgVerseContent);
                    handleNoteIcon(bibleVerse,issueName,msgVerseContent);
                    handleShareIcon(bibleVerse,msgVerseContent);
                    //dailyVerseNotification(verseID,bibleVerse,msgVerseContent);
                    break;
                case "amp":
                    verse.setText(bibleVerse);
                    verse.append(" (AMP)");
                    verse_content.setText(ampVerseContent);
                    handleNoteIcon(bibleVerse,issueName,ampVerseContent);
                    handleShareIcon(bibleVerse,ampVerseContent);
                    //dailyVerseNotification(verseID,bibleVerse,ampVerseContent);
                    break;
                case "kjv":
                default:
                    verse.setText(bibleVerse);
                    verse.append(" (KJV)");
                    verse_content.setText(kjvVerseContent);
                    handleNoteIcon(bibleVerse,issueName,kjvVerseContent);
                    handleShareIcon(bibleVerse,ampVerseContent);
                    //dailyVerseNotification(verseID,bibleVerse,kjvVerseContent);
            }

        }
    }

    //save the daily verse generated for today and the one for tomorrow
    private void saveDailyVerse(){
        if (c1 != null && c2date != null){
            c1.moveToFirst();
            String issueName = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            int verseID = c1.getInt(c1.getColumnIndex(DatabaseTable.KEY_ID));
            String bibleVerse = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_AMP));
            String favValue = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_FAVOURITE));

            Calendar c = Calendar.getInstance();
            System.out.println("Current time => " + c.getTime());
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());
            //today's notification
            //dailyVerseNotification(verseID,bibleVerse,kjvVerseContent);

            if (dbhelper.addDailyVerse(verseID,bibleVerse,kjvVerseContent,msgVerseContent,ampVerseContent,
                    favValue,issueName,dateToday)){
                new getDailyVerse().execute();
            }

            //handle cursor for tomorrows date
            c2date.moveToFirst();
            String issueName2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            int verseID2 = c2date.getInt(c2date.getColumnIndex(DatabaseTable.KEY_ID));
            String bibleVerse2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_AMP));
            String favValue2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_FAVOURITE));

            //getting tomorrows's date to store notification for tomorrow
            c.add(Calendar.DATE, 1); //add 1 to go to the next date
            String dateTomorrow = df.format(c.getTime());

            //dailyVerseNotification(verseID2,bibleVerse2,kjvVerseContent2);


            if (dbhelper.addDailyVerse(verseID2,bibleVerse2,kjvVerseContent2,msgVerseContent2,ampVerseContent2,
                    favValue2,issueName2,dateTomorrow)){
                Log.d(TAG, "Tomorrow's daily verse saved");
            }
        }
    }

    //method to notify user of daily verse
    private void dailyVerseNotification(int verseID, String verse, String content){
        String defaultSwitchValue = getString(R.string.notifications_new_message);
        boolean switchValue = prefs.getBoolean(defaultSwitchValue, false);

        //if notifications switch is on, notify user
        if (switchValue){
            notificationSwitch = switchValue;
            String defaultTimeKey = getString(R.string.key_daily_verse_time);
            String defaultTime = prefs.getString(defaultTimeKey, null);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm", Locale.getDefault());
            String currentDate = dateFormat.format(c.getTime());
            //MMM dd, yyyy - h:mm a
            String reminderDateTime;

            if (defaultTime != null) {
                reminderDateTime = currentDate + " - " + defaultTime;
                //notify user
                Date reminderTime = null;
                try {
                    reminderTime = dateTimeFormat.parse(reminderDateTime);
                    c.setTime(reminderTime);
                } catch (ParseException e) {
                    e.getMessage();
                    e.printStackTrace();
                }

                System.out.println("*******TIME = "+ c.getTime());
                System.out.println("*******Default Time = "+ defaultTime);
                //Toast.makeText(getActivity(), "rt="+ c.getTime(), Toast.LENGTH_LONG).show();
                new ReminderManager(getActivity()).setReminder(verseID, verse, content, c);
            }

        }else{
            //Toast.makeText(getActivity(), "Switch is off", Toast.LENGTH_LONG).show();
            Log.d(TAG+" :notifctn switchValue", "Switch is off");
        }
        //getActivity().recreate();
    }

    //async task to get daily verse from daily verse table
    private class getDailyVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());

                c4 = dbhelper.getDailyVerse(dateToday);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String defaultVersionKey = getString(R.string.key_daily_verse_version);
            String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
            if (!defaultVersion.equals("kjv")) {
                versionCheck = defaultVersion;
                //Toast.makeText(getActivity(), "version = "+ defaultVersion, Toast.LENGTH_LONG).show();
                inflateViews(c4, defaultVersion);
            }else{
                inflateViews(c4,"kjv");
            }

        }
    }

    //async task to check daily verse from daily verse table
    private class checkDailyVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());

            c2 = dbhelper.checkDailyVerse(dateToday);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

                //compare the date today with the date in the db to see if a daily verse has already
                //been generated for the day
                // if not yet, generate random verse for the day
                //save to db
                //display it
                //if already generated daily verse, then user is just changing the time
                if (c2.getCount() == 0){
                    //generate random verse
                    new getRandomVerse().execute();
                }else {
                    //get today's verse from db
                    new getDailyVerse().execute();
                }

            //}

        }
    }

    private void updateStar(String value){
        if(value.equals("yes")){
            inFav.setVisibility(View.VISIBLE);
            notFav.setVisibility(View.INVISIBLE);
        } else if (value.equals("no")) {
            inFav.setVisibility(View.INVISIBLE);
            notFav.setVisibility(View.VISIBLE);
        }
    }

    //method to handle clicks on the note icon
    private void handleNoteIcon(final String bibleVerse, final String issue, final String content){
        addNoteIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.getInstance(), NoteActivity.class);
                intent.putExtra("verse", bibleVerse);
                intent.putExtra("issueName", issue);
                intent.putExtra("content", content);
                startActivity(intent);

            }
        });
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
                        "\n Life Issues App.";
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                sharingIntent.setType("text/plain");
                startActivity(Intent.createChooser(sharingIntent, "Share verse"));

            }
        });
    }
}
