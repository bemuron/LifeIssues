package com.lifeissues.lifeissues.data.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface DailyVersesDao {

    //checking if a daily verse for today has already been entered
    @Query("SELECT * FROM daily_verses WHERE date_taken = :dateToday")
    Cursor checkDailyVerse(String dateToday);

    //adding a daily verse to the db
    @Query("INSERT INTO daily_verses (_id, verse, kjv, msg, amp, issue_name, issue_id, favourite, date_taken) " +
            "VALUES(:verse_id, :verse, :kjv, :msg, :amp, :issueName, :issue_id, :favValue, :dateTaken)")
    long addDailyVerse(int verse_id, String verse, String kjv, String msg, String amp,
                          String favValue, String issueName, int issue_id, String dateTaken);

    @Query("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
            "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
            "issues i on i.ROWID = iv.issue_id WHERE b._id =:verse_id LIMIT 1")
    Cursor getRandomVerse(int verse_id);

    //getting daily verse from the db
    @Query("SELECT * FROM daily_verses WHERE date_taken = :dateToday")
    Cursor getDailyVerse(String dateToday);
}
