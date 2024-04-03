package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "testimonies")
public class Testimony {

    @PrimaryKey
    @ColumnInfo(name = "testimony_id")
    private int testimony_id;

    @ColumnInfo(name = "category_id")
    private int category_id;

    @ColumnInfo(name = "testimony_name")
    private String testimony_name;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "testimony_likes")
    private int testimonyLikes;

    @ColumnInfo(name = "posted_by")
    private int posted_by;

    @ColumnInfo(name = "posted_on")
    private String posted_on;

    @ColumnInfo(name = "testimony_status")
    private int testimony_status;

    @ColumnInfo(name = "is_reported")
    private int is_reported;

    @ColumnInfo(name = "image_name")
    private String image_name;

    @ColumnInfo(name = "profile_pic")
    private String profile_pic;

    @ColumnInfo(name = "user_name")
    private String user_name;

    @ColumnInfo(name = "poster_name")
    private String posterName;

    @ColumnInfo(name = "is_liked")
    private int is_liked;

    @Ignore
    private int likes_number;

    @Ignore
    private int color = -1;


    public int getTestimony_id() {
        return testimony_id;
    }

    public void setTestimony_id(int testimony_id) {
        this.testimony_id = testimony_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTestimonyLikes() {
        return testimonyLikes;
    }

    public void setTestimonyLikes(int testimonyLikes) {
        this.testimonyLikes = testimonyLikes;
    }

    public int getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(int posted_by) {
        this.posted_by = posted_by;
    }

    public String getPosted_on() {
        return posted_on;
    }

    public void setPosted_on(String posted_on) {
        this.posted_on = posted_on;
    }

    public int getTestimony_status() {
        return testimony_status;
    }

    public void setTestimony_status(int testimony_status) {
        this.testimony_status = testimony_status;
    }

    public int getIs_reported() {
        return is_reported;
    }

    public void setIs_reported(int is_reported) {
        this.is_reported = is_reported;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getTestimony_name() {
        return testimony_name;
    }

    public void setTestimony_name(String testimony_name) {
        this.testimony_name = testimony_name;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public int getIs_liked() {
        return is_liked;
    }

    public void setIs_liked(int is_liked) {
        this.is_liked = is_liked;
    }

    public int getLikes_number() {
        return likes_number;
    }

    public void setLikes_number(int likes_number) {
        this.likes_number = likes_number;
    }
}
