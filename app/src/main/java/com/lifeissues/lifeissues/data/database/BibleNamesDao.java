package com.lifeissues.lifeissues.data.database;

import android.app.SearchManager;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lifeissues.lifeissues.models.BibleName;

import java.util.List;

@Dao
public interface BibleNamesDao {

    //Table Name
    static final String FTS_VIRTUAL_PHRASES_TABLE = "bible_names";

    //column names
    public static final String KEY_ID = "_id";

    //the name is suggest_text_1
    String KEY_BIBLE_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    //suggest_text_2
    String KEY_MEANING = SearchManager.SUGGEST_COLUMN_TEXT_2;


    /*
    insert name into db
     */
    @Insert
    long insertName(BibleName bibleName);

    @Query("Delete from bible_names")
    void deleteAll();

    /*@Query("select rowid AS rowid," +KEY_MEANING + "," + KEY_BIBLE_NAME +", isFavourite " +
            "from bible_names where section_id = :sectionId and category_id = :categoryId")
    LiveData<List<BibleName>> getPhrasesInSection(int sectionId, int categoryId);*/

    //@Query("SELECT * FROM phrases")
    //Cursor getPhrasesInCategory();

    //get a random phrase
    @Query("select rowid AS rowid," +KEY_MEANING + "," + KEY_BIBLE_NAME + " FROM bible_names WHERE rowid = :nameId")
    LiveData<List<BibleName>> getRandomPhrase(int nameId);

    //adding a favourite to the db
    //@Query("UPDATE bible_names SET isFavourite = 1 WHERE rowid = :nameId")
    //void addFavorite(int nameId);

    //getting a favourite from the db
    /*@Query("select rowid AS rowid," +KEY_MEANING + "," + KEY_BIBLE_NAME +", isFavourite" +
            " FROM bible_names WHERE isFavourite = 1")
    LiveData<List<BibleName>> getFavouritePhrases();*/

    //removing/unsetting a favourite
    //@Query("UPDATE bible_names SET isFavourite = 0 WHERE rowid = :nameId")
    //void removeFavorite(int nameId);

    //count all the phrases we have
    @Query("SELECT COUNT(rowid) FROM bible_names")
    int phrasesNumber();

    /*Phrases Search Feature queries*/

    @Query("SELECT rowid AS rowid, "+KEY_MEANING + ","+KEY_BIBLE_NAME+ ", rowid AS "+ BaseColumns._ID+ ", " +
            "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID+ ", rowid AS " +
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID+
            " FROM bible_names WHERE "+KEY_BIBLE_NAME + " MATCH :query")
    Cursor getWordMatches(String query);
    //DataSource.Factory<Integer, BibleName> getWordMatches(String query);

    @Query("SELECT rowid AS rowid, "+KEY_MEANING + ","+KEY_BIBLE_NAME+ ", rowid AS "+ BaseColumns._ID+ ", " +
            "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID+ ", rowid AS " +
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID+
            " FROM bible_names")
    DataSource.Factory<Integer, BibleName> getAllNames();


    @Query("SELECT rowid AS rowid,"+KEY_MEANING + ","+KEY_BIBLE_NAME+
            " FROM bible_names WHERE rowid = :rowid")
    Cursor getPhrase(String rowid);

    //get search results
    @Query("select rowid AS rowid," +KEY_MEANING + "," + KEY_BIBLE_NAME +
            " FROM bible_names WHERE "+KEY_BIBLE_NAME + " MATCH :query")
    LiveData<List<BibleName>> getSearchResults(String query);

    //get search results cursor
    @Query("select rowid AS rowid," +KEY_MEANING + "," + KEY_BIBLE_NAME +
            " FROM bible_names WHERE "+KEY_BIBLE_NAME + " MATCH :query")
    Cursor getSearchResultsCursor(String query);
}
