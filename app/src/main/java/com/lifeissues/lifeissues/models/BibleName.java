package com.lifeissues.lifeissues.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts3;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_BIBLE_NAME;
import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_MEANING;

import android.app.SearchManager;
import android.provider.BaseColumns;

@Fts3
@Entity(tableName = "bible_names")
public class BibleName {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "rowid")
    private int nameId;

    @ColumnInfo(name = KEY_BIBLE_NAME)
    private String name;

    @ColumnInfo(name = KEY_MEANING)
    private String meaning;

    @Ignore
    @ColumnInfo(name = SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID)
    private String suggestDataId;

    @Ignore
    @ColumnInfo(name = SearchManager.SUGGEST_COLUMN_SHORTCUT_ID)
    private String suggestShortcutId;

    @Ignore
    @ColumnInfo(name = BaseColumns._ID)
    private String columnId;

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    /*public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getSuggestDataId() {
        return suggestDataId;
    }

    public void setSuggestDataId(String suggestDataId) {
        this.suggestDataId = suggestDataId;
    }

    public String getSuggestShortcutId() {
        return suggestShortcutId;
    }

    public void setSuggestShortcutId(String suggestShortcutId) {
        this.suggestShortcutId = suggestShortcutId;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }
}
