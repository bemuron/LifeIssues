package com.lifeissues.lifeissues.models;

/**
 * Created by Emo on 8/31/2017.
 */

public class FavouriteVerse {
    private String issueName;
    private String verse;
    private int id;
    private int cursorPosition;

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getVerse() {
        return verse;
    }

    public void setVerse(String verse) {
        this.verse = verse;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }
}
