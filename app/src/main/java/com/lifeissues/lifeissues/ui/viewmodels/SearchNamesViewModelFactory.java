package com.lifeissues.lifeissues.ui.viewmodels;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link com.lifeissues.lifeissues.data.LifeIssuesRepository}
 */
public class SearchNamesViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final LifeIssuesRepository mRepository;
    private Context context;

    public SearchNamesViewModelFactory(LifeIssuesRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new SearchNamesActivityViewModel(mRepository);
    }
}
