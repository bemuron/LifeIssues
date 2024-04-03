package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "prayer_requests")
public class PrayerRequest {

    @PrimaryKey
    @ColumnInfo(name = "prayer_id")
    private int prayer_id;

    @ColumnInfo(name = "category_id")
    private int category_id;

    @ColumnInfo(name = "prayer_title")
    private String prayer_title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "prayers_received")
    private int prayersReceived;

    @ColumnInfo(name = "posted_by")
    private int posted_by;

    @ColumnInfo(name = "posted_on")
    private String posted_on;

    @ColumnInfo(name = "prayer_status")
    private int prayerStatus;

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

    @ColumnInfo(name = "is_prayed_for")
    private int is_prayed_for;

    @Ignore
    private int likes_number;

    @Ignore
    private int color = -1;


    public int getPrayer_id() {
        return prayer_id;
    }

    public void setPrayer_id(int prayer_id) {
        this.prayer_id = prayer_id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrayersReceived() {
        return prayersReceived;
    }

    public void setPrayersReceived(int prayersReceived) {
        this.prayersReceived = prayersReceived;
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

    public int getPrayerStatus() {
        return prayerStatus;
    }

    public void setPrayerStatus(int prayerStatus) {
        this.prayerStatus = prayerStatus;
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

    public String getPrayer_title() {
        return prayer_title;
    }

    public void setPrayer_title(String prayer_title) {
        this.prayer_title = prayer_title;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public int getIs_prayed_for() {
        return is_prayed_for;
    }

    public void setIs_prayed_for(int is_prayed_for) {
        this.is_prayed_for = is_prayed_for;
    }

    public int getLikes_number() {
        return likes_number;
    }

    public void setLikes_number(int likes_number) {
        this.likes_number = likes_number;
    }
}
