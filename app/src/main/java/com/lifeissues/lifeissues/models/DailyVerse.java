package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_verses")
public class DailyVerse {

    //@PrimaryKey(autoGenerate = true)
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private int dailyVerseId;

    /**
    * Added column verse_id
    * */
    @ColumnInfo(name = "verse_id")
    private int verseId;

    /**
     * Removed columns verse, kjv, msg, amp, issue_name, issue_id, favourite
     * */

    /**
     * changed column name from date_taken to notify_date
     * */
    @ColumnInfo(name = "notify_date")
    private String mNotifyDate;

    public int getVerseId() {
        return verseId;
    }

    public void setVerseId(int verseId) {
        this.verseId = verseId;
    }

    public int getDailyVerseId() {
        return dailyVerseId;
    }

    public void setDailyVerseId(int dailyVerseId) {
        this.dailyVerseId = dailyVerseId;
    }

    public String getNotifyDate() {
        return mNotifyDate;
    }

    public void setNotifyDate(String mNotifyDate) {
        this.mNotifyDate = mNotifyDate;
    }
}
