package com.lifeissues.lifeissues.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_verses")
public class DailyVerse {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_id")
    private int verseId;

    @ColumnInfo(name = "verse")
    private String mBibleVerse;

    @ColumnInfo(name = "kjv")
    private String mKJV;

    @ColumnInfo(name = "msg")
    private String mMSG;

    @ColumnInfo(name = "amp")
    private String mAMP;

    @ColumnInfo(name = "issue_name")
    private String mIssueName;

    @ColumnInfo(name = "issue_id")
    private int mIssueId;

    @ColumnInfo(name = "favourite")
    private String mFav;

    @ColumnInfo(name = "date_taken")
    private String mDateTaken;

    public int getVerseId() {
        return verseId;
    }

    public void setVerseId(int verseId) {
        this.verseId = verseId;
    }

    public String getBibleVerse() {
        return mBibleVerse;
    }

    public void setBibleVerse(String mBibleVerse) {
        this.mBibleVerse = mBibleVerse;
    }

    public String getKJV() {
        return mKJV;
    }

    public void setKJV(String mKJV) {
        this.mKJV = mKJV;
    }

    public String getMSG() {
        return mMSG;
    }

    public void setMSG(String mMSG) {
        this.mMSG = mMSG;
    }

    public String getAMP() {
        return mAMP;
    }

    public void setAMP(String mAMP) {
        this.mAMP = mAMP;
    }

    public String getIssueName() {
        return mIssueName;
    }

    public void setIssueName(String mIssueName) {
        this.mIssueName = mIssueName;
    }

    public int getIssueId() {
        return mIssueId;
    }

    public void setIssueId(int mIssueId) {
        this.mIssueId = mIssueId;
    }

    public String getFav() {
        return mFav;
    }

    public void setFav(String mFav) {
        this.mFav = mFav;
    }

    public String getDateTaken() {
        return mDateTaken;
    }

    public void setDateTaken(String mDateTaken) {
        this.mDateTaken = mDateTaken;
    }
}
