package com.lifeissues.lifeissues.ui.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.activities.MainActivity;
import com.lifeissues.lifeissues.ui.activities.NoteActivity;
import com.lifeissues.lifeissues.helpers.ReminderManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

/**
 * Created by Emo on 8/31/2017.
 */

public class TodayVerseFragment extends Fragment {
    private static final String TAG = TodayVerseFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 55;
    private View rootView;
    private BibleVersesActivityViewModel viewModel;
    private SharedPreferences prefs;
    private Random rand;
    private Cursor cursor,c1,c2,c3,c4, c2date;
    private int max,randomVerseID,randomVerseID2, min=1, verseID,issueID;
    private String versionCheck, bibleVerse;
    private MaterialButton shareImageButton, saveImageButton;
    private boolean notificationSwitch = false;
    private CardView verseImageCard;
    private ImageView notFav, inFav,addNoteIcon, shareIcon, verseImage;
    private TextView verse, verse_content,issue, todayDate;
    private Bitmap bitmap;
    private Boolean isVerseAdded;

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
        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);

        verse = (TextView) rootView.findViewById(R.id.verse);
        saveImageButton = rootView.findViewById(R.id.todaySaveImageButton);
        shareImageButton = rootView.findViewById(R.id.todayShareImageButton);
        verseImage = rootView.findViewById(R.id.todayVerseImageView);
        shareImageButton.setClickable(false);
        saveImageButton.setClickable(false);

        verseImageCard = rootView.findViewById(R.id.verse_image_card_view);
        verse_content = (TextView) rootView.findViewById(R.id.verse_content);
        issue = (TextView) rootView.findViewById(R.id.issue);
        todayDate = (TextView) rootView.findViewById(R.id.date);
        notFav = (ImageView) rootView.findViewById(R.id.notFav_icon);
        inFav = (ImageView) rootView.findViewById(R.id.yesFav_icon);
        addNoteIcon = (ImageView) rootView.findViewById(R.id.note_icon);
        //share icon
        shareIcon = (ImageView) rootView.findViewById(R.id.share_icon);

        new checkDailyVerse().execute();

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


    private void handleFavouriteStar(final int vID, int iID){
        notFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.VISIBLE);
                notFav.setVisibility(View.INVISIBLE);
                viewModel.setFavorite(vID);
                viewModel.addFavorite(vID,iID);
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

                viewModel.deleteFavourite(vID,iID);
                viewModel.removeFavourite(vID);
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
            max = viewModel.getTotNumberOfVerses();
            Log.e(TAG,"tot bible verses = "+ max);
            rand = new Random();//create random generator object
            randomVerseID = (int) (Math.random() * (max - min + 1) + min);
            //randomVerseID = rand.nextInt((max - min) + 1) + min;//get a random  verse id
            c1 = viewModel.getRandomVerse();//get the content of that verse id
            Log.e(TAG,"c1 count = "+ c1.getCount());
            Log.e(TAG,"Random verse id 1 = "+ randomVerseID);

            //create another random object to get a verse for tomorrow
            /*rand = new Random();//create random generator object
            randomVerseID2 = rand.nextInt(max);//get a random  verse id
            //randomVerseID2 = (int) (Math.random() * (max - min + 1) + min);
            if (randomVerseID2 == 0)
                randomVerseID2 = randomVerseID2 + 1;
            //c2date = dbhelper.getRandomVerse(randomVerseID2);
            c2date = viewModel.getRandomVerse();//get the content of that verse id
            Log.e(TAG,"Random verse id 2 = "+ randomVerseID2);
            Log.e(TAG,"c2 count = "+ c2date.getCount());*/
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (c1.moveToFirst())
                saveDailyVerse();
        }
    }

    private void inflateViews(Cursor c, String version){
        if (c != null){
            c.moveToFirst();
            String issueName = c.getString(c.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME_ID));
            verseID = c.getInt(c.getColumnIndex(DatabaseTable.KEY_ID));
            issueID = c.getInt(c.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            bibleVerse = c.getString(c.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = c.getString(c.getColumnIndex(DatabaseTable.KEY_AMP));
            int favValue = c.getInt(c.getColumnIndex(DatabaseTable.KEY_FAVOURITE));
            String dateToday = c.getString(c.getColumnIndex(DatabaseTable.KEY_DATE_TAKEN));

            Log.e(TAG, "Today Bible verse = "+bibleVerse);

            updateStar(favValue);
            handleFavouriteStar(verseID,issueID);

            issue.setText("Life Issue: " + issueName.substring(0, 1).toUpperCase() + issueName.substring(1));
            todayDate.setText(dateToday);
            switch (version){
                case "msg":
                    verse.setText(bibleVerse);
                    verse.append(" (MSG)");
                    verse_content.setText(msgVerseContent);
                    handleNoteIcon(bibleVerse,issueName,msgVerseContent);
                    handleShareIcon(bibleVerse,msgVerseContent);
                    dailyVerseNotification(verseID,bibleVerse,msgVerseContent);
                    break;
                case "amp":
                    verse.setText(bibleVerse);
                    verse.append(" (AMP)");
                    verse_content.setText(ampVerseContent);
                    handleNoteIcon(bibleVerse,issueName,ampVerseContent);
                    handleShareIcon(bibleVerse,ampVerseContent);
                    dailyVerseNotification(verseID,bibleVerse,ampVerseContent);
                    break;
                case "kjv":
                default:
                    verse.setText(bibleVerse);
                    verse.append(" (KJV)");
                    verse_content.setText(kjvVerseContent);
                    handleNoteIcon(bibleVerse,issueName,kjvVerseContent);
                    handleShareIcon(bibleVerse,ampVerseContent);
                    dailyVerseNotification(verseID,bibleVerse,kjvVerseContent);
            }

            //if internet is available, load and display image
            if (isNetworkAvailable(getActivity())){
                //check for the verse image and display it
                new checkForImageTask().execute(verseID);
                //save image to phone storage
                saveVerseImage(verseID);

                //share the verse image
                shareVerseImage();
            }

        }
    }

    //save the daily verse generated for today and the one for tomorrow
    private void saveDailyVerse(){
        // && c2date != null
        if (c1 != null){
            //c1.moveToFirst();
            String issueName = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
            int issueID = c1.getInt(c1.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            int verseID = c1.getInt(c1.getColumnIndex(DatabaseTable.KEY_ID));
            String bibleVerse = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = c1.getString(c1.getColumnIndex(DatabaseTable.KEY_AMP));
            int favValue = c1.getInt(c1.getColumnIndex(DatabaseTable.KEY_IS_FAVORITE));

            Calendar c = Calendar.getInstance();
            System.out.println("Current time => " + c.getTime());
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());
            //today's notification
            dailyVerseNotification(verseID,bibleVerse,kjvVerseContent);

            /*if (dbhelper.addDailyVerse(verseID,bibleVerse,kjvVerseContent,msgVerseContent,ampVerseContent,
                    favValue,issueName,issueID,dateToday)){
                new getDailyVerse().execute();
            }*/
            new addDailyVerseAsyncTask(verseID,bibleVerse,kjvVerseContent,msgVerseContent,ampVerseContent,
                    favValue,issueName,issueID,dateToday).execute();
            /*if (viewModel.addDailyVerse(verseID,bibleVerse,kjvVerseContent,msgVerseContent,ampVerseContent,
                    favValue,issueName,issueID,dateToday)){
                new getDailyVerse().execute();
            }*/

            //handle cursor for tomorrows date
            //c2date.moveToFirst();
            /*String issueName2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
            int verseID2 = c2date.getInt(c2date.getColumnIndex(DatabaseTable.KEY_ID));
            int issueID2 = c1.getInt(c1.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            String bibleVerse2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_AMP));
            String favValue2 = c2date.getString(c2date.getColumnIndex(DatabaseTable.KEY_IS_FAVORITE));

            //getting tomorrows's date to store notification for tomorrow
            c.add(Calendar.DATE, 1); //add 1 to go to the next date
            String dateTomorrow = df.format(c.getTime());*/

            //dailyVerseNotification(verseID2,bibleVerse2,kjvVerseContent2);

            //new addTomorrowVerseAsyncTask(verseID2,bibleVerse2,kjvVerseContent2,msgVerseContent2,ampVerseContent2,
              //      favValue2,issueName2,issueID2,dateTomorrow).execute();

            /*if (viewModel.addDailyVerse(verseID2,bibleVerse2,kjvVerseContent2,msgVerseContent2,ampVerseContent2,
                    favValue2,issueName2,issueID2,dateTomorrow)){
                Log.d(TAG, "Tomorrow's daily verse saved");
            }*/
        }else{
            Log.e(TAG,"Cursors are null");
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

    //async task to add daily verse to daily verse table
    private class addDailyVerseAsyncTask extends AsyncTask<Void, Void, Void> {

        private int mVerseId, mIssueId, mFavValue;
        private String mBibleVerse,mKjv, mMsg, mAmp, mIssueName, mDate;
        addDailyVerseAsyncTask(int verseID, String bibleVerse, String kjvVerseContent,
                               String msgVerseContent, String ampVerseContent, int favValue,
                               String issueName, int issueID, String dateToday){
            mVerseId = verseID;
            mIssueId = issueID;
            mBibleVerse = bibleVerse;
            mKjv = kjvVerseContent;
            mMsg = msgVerseContent;
            mAmp = ampVerseContent;
            mFavValue = favValue;
            mIssueName = issueName;
            mDate = dateToday;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            isVerseAdded = viewModel.addDailyVerse(mVerseId,mBibleVerse,mKjv,mMsg,mAmp,
                    mFavValue,mIssueName,mIssueId,mDate);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isVerseAdded){
                Log.e(TAG,"daily verse added");
                new getDailyVerse().execute();
            }else{
                Log.e(TAG,"verse not added");
            }

        }
    }

    //async task to add daily verse to daily verse table
    private class addTomorrowVerseAsyncTask extends AsyncTask<Void, Void, Void> {

        private int mVerseId, mIssueId, mFavValue;
        private String mBibleVerse,mKjv, mMsg, mAmp, mIssueName, mDate;
        addTomorrowVerseAsyncTask(int verseID, String bibleVerse, String kjvVerseContent,
                               String msgVerseContent, String ampVerseContent, int favValue,
                               String issueName, int issueID, String dateTomorrow){
            mVerseId = verseID;
            mIssueId = issueID;
            mBibleVerse = bibleVerse;
            mKjv = kjvVerseContent;
            mMsg = msgVerseContent;
            mAmp = ampVerseContent;
            mFavValue = favValue;
            mIssueName = issueName;
            mDate = dateTomorrow;
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            isVerseAdded = viewModel.addDailyVerse(mVerseId,mBibleVerse,mKjv,mMsg,mAmp,
                    mFavValue,mIssueName,mIssueId,mDate);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isVerseAdded){
                Log.e(TAG,"2moro verse added");
            }

        }
    }

    //async task to get daily verse from daily verse table
    private class getDailyVerse extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateToday = df.format(c.getTime());

            c4 = viewModel.getDailyVerse(dateToday);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.e(TAG,"Daily verse cursor = "+c4.getCount());
            String defaultVersionKey = getString(R.string.key_daily_verse_version);
            String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
            if (!defaultVersion.equals("kjv")) {
                Log.e(TAG,"Using default version");
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

            //c2 = dbhelper.checkDailyVerse(dateToday);
            c2 = viewModel.checkDailyVerse(dateToday);
            Log.e(TAG,"check daily verse cursor = "+ c2.getCount());

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

    private void updateStar(int value){
        if(value == 1){
            inFav.setVisibility(View.VISIBLE);
            notFav.setVisibility(View.INVISIBLE);
        } else if (value == 0) {
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

    //downloads the verse image to the user's phone storage
    private void saveVerseImage(int verseId){
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkPermission()) {
                            //get today's date to be added to name of the image
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                            String formattedDate = dateFormat.format(c.getTime());

                            BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                            bitmap = bitmapDrawable .getBitmap();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                ContentResolver resolver = getActivity().getContentResolver();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Life_Issues_Images");
                                contentValues.put(MediaStore.Images.Media.IS_PENDING, true);
                                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                                try {
                                    OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    if (fos != null) {
                                        fos.close();
                                    }
                                    contentValues.put(MediaStore.Images.Media.IS_PENDING, false);
                                    resolver.update(imageUri, contentValues, null, null);
                                    Log.d(TAG, "File saved");
                                    Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else{
                                //String path = getActivity().getExternalFilesDir(null).toString();
                                //File myDir = getActivity().getExternalFilesDir(
                                  //    Environment.DIRECTORY_PICTURES);
                                File myDir = new File(Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");
                                if (!myDir.mkdirs()) {
                                    myDir.mkdirs();
                                }
                                File file = new File(myDir, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                                if (!file.exists()) {
                                    Log.d("path", file.toString());
                                    try {
                                        FileOutputStream fos = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                        fos.flush();
                                        fos.close();
                                        Log.d(TAG, "File saved");
                                        Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            requestPermission();
                            Log.e(TAG, "Request for permission");
                        }
                    }else{
                        //get today's date to be added to name of the image
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        String formattedDate = dateFormat.format(c.getTime());

                        File myDir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");

                        //File myDir = getActivity().getExternalFilesDir(
                        //      Environment.DIRECTORY_PICTURES);
                        //File myDir = new File(path + "/Life_Issues_Images");
                        //myDir.mkdirs();
                        if (!myDir.mkdirs()) {
                            //myDir.mkdirs();
                            Log.e(TAG, "Life_Issues_Images directory not created");
                        }
                        File file = new File(myDir, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                        if (!file.exists()) {
                            Log.d("path", file.toString());
                            try {
                                FileOutputStream fos = new FileOutputStream(file);
                                BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                                Bitmap bitmap = bitmapDrawable .getBitmap();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();
                                fos.close();
                                Log.d(TAG, "File saved");
                                Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    //share the image with other apps
    private void shareVerseImage(){
        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                    Bitmap bitmap = bitmapDrawable .getBitmap();
                    String bitmapPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap,bibleVerse, null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    startActivity(Intent.createChooser(shareIntent,"Share Verse Image"));
                }catch (Exception e){
                    Log.e(TAG, "ERROR sharing verse image: "+e.getMessage());
                }
            }
        });
        //verseImageDialog.show();
    }

    private boolean checkPermission() {
        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            int permisionWriteExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permisionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            //if the permissions list is not empty, then request for the permission
            if (!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray
                        (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission Request")
                    .setMessage("Permission is required for the Life Issues app to read and write to storage")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },
                                    PERMISSION_REQUEST_CODE);
                        }
                    })
                    .show();
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    //async task checks if the image exists
    //if available, load it
    private class checkForImageTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con =  (HttpURLConnection)
                        new URL("https://www.emtechint.com/lifeissues/verse_images/" + params[0] +".png").openConnection();
                con.setRequestMethod("HEAD");
                System.out.println(con.getResponseCode());
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            if (bResponse)
            {
                loadVerseImage(verseID);
            }
            else
            {
                Log.e(TAG, "Image does NOT exist");
            }
        }
    }

    //load verse image
    private void loadVerseImage(int verseID){
        //load the verse image from the network
        try {
            Glide.with(this)
                    .load("https://www.emtechint.com/lifeissues/verse_images/" + verseID +".png")
                    //.thumbnail(0.5f)
                    //.transition(withCrossFade())
                    //.apply(new RequestOptions().fitCenter())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            verseImageCard.setVisibility(View.INVISIBLE);
                            shareImageButton.setClickable(false);
                            saveImageButton.setClickable(false);
                            //Toast.makeText(getActivity(),"Could not load image",Toast.LENGTH_LONG).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            verseImageCard.setVisibility(View.VISIBLE);
                            shareImageButton.setClickable(true);
                            saveImageButton.setClickable(true);
                            return false;
                        }

                    })
                    .into(verseImage);
        }catch (Exception e){
            Log.e(TAG, "ERROR loading verse image: "+e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        Log.d(TAG, "Permission callback called ----");
        //fill with actual results from the user
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            int currentAPIVersion = Build.VERSION.SDK_INT;
            Map<String, Integer> perms = new HashMap<>();
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                //initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            }
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                //check for both permissions
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write and Read external storage permissions granted");
                    //selectJobImage(currentJobImage);
                } else {
                    Log.d(TAG, "Some permissions are not granted, ask again");
                    //permission is denied (this is the first time, when "never ask again" is not checked)
                    //so ask again explaining the use of the permissions
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission Request")
                                .setMessage("Permission is required for the app to write and read from storage")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_ID_MULTIPLE_PERMISSIONS);
                                    }
                                })
                                .show();
                    }
                    //permission is denied and never ask again is checked
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        Toast.makeText(getActivity(), "Go to settings and enable permissions",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }

    //method to check for internet connection
    public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
