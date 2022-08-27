package com.lifeissues.lifeissues.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts3;
import androidx.room.PrimaryKey;

import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_BIBLE_NAME;
import static com.lifeissues.lifeissues.data.database.BibleNamesDao.KEY_MEANING;

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
}
