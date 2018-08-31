package com.lifeissues.lifeissues.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.fragments.BibleVersesFragment;

import database.DatabaseTable;

/**
 * Created by Emo on 11/28/2017.
 */

public class BibleVersesPagerAdapter extends FragmentStatePagerAdapter {
    private Cursor cursorData;
    private SharedPreferences prefs;
    private String bibleVersion;

    Context context = AppController.getContext();

    public BibleVersesPagerAdapter(FragmentManager fm, Cursor cursor,
                                   SharedPreferences sharedPreferences, String version) {
        super(fm);
        this.cursorData = cursor;
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


        if (cursorData.moveToPosition(position)){
            int verseID = cursorData.getInt(cursorData.getColumnIndex(DatabaseTable.KEY_ID));
            String verse = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_VERSE));
            String kjvVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_KJV));
            String msgVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_MSG));
            String ampVerseContent = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_AMP));
            String favValue = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_FAVOURITE));
            String issue_name = cursorData.getString(cursorData.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
            //check if we are coming from the spinner selection, if null then we are not
            if (bibleVersion == null) {
                //check in prefs if user has a default version
                if (!defaultVersion.equals("kjv")) {
                    switch (defaultVersion) {
                        case "kjv":
                            data.putInt("VerseID", verseID);
                            data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                            data.putString("verseContent", kjvVerseContent);
                            break;
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
                    case "KJV":
                        data.putInt("VerseID", verseID);
                        data.putString("Verse", verse + context.getString(R.string.kjvVersion));
                        data.putString("verseContent", kjvVerseContent);
                        break;
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

            data.putString("issueName", issue_name);
            data.putString("favValue", favValue);
            //getSupportActionBar().setTitle(issue_name.substring(0, 1).toUpperCase() + issue_name.substring(1));
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
