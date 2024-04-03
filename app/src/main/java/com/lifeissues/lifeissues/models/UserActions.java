package com.lifeissues.lifeissues.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserActions {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("profile_pic")
    private String profile_pic;

    @SerializedName("fixer_profile_pic")
    private String fixer_profile_pic;

    @SerializedName("poster_profile_pic")
    private String poster_profile_pic;

    @SerializedName("name")
    private String name;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("fixer_user_name")
    private String fixer_user_name;

    @SerializedName("poster_user_name")
    private String poster_user_name;

    private List<Testimony> recentTestimoniesList;

    private List<Testimony> browsedTestimoniesList;

    private List<PrayerRequest> browsedRequestsList;
    private List<PrayerRequest> recentRequestsList;
    private List<ImageUpload> imageUploads;

    //number of pages
    private int pages_count;

    public UserActions() {

    }

    public String getProfilePic() {
        return profile_pic;
    }

    public String getFixer_profile_pic() {
        return fixer_profile_pic;
    }

    public String getPoster_profile_pic() {
        return poster_profile_pic;
    }

    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public List<ImageUpload> getContentPics() {
        return imageUploads;
    }

    public int getPages_count() {
        return pages_count;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getPoster_user_name() {
        return poster_user_name;
    }

    public String getFixer_user_name() {
        return fixer_user_name;
    }

    public List<Testimony> getBrowsedTestimoniesList() {
        return browsedTestimoniesList;
    }

    public List<PrayerRequest> getBrowsedRequestsList() {
        return browsedRequestsList;
    }

    public List<Testimony> getRecentTestimoniesList() {
        return recentTestimoniesList;
    }

    public List<PrayerRequest> getRecentRequestsList() {
        return recentRequestsList;
    }
}
