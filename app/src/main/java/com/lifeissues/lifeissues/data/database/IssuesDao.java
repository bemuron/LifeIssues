package com.lifeissues.lifeissues.data.database;

import android.app.SearchManager;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lifeissues.lifeissues.models.Issue;

import java.util.List;

@Dao
public interface IssuesDao {
    String FTS_VIRTUAL_ISSUES_TABLE = "issues";

    //Issues table column names
    String KEY_ISSUE_NAME = "name";
    String KEY_ISSUE_DESCRIPTION = "description";

    @Insert
    void insertIssue(Issue issue);

    @Query("Delete from " +FTS_VIRTUAL_ISSUES_TABLE)
    void deleteAll();

    //get issue name
    @Query("SELECT " + KEY_ISSUE_NAME + " FROM " + FTS_VIRTUAL_ISSUES_TABLE + " WHERE issue_id = :issueID")
    String getIssueName(int issueID);

    @Query("SELECT * from issues ORDER BY issue_id ASC")
    LiveData<List<Issue>> getAllIssues();

    /*Issue Search Feature queries*/

    @Query("SELECT "+KEY_ISSUE_NAME + ","+ KEY_ISSUE_DESCRIPTION + ", issue_id AS "+ BaseColumns._ID+ ", " +
            "issue_id AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID+ ", issue_id AS " +
            SearchManager.SUGGEST_COLUMN_SHORTCUT_ID+
            " FROM issues WHERE "+KEY_ISSUE_NAME + " MATCH :query")
    Cursor getWordMatches(String query);

    @Query("SELECT issue_id AS rowid,"+KEY_ISSUE_NAME + ","+ KEY_ISSUE_DESCRIPTION + " FROM issues WHERE rowid = :rowid")
    Cursor getIssue(String rowid);

    //get search results
    @Query("select * FROM issues WHERE" +
            " "+KEY_ISSUE_NAME + " MATCH :query")
    LiveData<List<Issue>> getSearchResults(String query);

    //get search results cursor
    @Query("select issue_id AS rowid," +KEY_ISSUE_NAME + "," + KEY_ISSUE_DESCRIPTION +" FROM issues WHERE" +
            " "+KEY_ISSUE_NAME + " MATCH :query")
    Cursor getSearchResultsCursor(String query);

    //get all the issues
    @Query("SELECT issue_id AS rowid,"+KEY_ISSUE_NAME + ","+ KEY_ISSUE_DESCRIPTION + " FROM issues")
    Cursor getIssues();

    /*@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT rowid AS rowid,"+KEY_ISSUE_NAME + ","+KEY_ISSUE_VERSES+ " FROM issues")
    LiveData<Cursor> getIssues2();*/

    /*@Query("SELECT * FROM favourites WHERE issue_name =:issueName")
    Cursor getFavouriteIssue(String issueName);

    //add a favorite issue
    @Query("INSERT into favourites (_id, issue_name, issue_verses) values (:issueId, :issueName, :verses)")
    long addFavouriteIssue(int issueId, String issueName, String verses);

    //delete a favorite issue
    @Query("DELETE from favourites where issue_name = :issue")
    int deleteFavouriteIssue(String issue);

    //getting all fav issues
    @Query("SELECT * FROM favourites")
    Cursor getAllFavouriteIssues();*/

}
