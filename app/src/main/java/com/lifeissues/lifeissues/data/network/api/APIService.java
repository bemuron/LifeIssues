package com.lifeissues.lifeissues.data.network.api;


import com.lifeissues.lifeissues.data.network.Result;
import com.lifeissues.lifeissues.models.UserActions;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface APIService {

    //getting the app categories
    /*@Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("categories")
    Call<Categories> getCategories();

    //get the featured content
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getFeaturedContent")
    Call<Categories> getFeaturedContent();

    //get the recently posted tasks
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getRecentTasks")
    Call<Categories> getRecentTasks();
     */

    //the login/signing call
    @FormUrlEncoded
    @POST("login")
    Call<Result> userLogin(
            @Field("email") String email,
            @Field("password") String password);

    //user log out
    //deletes the user's fcm and access tokens
    @FormUrlEncoded
    @POST("logout")
    Call<Result> userLogout(
            @Header("Authorization") String token,
            @Field("user_id") int user_id);

    //The register call
    @FormUrlEncoded
    @POST("register")
    Call<Result> createUser(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password);

    //get content images for edit
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getContentImages/{content_id}")
    Call<UserActions> getContentImages(
            //@Header("Authorization") String token,
            @Path("content_id") int content_id,
            @Field("content_type") int content_type);

    //get content images for details view
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getContentImagesDetails/{content_id}")
    Call<UserActions> getContentImagesDetails(
            //@Header("Authorization") String token,
            @Path("content_id") int content_id);

    //delete the ad pic
    @FormUrlEncoded
    @POST("deleteContentPic")
    Call<Result> deleteAdPic(
            @Header("Authorization") String token,
            @Field("pic_id") int pic_id);

    //saving a report made by a user on any content
    @FormUrlEncoded
    @POST("reportContent")
    Call<Result> reportContent(
            @Header("Authorization") String token,
            @Field("reported_by") int reported_by,
            @Field("content_id") int content_id,
            @Field("comment") String comment,
            @Field("content_type") int content_type);

    @Multipart
    @POST("postNewContent")
    Call<Result> postNewContent(
            @Header("Authorization") String token,
            @Part("title") String title,
            @Part("description") String description,
            @Part("user_id") int user_id,
            @Part("content_type") int content_type,
            @Part("category_id") int categoryId,
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize);

    @Multipart
    @POST("editContent")
    Call<Result> editContent(
            @Header("Authorization") String token,
            @Part("content_id") int content_id,
            @Part("title") String title,
            @Part("description") String description,
            @Part("user_id") int user_id,
            @Part("content_type") int content_type,
            @Part("category_id") int categoryId,
            @Part MultipartBody.Part[] file,
            @Part("size") int partsSize);

    //deleting content
    @FormUrlEncoded
    @POST("deleteContent")
    Call<Result> deleteContent(
            @Header("Authorization") String token,
            @Field("user_id") int user_id,
            @Field("content_id") int content_id,
            @Field("content_type") int content_type);

    //user like content
    @FormUrlEncoded
    @POST("likeContent")
    Call<Result> likeContent(
            @Header("Authorization") String token,
            @Field("user_id") int user_id,
            @Field("content_id") int content_id,
            @Field("content_type") int content_type);

    //user unlike content
    @FormUrlEncoded
    @POST("unLikeContent")
    Call<Result> unLikeContent(
            @Header("Authorization") String token,
            @Field("user_id") int user_id,
            @Field("content_id") int content_id,
            @Field("content_type") int content_type);

    //update the user details
    @FormUrlEncoded
    @POST("updateUserDetails")
    Call<Result> updateUserDetails(
            @Header("Authorization") String token,
            @Field("user_id") int user_id,
            @Field("username") String username,
            @Field("email") String email);

    //save a profile pic added
    @Multipart
    @POST("updateProfilePic")
    Call<Result> saveProfilePic(
            @Header("Authorization") String token,
            @Part("user_id") int user_id,
            @Part MultipartBody.Part file,
            @Part("name") RequestBody name);

    //getting ads for browsing
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getAllTestimonies/{page}/{page_size}")
    Call<UserActions> getAllTestimonies(
            //@Header("Authorization") String token,
            @Path("page") int page,
            @Path("page_size") int page_size);

    //get all the prayer requests
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @GET("getAllPrayerRequests/{page}/{page_size}")
    Call<UserActions> getAllPrayerRequests(
            //@Header("Authorization") String token,
            @Path("page") int page,
            @Path("page_size") int page_size);
}