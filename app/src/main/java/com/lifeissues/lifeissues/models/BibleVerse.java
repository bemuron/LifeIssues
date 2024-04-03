package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "bible_verses")
public class BibleVerse {

    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int verseId;

    @Ignore
    private String name;

    @ColumnInfo(name = "verse")
    private String mBibleVerse;

    @ColumnInfo(name = "kjv")
    private String mKJV;

    @ColumnInfo(name = "msg")
    private String mMSG;

    @ColumnInfo(name = "amp")
    private String mAMP;

    @Ignore
    private int issue_id;

    @Ignore
    private int is_favorite;

    public int getVerseId() {
        return verseId;
    }

    public void setVerseId(int verseId) {
        this.verseId = verseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getIssueId() {
        return issue_id;
    }

    public void setIssueId(int mIssueId) {
        this.issue_id = mIssueId;
    }

    public int getFav() {
        return is_favorite;
    }

    public void setFav(int mFav) {
        this.is_favorite = mFav;
    }
}
