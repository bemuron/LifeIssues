package com.lifeissues.lifeissues.models;

import android.app.SearchManager;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts3;
import androidx.room.PrimaryKey;

import com.lifeissues.lifeissues.data.database.IssuesDao;

@Fts3
@Entity(tableName = "issues")
public class Issue {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "rowid")
    private int rowId;

    @ColumnInfo(name = IssuesDao.KEY_ISSUE_NAME)//name of issue
    private String issueName;

    @ColumnInfo(name = IssuesDao.KEY_ISSUE_VERSES)//verses
    private String issueVerses;

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getIssueVerses() {
        return issueVerses;
    }

    public void setIssueVerses(String issueVerses) {
        this.issueVerses = issueVerses;
    }
}
