package com.lifeissues.lifeissues.ui.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.lifeissues.lifeissues.ui.fragments.FavouriteIssuesFragment;
import com.lifeissues.lifeissues.ui.fragments.FavouriteNotesListFragment;
import com.lifeissues.lifeissues.ui.fragments.FavouriteVersesFragment;

/**
 * Created by Emo on 8/31/2017.
 */

public class FavouritesTabsPagerAdapter extends FragmentPagerAdapter {

    int tabCount;

    public FavouritesTabsPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return FavouriteVersesFragment.newInstance();
            case 1:
                return FavouriteIssuesFragment.newInstance();
            case 2:
                return FavouriteNotesListFragment.newInstance();
            default:
                return FavouriteVersesFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
