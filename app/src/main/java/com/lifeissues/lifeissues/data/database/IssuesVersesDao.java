package com.lifeissues.lifeissues.data.database;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface IssuesVersesDao {

    //deleting a favourite note from the db
    @Query("UPDATE notes set favourite = :favVal WHERE _id = :verse_id")
    int deleteFavouriteNote(String favVal, int verse_id);
}
