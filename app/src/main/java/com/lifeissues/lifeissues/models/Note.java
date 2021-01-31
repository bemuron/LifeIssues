package com.lifeissues.lifeissues.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Emo on 6/7/2017.
 */

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "_id")
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "verse")
    private String verse;

    @ColumnInfo(name = "life_issue")
    private String issue;

    @ColumnInfo(name = "date_created")
    private String dateCreated;

    @ColumnInfo(name = "date_updated")
    private String dateModified;

    @Ignore
    private String timestamp;
    @Ignore
    private String picture;

    @ColumnInfo(name = "favourite")
    private String important;

    @Ignore
    private boolean isRead;
    @Ignore
    private int color = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImportant() {
        return important;
    }

    public void setImportant(String important) {
        important = important;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getVerse() {
        return verse;
    }

    public void setVerse(String verse) {
        this.verse = verse;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getReadableModifiedDate(){
    /*
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
        sdf.setTimeZone(getDataModified().getTimeZone());
        Date modifiedDate = getDataModified().getTime();
        String displayDate = sdf.format(modifiedDate);
        return displayDate;
        */
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

}
