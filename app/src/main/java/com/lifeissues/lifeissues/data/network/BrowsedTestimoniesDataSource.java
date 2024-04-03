package com.lifeissues.lifeissues.data.network;

import static com.lifeissues.lifeissues.ui.fragments.TestimonyFragment.testimonyFragment;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.data.network.api.APIService;
import com.lifeissues.lifeissues.data.network.api.LocalRetrofitApi;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.models.UserActions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BrowsedTestimoniesDataSource extends PageKeyedDataSource<Integer, Testimony> {
    private static final String LOG_TAG = BrowsedTestimoniesDataSource.class.getSimpleName();
    //private final Context mContext;
    //private final AppExecutors mExecutors;

    //the size of a page that we want
    public static final int PAGE_SIZE = 10;

    //we will start from the first page which is 1
    //this is an index value, so 1 is at position 0
    private static final int FIRST_PAGE = 1;

    private static final Object LOCK = new Object();
    private static BrowsedTestimoniesDataSource sInstance;
    //private final Context mContext;
    //private final AppExecutors mExecutors;

    private List<Testimony> testimonyList = new ArrayList<Testimony>();
    // LiveData storing the latest downloaded jobs list
    private final MutableLiveData<List<Testimony>> mAdsForBrowsing;
    private SessionManager sessionManager;

    public BrowsedTestimoniesDataSource() {
        //mContext = context;
        //mExecutors = executors;
        mAdsForBrowsing = new MutableLiveData<>();
        // Session manager
        sessionManager = new SessionManager(AppController.getContext().getApplicationContext());
    }

    /**
     * Get the singleton for this class
     */
    public static BrowsedTestimoniesDataSource getInstance() {
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new BrowsedTestimoniesDataSource();
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    //returned jobs for browsing
    public LiveData<List<Testimony>> getAdsForBrowsing() {
        return mAdsForBrowsing;
    }

    /*
     * Step 1: This method is responsible to load the data initially
     * when app screen is launched for the first time.
     * We are fetching the first page data from the api
     * and passing it via the callback method to the UI.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Integer, Testimony> callback) {
        Log.d(LOG_TAG, "Loading initial Browse ads started");

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<UserActions> call = service.getAllTestimonies(FIRST_PAGE, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                try{
                    if (response.body() != null && response.body().getBrowsedTestimoniesList() != null) {
                        if (response.body().getBrowsedTestimoniesList().size() == 0) {
                            testimonyFragment.isListEmpty(true);
                        } else {
                            testimonyList = response.body().getBrowsedTestimoniesList();
                            //callback.onResult(response.body().getBrowsedJobsList(), null, PAGE_SIZE);
                            callback.onResult(testimonyList, null, FIRST_PAGE + 1);
                            testimonyFragment.isListEmpty(false);
                        }
                        mAdsForBrowsing.postValue(testimonyList);
                    }
                }catch (Exception e){
                    //mArticlesForBrowsing.postValue(null);
                    //browseAdvertsFragment.isListEmpty(true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                //browseAdvertsFragment.isListEmpty(true);
            }
        });
    }

    //this will load the previous page
    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Testimony> callback) {

        Log.d(LOG_TAG, "Loading previous Browse jobs list started");

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<UserActions> call = service.getAllTestimonies(params.key, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                //if the current page is greater than one
                //we are decrementing the page number
                //else there is no previous page
                Integer adjacentKey = (params.key > 1) ? params.key - 1 : null;
                //Integer adjacentKey = (params.key > 1) ? params.key - 1 : 0;

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null && response.body().getBrowsedTestimoniesList() != null) {
                    //Log.d(LOG_TAG, "Previous JSON not null");

                    //passing the loaded data
                    //and the previous page key
                    //callback.onResult(jobList, adjacentKey);
                    testimonyList = response.body().getBrowsedTestimoniesList();
                    callback.onResult(testimonyList, adjacentKey);
                    testimonyFragment.isListEmpty(false);

                }
                mAdsForBrowsing.postValue(testimonyList);
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                testimonyFragment.isListEmpty(true);
            }
        });
    }

    //this will load the next page
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Testimony> callback) {
        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<UserActions> call = service.getAllTestimonies(params.key, PAGE_SIZE);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<UserActions>() {
            @Override
            public void onResponse(Call<UserActions> call, Response<UserActions> response) {

                //if the current page is greater than one
                //we are incrementing the page number
                //else there is no previous page
                //Integer adjacentKey = (params.key > 1) ? params.key + 1 : null;

                //if response body is not null, we have some data
                //count what we have in the response
                if (response.body() != null) {
                    Log.d(LOG_TAG, "Next JSON not null");

                    Integer adjacentKey;
                    if (response.body().getPages_count() == params.key) {
                        adjacentKey = null;
                        //Log.e(LOG_TAG, " adjacentKey = null | Pages count from server is " + response.body().getPages_count() + " " +
                          //      "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        testimonyList = response.body().getBrowsedTestimoniesList();
                        callback.onResult(testimonyList, adjacentKey);
                        //callback.onResult(response.body().getBrowsedJobsList(), params.key + 1);
                        testimonyFragment.isListEmpty(false);

                    }else if (response.body().getPages_count() < params.key && response.body().getPages_count() != 0) {
                        adjacentKey = null;
                        //Log.e(LOG_TAG, " adjacentKey = null | Pages count from server is " + response.body().getPages_count() + " " +
                          //      "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        testimonyList = response.body().getBrowsedTestimoniesList();
                        callback.onResult(testimonyList, adjacentKey);
                        testimonyFragment.isListEmpty(false);

                    }else if (response.body().getPages_count() > params.key){
                        adjacentKey = params.key + 1;

                        //Integer adjacentKey = ((response.body().getPages_count() > params.key || response.body().getPages_count() != params.key)) ? params.key + 1 : null;

                        //Log.e(LOG_TAG, "Pages count from server is " + response.body().getPages_count() + " " +
                          //      "and current page count is " + params.key);

                        //passing the loaded data
                        //and the previous page key
                        testimonyList = response.body().getBrowsedTestimoniesList();
                        callback.onResult(testimonyList, adjacentKey);
                        testimonyFragment.isListEmpty(false);

                        //Log.d(LOG_TAG, "Next Size of list: "+response.body().getBrowsedJobsList().size());
                        // If the code reaches this point, we have successfully performed our sync
                        //Log.d(LOG_TAG, "Successfully got next list of jobs for browsing");
                    }

                    // When you are off of the main thread and want to update LiveData, use postValue.
                    // It posts the update to the main thread.
                    mAdsForBrowsing.postValue(testimonyList);
                }
            }

            @Override
            public void onFailure(Call<UserActions> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                testimonyFragment.isListEmpty(true);
            }
        });
    }
}
