package com.lifeissues.lifeissues.ui.viewmodels;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;

public class TestimonyPrayerViewModelFactory extends ViewModelProvider.NewInstanceFactory
implements ViewModelProvider.Factory{
    private final LifeIssuesRepository mRepository;
    private Context mContext;

    public TestimonyPrayerViewModelFactory(LifeIssuesRepository repository, Context context) {
        this.mRepository = repository;
        this.mContext = context;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return null;
        //return (T) new TestimonyPrayerViewModel(mRepository, mContext);
    }
}
