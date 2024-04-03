package com.lifeissues.lifeissues.data.network;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PageKeyedDataSource;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.models.PrayerRequest;

public class BrowsePrayerRequestsDataFactory extends DataSource.Factory {
    private LifeIssuesRepository repository;

    //creating the mutable live data
    private MutableLiveData<PageKeyedDataSource<Integer, PrayerRequest>> browsedRequestsLiveDataSource = new MutableLiveData<>();
    private DataSource<Integer, PrayerRequest> prayerRequestDataSource;

    @NonNull
    @Override
    public DataSource<Integer, PrayerRequest> create() {
        //getting our data source object
        BrowsedPrayerRequestsDataSource prayerRequestsDataSource = new BrowsedPrayerRequestsDataSource();

        //posting the datasource to get the values
        browsedRequestsLiveDataSource.postValue(prayerRequestsDataSource);

        prayerRequestDataSource = prayerRequestsDataSource;

        //returning the datasource
        return prayerRequestsDataSource;
    }

    public DataSource<Integer, PrayerRequest> getBrowsedPrayerRequestsLiveDataSource() {
        return prayerRequestDataSource;
    }
}
