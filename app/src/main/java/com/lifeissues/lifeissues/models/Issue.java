package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.lifeissues.lifeissues.data.database.IssuesDao;

@Entity(tableName = "issues")
public class Issue {

    @PrimaryKey
    @ColumnInfo(name = "issue_id")
    private int issue_id;

    @ColumnInfo(name = IssuesDao.KEY_ISSUE_NAME)//name
    private String issueName;

    @ColumnInfo(name = IssuesDao.KEY_ISSUE_DESCRIPTION)//description
    private String description;

    @ColumnInfo(name = "image")//image
    private String image;

    @ColumnInfo(name = "is_favorite")
    private int is_favorite;

    public int getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(int issue_id) {
        this.issue_id = issue_id;
    }

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(int is_favorite) {
        this.is_favorite = is_favorite;
    }
}
