package com.lifeissues.lifeissues.data.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface BibleVersesDao {

    //get all Bible verses
    @Query("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
            "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
            "issues i on i.ROWID = iv.issue_id")
    Cursor getAllBibleVerses();

    //count all bible verses in the db
    @Query("select count(_id) as versesNumber from bible_verses")
    int countAllBibleVerses();

    //getting specific category content from the db
    @Query("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
            "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
            "issues i on i.ROWID = iv.issue_id WHERE iv.issue_id = :issueID")
    Cursor getBibleVerses(int issueID);

    @Query("select b._id," +
            "iv.issue_id," +
            "i.suggest_text_1," +
            "b.verse, b.kjv," +
            "b.msg," +
            "b.amp," +
            "iv.is_favorite " +
            "from bible_verses b " +
            "JOIN issues_verses iv on iv.verse_id = b._id " +
            "JOIN issues i on i.ROWID = iv.issue_id " +
            "WHERE iv.is_favorite = 1")
    Cursor getAllFavouriteVerses();

    //checking if a specific verse is favorite
    @Query("select _id " +
            "from bible_verses " +
            "WHERE _id in (select verse_id from issues_verses where issue_id = :issueID and verse_id = :verseID and is_favorite =1)")
    boolean isVerseFavorite(int verseID, int issueID);

    //getting random verse from the db
    @Query("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
            "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
            "issues i on i.ROWID = iv.issue_id order by random() LIMIT 1")
    Cursor getRandomVerse();

    //getting a single Bible verse from the db
    @Query("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
            "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
            "issues i on i.ROWID = iv.issue_id where b._id = :verseId")
    Cursor getSingleBibleVerse(int verseId);

    //adding a favourite Bible verse to the db
    @Query("UPDATE issues_verses set is_favorite = 1 WHERE verse_id = :verse_id AND issue_id = :issueID")
    int addFavourite(int verse_id, int issueID);

    //adding a favourite Bible verse to the db
    @Query("UPDATE issues_verses set is_favorite = 0 WHERE verse_id = :verse_id AND issue_id = :issueID")
    int deleteFavourite(int verse_id, int issueID);
}
