package com.lifeissues.lifeissues.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.BibleVerseResult;
import com.lifeissues.lifeissues.ui.fragments.BibleVersesFragment;

import java.util.List;

/**
 * Created by Emo on 11/28/2017.
 */

public class BibleVersesPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = BibleVersesPagerAdapter.class.getSimpleName();
    private List<BibleVerseResult> bibleVerseList;
    private BibleVerse bibleVerse;
    private SharedPreferences prefs;
    private String bibleVersion;
    private int issueID, currentPosition;

    Context context = AppController.getContext();

    public BibleVersesPagerAdapter(FragmentManager fm, List<BibleVerseResult> versesList,
                                   SharedPreferences sharedPreferences, String version) {
        super(fm);
        this.bibleVerseList = versesList;
        //this.issueID = issueID;
        this.prefs = sharedPreferences;
        this.bibleVersion = version;
    }

    @Override
    public Fragment getItem(int position) {
        currentPosition = position;
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        BibleVersesFragment bibleVersesFragment = new BibleVersesFragment();
        Bundle data = new Bundle();
        //check if user set default bible verse version
        String defaultVersionKey = context.getString(R.string.key_daily_verse_version);
        String defaultVersion = prefs.getString(defaultVersionKey, "kjv");
        if (bibleVerseList != null){
            bibleVerse = new BibleVerse();
            String issueName = bibleVerseList.get(position).getName();
            int verseID = bibleVerseList.get(position).get_id();
            int issueID = bibleVerseList.get(position).getIssue_id();
            String verse = bibleVerseList.get(position).getVerse();
            String kjvVerseContent = bibleVerseList.get(position).getKjv();
            String msgVerseContent = bibleVerseList.get(position).getMsg();
            String ampVerseContent = bibleVerseList.get(position).getAmp();
            int favValue = bibleVerseList.get(position).getIs_favorite();

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
        }

        //mViewPager.getAdapter().notifyDataSetChanged();
        return bibleVersesFragment;
    }

    @Override
    public int getItemPosition(Object item){
        BibleVersesFragment bibleVersesFragment = (BibleVersesFragment)item;
        //String title = bibleVersesFragment.title;
        //int position = cursorData.getPosition();
/*
        if (position >= 0){
            return position;
        }else {
            return POSITION_NONE;
        }
*/
        return currentPosition;
        //return ((BibleVersesFragment) item).getId();
        //return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return bibleVerseList.size();
    }
}
