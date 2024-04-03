package com.lifeissues.lifeissues.data.database;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RoomWarnings;

import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.BibleVerseResult;
import com.lifeissues.lifeissues.models.DailyVerse;

import java.util.List;

@Dao
public interface DailyVersesDao {

    //checking if a daily verse for today has already been entered
    @Query("SELECT * FROM daily_verses WHERE notify_date = :dateToday")
    Cursor checkDailyVerse(String dateToday);

    @Query("INSERT INTO daily_verses(verse_id, notify_date) VALUES (:verseId, :date)")
    void insertDailyVerseRecord(int verseId, String date);

    //getting daily verse from the db
    //@Query("SELECT * FROM daily_verses WHERE notify_date = :dateToday")
    //Cursor getDailyVerse(String dateToday);

    //@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select b._id, iv.issue_id, i.name, b.verse, b.kjv, b.msg, b.amp," +
            "iv.is_favorite from daily_verses d JOIN bible_verses b ON b._id = d.verse_id " +
            "JOIN issues_verses iv ON iv.verse_id = b._id JOIN issues i on i.issue_id = iv.issue_id " +
            "WHERE d.notify_date =:date")
    LiveData<List<BibleVerseResult>> getDailyVerse(String date);

    //adding a favourite Bible verse to the db
//    @Query("UPDATE daily_verses set favourite = 1 WHERE _id = :verse_id")
//    int setFavourite(int verse_id);

    //adding a favourite Bible verse to the db
//    @Query("UPDATE daily_verses set favourite = 0 WHERE _id = :verse_id")
//    int removeFavourite(int verse_id);
}
