package com.lifeissues.lifeissues.ui.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.lifeissues.lifeissues.ui.fragments.IssuesFragment;
import com.lifeissues.lifeissues.ui.fragments.TodayVerseFragment;

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
