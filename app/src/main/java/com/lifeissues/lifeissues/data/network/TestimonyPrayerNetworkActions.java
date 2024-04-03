package com.lifeissues.lifeissues.data.network;

import static com.lifeissues.lifeissues.ui.activities.PostPrayerRequestActivity.postPrayerRequestActivity;
import static com.lifeissues.lifeissues.ui.activities.PostTestimonyActivity.postTestimonyActivity;
import static com.lifeissues.lifeissues.ui.activities.PrayerRequestDetailsActivity.prayerRequestDetailsActivity;
import static com.lifeissues.lifeissues.ui.activities.TestimonyDetailsActivity.testimonyDetailsActivity;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lifeissues.lifeissues.data.network.api.APIService;
import com.lifeissues.lifeissues.data.network.api.LocalRetrofitApi;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.ImageUpload;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.models.UserActions;
import com.lifeissues.lifeissues.ui.activities.PostPrayerRequestActivity;
import com.lifeissues.lifeissues.ui.activities.PostTestimonyActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestimonyPrayerNetworkActions {
    private static final String LOG_TAG = TestimonyPrayerNetworkActions.class.getSimpleName();

    private final AppExecutors mExecutors;

    private final MutableLiveData<List<ImageUpload>> mContentImages;
    private final MutableLiveData<List<ImageUpload>> mTestimonyImagesForEdit;
    private final MutableLiveData<List<Testimony>> mRecentTestimoniesList;
    private final MutableLiveData<List<PrayerRequest>> mRecentRequestsList;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static TestimonyPrayerNetworkActions sInstance;
    private ImageUpload imageEdit, imageDetail;
    private final Context mContext;
    private List<ImageUpload> contentImagesList = new ArrayList<ImageUpload>();
    private List<ImageUpload> imagesList = new ArrayList<ImageUpload>();
    private SessionManager sessionManager;

    public TestimonyPrayerNetworkActions(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mContentImages = new MutableLiveData<>();
        mTestimonyImagesForEdit = new MutableLiveData<>();
        mRecentTestimoniesList = new MutableLiveData<>();
        mRecentRequestsList = new MutableLiveData<>();
        // Session manager
        sessionManager = new SessionManager(context.getApplicationContext());
    }

    /**
     * Get the singleton for this class
     */
    public static TestimonyPrayerNetworkActions getInstance(Context context, AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new TestimonyPrayerNetworkActions(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public LiveData<List<Testimony>> getRecentPostedTestimonies() {
        return mRecentTestimoniesList;
    }

    public LiveData<List<PrayerRequest>> getRecentPostedPrayerRequests() {
        return mRecentRequestsList;
    }

    public LiveData<List<ImageUpload>> getTestimonyImagesForEdit() {
        return mTestimonyImagesForEdit;
    }

    //returned list of job images
    public LiveData<List<ImageUpload>> getContentPicsDetails() {
        return mContentImages;
    }

    public void GetContentImagesForEdit(int content_id, int contentType) {
        Log.d(LOG_TAG, "Fetch testimony images for edit started");

        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<UserActions> call = service.getContentImages(content_id,contentType);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getContentPics() != null) {
                        //Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (contentImagesList != null) {
                            contentImagesList.clear();
                        }

                        for (int i = 0; i < response.body().getContentPics().size(); i++) {
                            imageEdit = new ImageUpload();
                            imageEdit.setImage_name(response.body().getContentPics().get(i).getImage_name());
                            imageEdit.setPic_id(response.body().getContentPics().get(i).getPic_id());

                            contentImagesList.add(imageEdit);
                        }

                        if (contentType == 1){
                            postTestimonyActivity.onTestimonyImagesGot(true,
                                    contentImagesList);
                        }else{
                            postPrayerRequestActivity.onPrayerRequestImagesGot(true,
                                    contentImagesList);
                        }
                    }
                }catch (Exception e){
                    if (contentType == 1){
                        postTestimonyActivity.onTestimonyImagesGot(false,
                                null);
                    }else{
                        postPrayerRequestActivity.onPrayerRequestImagesGot(false,
                                null);
                    }
                    //Log.e(LOG_TAG,"Could not get jobs images for job id " +job_id);
                    //e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                if (contentType == 1){
                    postTestimonyActivity.onTestimonyImagesGot(false,
                            null);
                }else{
                    postPrayerRequestActivity.onPrayerRequestImagesGot(false,
                            null);
                }
            }
        });
    }

    //getting the images for a specific testimony or prayer request for the details page
    public void GetContentPicsDetails(int content_id) {

        //Defining retrofit com.emtech.retrofitexample.api service
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<UserActions> call = service.getContentImagesDetails(content_id);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                try {
                    //if response body is not null, we have some data
                    //count what we have in the response
                    if (response.body() != null && response.body().getContentPics() != null) {
                        Log.d(LOG_TAG, "JSON not null");

                        //clear the previous list if it has content
                        if (imagesList != null) {
                            imagesList.clear();
                        }

                        for (int i = 0; i < response.body().getContentPics().size(); i++) {
                            imageDetail = new ImageUpload();
                            imageDetail.setImage_name(response.body().getContentPics().get(i).getImage_name());

                            imagesList.add(imageDetail);
                        }

                        // When you are off of the main thread and want to update LiveData, use postValue.
                        // It posts the update to the main thread.
                        mContentImages.postValue(imagesList);

                    }
                }catch (Exception e){
                    //Log.e(LOG_TAG,"Could not get jobs images for job id " +job_id);
                    //e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });
    }

    //delete the pic
    public void deleteContentPic(int pic_id){

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.deleteAdPic(token, pic_id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try{
                    if (response.body() != null && !response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //Log.e(LOG_TAG, "nin pic deleted");
                        //send data to parent activity
                        if (postTestimonyActivity != null){
                            postTestimonyActivity.onTestimonyPicDeleted(true,
                                    response.body().getMessage());
                        }else{
                            postPrayerRequestActivity.onPrayerRequestPicDeleted(true,
                                    response.body().getMessage());
                        }
                    }else{
                        //Log.e(LOG_TAG, "nin pic NOT deleted");
                        //send data to parent activity
                        if (postTestimonyActivity != null){
                            postTestimonyActivity.onTestimonyPicDeleted(false,
                                    "Ad image not removed");
                        }else{
                            postPrayerRequestActivity.onPrayerRequestPicDeleted(false,
                                    "Ad image not removed");
                        }
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                    //Log.e(LOG_TAG, "nin pic NOT deleted");
                    //send data to parent activity
                    if (postTestimonyActivity != null){
                        postTestimonyActivity.onTestimonyPicDeleted(false,
                                "Ad image not removed");
                    }else{
                        postPrayerRequestActivity.onPrayerRequestPicDeleted(false,
                                "Ad image not removed");
                    }
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //Log.e(LOG_TAG, t.getMessage());
                if (postTestimonyActivity != null){
                    postTestimonyActivity.onTestimonyPicDeleted(false,
                            "Sorry could not remove the ad pic now");
                }else{
                    postPrayerRequestActivity.onPrayerRequestPicDeleted(false,
                            "Sorry could not remove the ad pic now");
                }
            }
        });
    }

    public void postNewContent(String title, String description, ArrayList<File> fileArrayList,
                               int categoryId, int userId, int contentType,
                               PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity){

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[fileArrayList.size()];

        try {
            for (int pos = 0; pos < fileArrayList.size(); pos++) {
                //parsing any media file
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), fileArrayList.get(pos));

                fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                        fileArrayList.get(pos).getName(), requestBody);
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"Error when parsing image list array");
            e.printStackTrace();
        }

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.postNewContent(token, title, description, userId,
                contentType, categoryId, fileToUpload, fileToUpload.length);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (response.body() != null && !response.body().getError()) {
                    //send response data to postjobactivity
                    //success
                    if (contentType == 1){
                        postTestimonyActivity.onTestimonyPostUpdated(true,
                                response.body().getMessage());
                    }else{
                        prayerRequestActivity.onPrayerRequestUpdated(true,
                                response.body().getMessage());
                    }
                }else{
                    if (contentType == 1){
                        postTestimonyActivity.onTestimonyPostUpdated(false,
                                "Oops error occurred: Could not post Testimony");
                    }else{
                        prayerRequestActivity.onPrayerRequestUpdated(false,
                                "Oops error occurred: Could not post Prayer Request");
                    }
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                if (contentType == 1){
                    postTestimonyActivity.onTestimonyPostUpdated(false,
                            "Oops error occurred: Could not post Testimony");
                }else{
                    prayerRequestActivity.onPrayerRequestUpdated(false,
                            "Oops error occurred: Could not post Prayer Request");
                }
            }
        });
    }

    public void editContent(int contentId, String title, String description, ArrayList<File> fileArrayList,
                            int categoryId, int userId, int contentType,
                            PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity){

        MultipartBody.Part[] fileToUpload = new MultipartBody.Part[fileArrayList.size()];

        try {
            for (int pos = 0; pos < fileArrayList.size(); pos++) {
                //parsing any media file
                RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), fileArrayList.get(pos));

                fileToUpload[pos] = MultipartBody.Part.createFormData("file[]",
                        fileArrayList.get(pos).getName(), requestBody);
            }
        }catch (Exception e){
            Log.e(LOG_TAG,"Error when parsing image list array");
            e.printStackTrace();
        }

        //Defining retrofit api service*/
        //APIService service = retrofit.create(APIService.class);
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.editContent(token,contentId, title, description, userId,
                contentType, categoryId, fileToUpload, fileToUpload.length);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                if (response.body() != null && !response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());
                    //send response data to postjobactivity
                    //success
                    if (contentType == 1){
                        postTestimonyActivity.onTestimonyPostUpdated(true,
                                response.body().getMessage());
                    }else{
                        prayerRequestActivity.onPrayerRequestUpdated(true,
                                response.body().getMessage());
                    }
                }else{
                    if (contentType == 1){
                        postTestimonyActivity.onTestimonyPostUpdated(false,
                                "Oops error occurred: Could not post Testimony");
                    }else{
                        prayerRequestActivity.onPrayerRequestUpdated(false,
                                "Oops error occurred: Could not post Prayer Request");
                    }
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                if (contentType == 1){
                    postTestimonyActivity.onTestimonyPostUpdated(false,
                            "Oops error occurred: Could not post Testimony");
                }else{
                    prayerRequestActivity.onPrayerRequestUpdated(false,
                            "Oops error occurred: Could not post Prayer Request");
                }
            }
        });
    }

    //user content
    public void reportContent(int userId, int content_id, String comment, int content_type){
        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.reportContent(token, userId, content_id, comment,content_type);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (response.body() != null && !response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());

                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.testimonyReportedResponse(true,
                                    response.body().getMessage());
                        }else{
                            prayerRequestDetailsActivity.prayerReportedResponse(true,
                                    response.body().getMessage());
                        }

                    }else{
                        Log.e(LOG_TAG, "Error: Ad not reported at reportAd()");
                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.testimonyReportedResponse(false,
                                    "Failed to report testimony");
                        }else{
                            prayerRequestDetailsActivity.prayerReportedResponse(false,
                                    "Failed to report prayer request");
                        }
                    }
                }catch (Exception e){
                    if (content_type == 1){
                        testimonyDetailsActivity.testimonyReportedResponse(false,
                                "Failed to report testimony");
                    }else{
                        prayerRequestDetailsActivity.prayerReportedResponse(false,
                                "Failed to report prayer request");
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                if (content_type == 1){
                    testimonyDetailsActivity.testimonyReportedResponse(false,
                            "Something went wrong. Please try again later");
                }else{
                    prayerRequestDetailsActivity.prayerReportedResponse(false,
                            "Something went wrong. Please try again later");
                }
            }
        });
    }

    public void deleteContent(int userId, int content_id, int content_type){
        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.deleteContent(token, userId, content_id, content_type);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (response.body() != null && !response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());

                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.testimonyDeletedResponse(true,
                                    response.body().getMessage());
                        }else{
                            prayerRequestDetailsActivity.requestDeletedResponse(true,
                                    response.body().getMessage());
                        }

                    }else{
                        Log.e(LOG_TAG, "Error: Ad not deleted at deleteAd()");
                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.testimonyDeletedResponse(false,
                                    "Failed to delete Ad");
                        }else{
                            prayerRequestDetailsActivity.requestDeletedResponse(false,
                                    "Failed to delete Ad");
                        }
                    }
                }catch (Exception e){
                    if (content_type == 1){
                        testimonyDetailsActivity.testimonyDeletedResponse(false,
                                "Failed to delete Ad");
                    }else{
                        prayerRequestDetailsActivity.requestDeletedResponse(false,
                                "Failed to delete Ad");
                    }

                    //Log.d(LOG_TAG, "Error: Task not canceled at cancelTask()");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                if (content_type == 1){
                    testimonyDetailsActivity.testimonyDeletedResponse(false,
                            "Something went wrong. Please try again later");
                }else{
                    prayerRequestDetailsActivity.requestDeletedResponse(false,
                            "Something went wrong. Please try again later");
                }
            }
        });
    }

    public void unLikeContent(int userId, int content_id, int content_type){

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.unLikeContent(token, userId, content_id, content_type);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (response.body() != null && !response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        if (content_type == 1){
                            testimonyDetailsActivity.onTestimonyLike(false,
                                    response.body().getMessage());
                        }else{
                            prayerRequestDetailsActivity.onPrayerRequestLike(false,
                                    response.body().getMessage());
                        }

                    }else{
                        Log.e(LOG_TAG, "Error: Article not unliked at likeArticle()");
                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.onTestimonyLike(true,
                                    "Failed to perform action");
                        }else{
                            prayerRequestDetailsActivity.onPrayerRequestLike(true,
                                    "Failed to perform action");
                        }
                    }
                }catch (Exception e){
                    if (content_type == 1){
                        testimonyDetailsActivity.onTestimonyLike(true,
                                "Failed to perform action");
                    }else{
                        prayerRequestDetailsActivity.onPrayerRequestLike(true,
                                "Failed to perform action");
                    }
                    Log.d(LOG_TAG, "Error: Article not unliked at likeArticle()");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                if (content_type == 1){
                    testimonyDetailsActivity.onTestimonyLike(true,
                            "Failed to perform action");
                }else{
                    prayerRequestDetailsActivity.onPrayerRequestLike(true,
                            "Failed to perform action");
                }
            }
        });
    }

    //save user like content
    public void likeContent(int userId, int content_id, int content_type){

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.likeContent(token, userId, content_id, content_type);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (response.body() != null && !response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());

                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.onTestimonyLike(true,
                                    response.body().getMessage());
                        }else{
                            prayerRequestDetailsActivity.onPrayerRequestLike(true,
                                    response.body().getMessage());
                        }

                    }else{
                        //send data to parent activity
                        if (content_type == 1){
                            testimonyDetailsActivity.onTestimonyLike(false,
                                    "Failed to perform action");
                        }else{
                            prayerRequestDetailsActivity.onPrayerRequestLike(false,
                                    "Failed to perform action");
                        }
                    }
                }catch (Exception e){
                    if (content_type == 1){
                        testimonyDetailsActivity.onTestimonyLike(false,
                                "Failed to perform action");
                    }else{
                        prayerRequestDetailsActivity.onPrayerRequestLike(false,
                                "Failed to perform action");
                    }
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                if (content_type == 1){
                    testimonyDetailsActivity.onTestimonyLike(false,
                            "Failed to perform action");
                }else{
                    prayerRequestDetailsActivity.onPrayerRequestLike(false,
                            "Failed to perform action");
                }
            }
        });
    }

    //get the recently posted testimonies
    public void GetRecentTestimonies() {
        Log.d(LOG_TAG, "Fetch recent testimonies started");
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserActions> call = service.getAllTestimonies(1, 10);

        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null && response.body().getBrowsedTestimoniesList().size() > 0) {
                    //Log.d(LOG_TAG, "JSON not null and has " + response.body().getBrowsedTestimoniesList().size()
                      //      + " values");

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mRecentTestimoniesList.postValue(response.body().getBrowsedTestimoniesList());

                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, "Could not get recent testimonies");
                //Log.e(LOG_TAG, "featured content error msg = "+t.getMessage());
            }
        });
    }

    //get the recently posted prayer requests
    public void GetRecentRequests() {
        Log.d(LOG_TAG, "Fetch recent prayer requests started");
        APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<UserActions> call = service.getAllPrayerRequests(1, 10);

        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null && response.body().getBrowsedRequestsList().size() > 0) {
                    //Log.d(LOG_TAG, "JSON not null and has " + response.body().getBrowsedRequestsList().size()
                     //       + " values");

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mRecentRequestsList.postValue(response.body().getBrowsedRequestsList());

                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, "Could not get recent prayer requests");
                //Log.e(LOG_TAG, "featured content error msg = "+t.getMessage());
            }
        });
    }

}
