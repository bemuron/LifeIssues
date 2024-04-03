package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.data.database.PrayerRequestsDao;
import com.lifeissues.lifeissues.data.database.TestimoniesDao;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;

public class MainActivityViewModel extends AndroidViewModel {
    private LifeIssuesRepository issuesRepository;
    LiveData<PagedList<Testimony>> testimoniesPagedList;
    LiveData<PagedList<PrayerRequest>> prayerPagedList;
    private TestimoniesDao testimoniesDao;
    private PrayerRequestsDao prayerRequestsDao;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        issuesRepository = new LifeIssuesRepository(application);
        //this.testimoniesDao = issuesRepository.getHomeTestimonies();
        this.testimoniesDao = issuesRepository.getTestimoniesDao();
        this.prayerRequestsDao = issuesRepository.getPrayerRequestsDao();
        //this.bibleNamesDao = this.issuesRepository.getBibleNamesDao();

        //Getting PagedList config for articles
        PagedList.Config pagedListConfig1 =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(20).build();

        //Building the paged list
        testimoniesPagedList = new LivePagedListBuilder<>(testimoniesDao.getAllTestimonies(), pagedListConfig1)
                .build();

        //Getting PagedList config for articles
        PagedList.Config pagedListConfig2 =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(20).build();

        //Building the paged list
        prayerPagedList = new LivePagedListBuilder<>(prayerRequestsDao.getAllPrayerRequests(), pagedListConfig2)
                .build();
    }

    public int countAllBibleVerses(){
        return issuesRepository.countAllBibleVerses();
    }

    //get a random verse
    public Cursor getRandomVerseContent(){
        return issuesRepository.getRandomVerse();
    }

    public LiveData<PagedList<Testimony>> getHomeTestimonies() {
        return testimoniesPagedList;
    }

    public LiveData<PagedList<PrayerRequest>> getHomePrayer() {
        return prayerPagedList;
    }
}
