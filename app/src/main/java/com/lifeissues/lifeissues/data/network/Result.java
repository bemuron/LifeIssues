package com.lifeissues.lifeissues.data.network;

import com.google.gson.annotations.SerializedName;
import com.lifeissues.lifeissues.models.User;

import java.util.ArrayList;
import java.util.List;

public class Result {
    @SerializedName("error")
    private Boolean error;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    @SerializedName("is_offer_already_made")
    private Boolean is_offer_already_made;

    @SerializedName("is_ad_liked")
    private Boolean is_ad_liked;

    @SerializedName("fixerRating")
    private User fixerRating;

    @SerializedName("posterRating")
    private User posterRating;

    @SerializedName("nin")
    private String nin;

    @SerializedName("access_token")
    private String access_token;

    private int pages_count;

    public Result(Boolean error, String message, User user, Boolean is_offer_already_made) {
        this.error = error;
        this.message = message;
        this.user = user;
        this.is_offer_already_made = is_offer_already_made;
    }


    public Boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Boolean getIs_offer_already_made() {
        return is_offer_already_made;
    }

    public Boolean getIs_ad_liked() {
        return is_ad_liked;
    }

    public User getFixerRating() {
        return fixerRating;
    }

    public User getPosterRating() {
        return posterRating;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getNin() {
        return nin;
    }

    public int getPages_count() {
        return pages_count;
    }
}
