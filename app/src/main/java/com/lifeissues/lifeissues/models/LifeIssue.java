package com.lifeissues.lifeissues.models;

/**
 * Created by Emo on 8/31/2017.
 */

public class LifeIssue {
    private String issueName;
    private String verses;
    private String num_of_verses;
    private int id;
    private boolean isImportant;

    private String image;

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getVerses() {
        return verses;
    }

    public void setVerses(String verses) {
        this.verses = verses;
    }

    public String getNum_of_verses() {
        return num_of_verses;
    }

    public void setNum_of_verses(String num_of_verses) {
        this.num_of_verses = num_of_verses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
