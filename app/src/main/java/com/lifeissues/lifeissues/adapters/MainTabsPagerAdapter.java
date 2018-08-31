package com.lifeissues.lifeissues.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lifeissues.lifeissues.fragments.IssuesFragment;
import com.lifeissues.lifeissues.fragments.TodayVerseFragment;

/**
 * Created by Emo on 8/31/2017.
 */

public class MainTabsPagerAdapter extends FragmentStatePagerAdapter {

    int tabCount;

    public MainTabsPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return TodayVerseFragment.newInstance();
            case 1:
                return IssuesFragment.newInstance();
            default:
                return TodayVerseFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
