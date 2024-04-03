package com.lifeissues.lifeissues.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;

/**
 * Factory method that allows us to create a ViewModel with a constructor that takes a
 * {@link LifeIssuesRepository}
 */
public class UserProfileActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final LifeIssuesRepository mRepository;

    public UserProfileActivityViewModelFactory(LifeIssuesRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        //return (T) new UserProfileActivityViewModel(mRepository);
        return null;
    }
}
