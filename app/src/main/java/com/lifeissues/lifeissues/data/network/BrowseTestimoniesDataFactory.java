package com.lifeissues.lifeissues.data.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.models.Testimony;

public class BrowseTestimoniesDataFactory extends DataSource.Factory {
    private LifeIssuesRepository repository;

    //creating the mutable live data
    private MutableLiveData<PageKeyedDataSource<Integer, Testimony>> browsedAdsLiveDataSource = new MutableLiveData<>();
    //private MutableLiveData<BrowsedJobsDataSource> browsedJobsLiveDataSource = new MutableLiveData<>();
    private DataSource<Integer, Testimony> testimonyDataSource;

    @NonNull
    @Override
    public DataSource<Integer, Testimony> create() {
        //getting our data source object
        BrowsedTestimoniesDataSource testimoniesDataSource = new BrowsedTestimoniesDataSource();

        //posting the datasource to get the values
        browsedAdsLiveDataSource.postValue(testimoniesDataSource);

        testimonyDataSource = testimoniesDataSource;

        //returning the datasource
        return testimoniesDataSource;
    }

    public DataSource<Integer, Testimony> getBrowsedTestimoniesLiveDataSource() {
        return testimonyDataSource;
    }
}
