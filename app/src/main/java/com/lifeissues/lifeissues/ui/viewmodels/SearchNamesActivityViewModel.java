package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.data.database.BibleNamesDao;
import com.lifeissues.lifeissues.models.BibleName;

/**
 * Created by BE on 11/02/2020.
 */

public class SearchNamesActivityViewModel extends ViewModel {
    private BibleNamesDao bibleNamesDao;
    //creating livedata for PagedList  and PagedKeyedDataSource
    LiveData<PagedList<BibleName>> searchResultsList;
    LiveData<PagedList<BibleName>> namesPagedList;
    //LiveData<BrowsedJobsDataSource> liveDataSource;
    LiveData<PageKeyedDataSource<Integer, BibleName>> liveDataSource;

    //private member variable to hold reference to the repository
    private LifeIssuesRepository mRepository;

    public SearchNamesActivityViewModel(LifeIssuesRepository lifeIssuesRepository) {
        this.mRepository = lifeIssuesRepository;
        this.bibleNamesDao = this.mRepository.getBibleNamesDao();

        //Getting PagedList config for articles
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(20).build();

        //Building the paged list
        namesPagedList = new LivePagedListBuilder<>(bibleNamesDao.getAllNames(), pagedListConfig)
                .build();
    }

    //a getter method to search jobs based on the query inserted by the user
    // This hides the implementation from the UI
    public LiveData<PagedList<BibleName>> searchForNames(String searchQuery){
        //getting our data source factory
        //SearchJobsDataFactory searchJobsDataFactory = new SearchJobsDataFactory(searchQuery);

        //getting the live data source from data source factory
        //liveDataSource = searchJobsDataFactory.getSearchedJobsLiveDataSource();

        //Getting PagedList config
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(20).build();

        //Building the paged list
        /*searchResultsList = (new LivePagedListBuilder(bibleNamesDao.getWordMatches(searchQuery), pagedListConfig))
                .build();*/

        return searchResultsList;
    }

    public LiveData<PagedList<BibleName>> getNamesList() {
        return namesPagedList;
    }

}
