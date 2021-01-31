package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favourites")
public class Favourite {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int verseId;

    @ColumnInfo(name = "issue_name")
    private String mIssueName;

    @ColumnInfo(name = "issue_verses")
    private String mIssueVerse;

    public int getVerseId() {
        return verseId;
    }

    public void setVerseId(int verseId) {
        this.verseId = verseId;
    }

    public String getIssueName() {
        return mIssueName;
    }

    public void setIssueName(String mIssueName) {
        this.mIssueName = mIssueName;
    }

    public String getIssueVerse() {
        return mIssueVerse;
    }

    public void setIssueVerse(String mIssueVerse) {
        this.mIssueVerse = mIssueVerse;
    }
}
