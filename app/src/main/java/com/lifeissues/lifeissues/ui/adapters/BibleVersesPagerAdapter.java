package com.lifeissues.lifeissues.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.ui.fragments.BibleVersesFragment;

import com.lifeissues.lifeissues.data.database.DatabaseTable;

/**
 * Created by Emo on 11/28/2017.
 */

public class BibleVersesPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = BibleVersesPagerAdapter.class.getSimpleName();
    private Cursor cursorData;
    private SharedPreferences prefs;
    private String bibleVersion;
    private int issueID;

    Context context = AppController.getContext();

    public BibleVersesPagerAdapter(FragmentManager fm, Cursor cursor,
                                   SharedPreferences sharedPreferences, String version) {
        super(fm);
        this.cursorData = cursor;
        //this.issueID = issueID;
        this.prefs = sharedPreferences;
        this.bibleVersion = version;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        BibleVersesFragment bibleVersesFragment = new BibleVersesFragment();
        Bundle data = new Bundle();
        //check if user set default bible verse version
        String defaultVersionKey = context.getString(R.string.key_daily_verse_version);
        String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
        Log.e(TAG,"Cursor data in Bibleverse adapter = "+cursorData.getCount());


        if (cursorData.moveToPosition(position)){
            String issueName = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
            int verseID = cursorData.getInt(cursorData.getColumnIndex(DatabaseTable.KEY_ID));
            int issueID = cursorData.getInt(cursorData.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            String verse = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_AMP));
            int favValue = cursorData.getInt(cursorData.getColumnIndex(DatabaseTable.KEY_IS_FAVORITE));

            //check if we are coming from the spinner selection, if null then we are not
            if (bibleVersion == null) {
                //check in prefs if user has a default version
                if (!defaultVersion.equals("kjv")) {
                    switch (defaultVersion) {
                        case "msg":
                            data.putInt("VerseID", verseID);
                            data.putString("Verse", verse + context.getString(R.string.msgVersion));
                            data.putString("verseContent", msgVerseContent);
                            break;
                        case "amp":
                            data.putInt("VerseID", verseID);
                            data.putString("Verse", verse + context.getString(R.string.ampVersion));
                            data.putString("verseContent", ampVerseContent);
                            break;
                        default:
                            data.putInt("VerseID", verseID);
                            data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                            data.putString("verseContent", kjvVerseContent);
                    }
                } else {
                    data.putInt("VerseID", verseID);
                    data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                    data.putString("verseContent", kjvVerseContent);
                }
            }else{
                switch (bibleVersion) {
                    case "MSG":
                        data.putInt("VerseID", verseID);
                        data.putString("Verse", verse + context.getString(R.string.msgVersion));
                        data.putString("verseContent", msgVerseContent);
                        break;
                    case "AMP":
                        data.putInt("VerseID", verseID);
                        data.putString("Verse", verse + context.getString(R.string.ampVersion));
                        data.putString("verseContent", ampVerseContent);
                        break;
                    case "Compare":
                        data.putString("compare_mode", "compare");
                        data.putInt("VerseID", verseID);
                        data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                        data.putString("verseContent", kjvVerseContent);
                        data.putString("msgVerse", verse + context.getString(R.string.msgVersion));
                        data.putString("msgContent", msgVerseContent);
                        data.putString("ampVerse", verse + context.getString(R.string.ampVersion));
                        data.putString("ampContent", ampVerseContent);
                        break;
                    default:
                        data.putInt("VerseID", verseID);
                        data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                        data.putString("verseContent", kjvVerseContent);
                }
            }

            data.putInt("issueID", issueID);
            data.putString("issueName", issueName);
            data.putInt("favValue", favValue);
            bibleVersesFragment.setArguments(data);
            //mViewPager.getAdapter().notifyDataSetChanged();
        }

        //mViewPager.getAdapter().notifyDataSetChanged();
        return bibleVersesFragment;
    }

    @Override
    public int getItemPosition(Object item){
        BibleVersesFragment bibleVersesFragment = (BibleVersesFragment)item;
        //String title = bibleVersesFragment.title;
        int position = cursorData.getPosition();
/*
        if (position >= 0){
            return position;
        }else {
            return POSITION_NONE;
        }
*/
        //return ((BibleVersesFragment) item).getId();
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return cursorData.getCount();
    }
}
