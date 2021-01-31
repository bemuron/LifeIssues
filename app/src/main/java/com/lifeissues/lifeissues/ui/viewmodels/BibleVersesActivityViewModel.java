package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;

public class BibleVersesActivityViewModel extends AndroidViewModel {
    private LifeIssuesRepository issuesRepository;

    public BibleVersesActivityViewModel(@NonNull Application application) {
        super(application);
        issuesRepository = new LifeIssuesRepository(application);
    }

    //get the bible verses related to the issue id
    public Cursor getBibleVersesForIssue(int issueId){
        return issuesRepository.getBibleVersesForIssue(issueId);
    }

    //get a random verse
    public Cursor getRandomVerse(){
        return issuesRepository.getRandomVerse();
    }

    //get all the favourite verses
    public Cursor getAllFavouriteVerses(){
        return issuesRepository.getAllFavouriteVerses();
    }

    //get a single Bible verse
    public Cursor getSingleVerse(int verseId){
        return issuesRepository.getSingleVerse(verseId);
    }

    //add a favorite verse
    public void addFavorite(int verse_id, int issueID){
        issuesRepository.addFavVerse(verse_id, issueID);
    }

    //remove a favorite verse
    public void deleteFavourite(int verse_id, int issueID){
        issuesRepository.deleteFavVerse(verse_id, issueID);
    }

    //get number of all Bible verses in the db
    public int getTotNumberOfVerses(){
        return issuesRepository.countAllBibleVerses();
    }

    //adding a daily verse to the db
    public boolean addDailyVerse(int verse_id, String verse, String kjv, String msg, String amp,
                              String favValue, String issueName, int issue_id, String dateTaken){
        return issuesRepository.addDailyVerse(verse_id, verse, kjv, msg, amp,
                favValue, issueName, issue_id, dateTaken);
    }

    //get daily verse
    public Cursor getDailyVerse(String dateToday){
        return issuesRepository.getDailyVerse(dateToday);
    }

    //check daily verse
    public Cursor checkDailyVerse(String dateToday){
        return issuesRepository.checkDailyVerse(dateToday);
    }

    //get issues
    public Cursor getIssues(){
        return issuesRepository.getIssues();
    }

    //getting a favourite issue from db
    public Cursor getFavoriteIssue(String issueName){
        return issuesRepository.getFavouriteIssue(issueName);
    }

    //add a fav issue
    public boolean addFavIssue(String issueName, String verses){
        return issuesRepository.addFavoriteIssue(issueName, verses);
    }

    //delete fav issue
    public boolean deleteFavIssue(String issue){
        return issuesRepository.deleteFavIssue(issue);
    }

    //get all favorite issues
    public Cursor getAllFavoriteIssues(){
        return issuesRepository.getAllFavoriteIssues();
    }

    //get all the favourite notes
    public Cursor getAllFavouriteNotes(){
        return issuesRepository.getAllFavoriteNotes();
    }

    //delete favourite note
    public void deleteFavouriteNote(String favValue, int verseId){
        issuesRepository.deleteFavNote(favValue,verseId);
    }

    //add favourite note
    public void addFavouriteNote(String favValue, int verseId){
        issuesRepository.deleteFavNote(favValue,verseId);
    }
}
