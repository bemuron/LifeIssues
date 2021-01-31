package com.lifeissues.lifeissues.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues_verses")
public class IssueVerse {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_id")
    private int rowId;

    @ColumnInfo(name = "verse_id")
    private String mVerseId;

    @ColumnInfo(name = "issue_id")
    private String mIssueId;

    @ColumnInfo(name = "is_favorite")
    private int mFavorite;

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getVerseId() {
        return mVerseId;
    }

    public void setVerseId(String mVerseId) {
        this.mVerseId = mVerseId;
    }

    public String getIssueId() {
        return mIssueId;
    }

    public void setIssueId(String mIssueId) {
        this.mIssueId = mIssueId;
    }

    public int getFavorite() {
        return mFavorite;
    }

    public void setFavorite(int mFavorite) {
        this.mFavorite = mFavorite;
    }
}
