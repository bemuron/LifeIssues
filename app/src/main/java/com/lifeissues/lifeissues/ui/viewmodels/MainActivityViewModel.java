package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;

public class MainActivityViewModel extends AndroidViewModel {
    private LifeIssuesRepository issuesRepository;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        issuesRepository = new LifeIssuesRepository(application);
    }

    public int countAllBibleVerses(){
        return issuesRepository.countAllBibleVerses();
    }

    //get a random verse
    public Cursor getRandomVerseContent(){
        return issuesRepository.getRandomVerse();
    }
}
