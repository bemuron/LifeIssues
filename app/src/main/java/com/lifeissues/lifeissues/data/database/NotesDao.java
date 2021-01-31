package com.lifeissues.lifeissues.data.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lifeissues.lifeissues.models.Note;

@Dao
public interface NotesDao {
    //getting all the goal-notes out of the db
    @Query("SELECT * FROM notes")
    Cursor getNotes();

    /*
    insert notes into db
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    //Update a note
    @Update
    void updateNote(Note note);

    //delete a note
    @Delete
    void deleteNote(Note note);

    //get a single note
    @Query("SELECT * FROM notes WHERE _id = :id")
    Cursor getNote(int id);

    //get all favorite notes
    //@Query("SELECT * FROM notes WHERE favourite =" + favVal)
    //Cursor getAllFavouriteNotes();

    //adding a favourite note to the db
    @Query("UPDATE notes set favourite = :favVal WHERE _id = :verse_id")
    int addFavouriteNote(String favVal,int verse_id);

    //deleting a favourite note from the db
    @Query("UPDATE notes set favourite = :favVal WHERE _id = :verse_id")
    int deleteFavouriteNote(String favVal, int verse_id);

    //get all favorite notes
    @Query("SELECT * FROM notes WHERE favourite = :favValue")
    Cursor getAllFavouriteNotes(String favValue);
}
