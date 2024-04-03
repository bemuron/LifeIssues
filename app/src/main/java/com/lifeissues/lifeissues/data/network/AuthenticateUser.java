package com.lifeissues.lifeissues.data.network;

import static com.lifeissues.lifeissues.ui.activities.LoginActivity.loginActivityInstance;
import static com.lifeissues.lifeissues.ui.activities.MainActivity.mainActivity;

import android.content.Context;
import android.util.Log;

import com.lifeissues.lifeissues.data.network.api.APIService;
import com.lifeissues.lifeissues.data.network.api.APIUrl;
import com.lifeissues.lifeissues.data.network.api.LocalRetrofitApi;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.helpers.SessionManager;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.activities.LoginActivity;
import com.lifeissues.lifeissues.ui.activities.MyProfileActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AuthenticateUser {
    private static final String LOG_TAG = AuthenticateUser.class.getSimpleName();

    private final AppExecutors mExecutors;
    SuccessfulLoginCallBack successfulLoginCallBack;
    ProfileUpdatedCallBack profileUpdatedCallBack;

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static AuthenticateUser sInstance;

    private final Context mContext;
    private User mFixappUser;
    public static LoginActivity loginActivity;

    private SessionManager sessionManager;

    public AuthenticateUser(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        // Session manager
        sessionManager = new SessionManager(context.getApplicationContext());

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            successfulLoginCallBack = loginActivity.getInstance();
        } catch (ClassCastException e) {
            Log.d(LOG_TAG, e.getMessage());
            throw new ClassCastException(context.toString()
                    + " must implement onLoginSuccessful");
        }

    }

    /**
     * Get the singleton for this class
     */
    public static AuthenticateUser getInstance(Context context, AppExecutors executors) {
        //successfulLoginCallBack = loginActivity.getInstance();
        Log.d(LOG_TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new AuthenticateUser(context.getApplicationContext(), executors);
                Log.d(LOG_TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public void UserRegister(String name, String email, String password) {
        Log.d(LOG_TAG, "Register user started");

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging)
                .connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = retrofit.create(APIService.class);
        //APIService service = new LocalRetrofitApi().getRetrofitService();

        //defining the call
        Call<Result> call = service.createUser(name, email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();

                //if response body is not null, we have some data
                //count what we have in the response
                if (!response.body().getError()) {
                    Log.d(LOG_TAG, response.body().getMessage());

                    // If the code reaches this point, we have successfully registered
                    Log.d(LOG_TAG, "Successful registration");

                    //registerResponse = true;
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
            }
        });

        //return registerResponse;
    }

    public void UserLogIn(String email, String password) {
        Log.d(LOG_TAG, "User login started");
        successfulLoginCallBack = loginActivityInstance;

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(logging)
                .connectTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS);

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(APIUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit com.emtech.retrofitexample.api service
        APIService service = retrofit.create(APIService.class);

        //defining the call
        Call<Result> call = service.userLogin(email, password);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                //if response body is not null, we have some data
                if (response.body() != null) {
                    //count what we have in the response
                    if (!response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //response.body().getUser();

                        // If the code reaches this point, we have successfully logged in
                        //Log.d(LOG_TAG, "Successful login");

                        //create new user object
                        mFixappUser = new User();
                        mFixappUser.setUser_id(response.body().getUser().getUser_id());
                        mFixappUser.setEmail(response.body().getUser().getEmail());
                        mFixappUser.setCreated_at(response.body().getUser().getCreated_at());
                        mFixappUser.setProfile_pic(response.body().getUser().getProfile_pic());
                        mFixappUser.setName(response.body().getUser().getName());
                        mFixappUser.setAccess_token(response.body().getAccess_token());
                        //Log.d(LOG_TAG, mFixappUser.getEmail() + " user email");

                        //insert user to the local db
                        //mRepository.insertUser(mFixappUser);

                        successfulLoginCallBack.onLoginSuccessful(true, mFixappUser, "successful login");
                    }else{
                        //Log.e(LOG_TAG, "we have an error logging in");
                        successfulLoginCallBack.onLoginSuccessful(false, null, "An error occurred logging in. Try again");
                    }
                }else{
                    //Log.e(LOG_TAG, "response.body() is null");
                    successfulLoginCallBack.onLoginSuccessful(false, null, "Invalid login details");
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                //Log.e(LOG_TAG, t.getMessage());
                successfulLoginCallBack.onLoginSuccessful(false, null, "Login failure");
            }
        });
    }

    //retrofit call to logout the user
    //deletes the user's fcm and access tokens from the db
    public void UserLogout(int user_id){
        APIService service = new LocalRetrofitApi().getRetrofitService();
        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.userLogout(token, user_id);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                //if response body is not null, we have some data
                //successful addition
                if (response.body() != null && !response.body().getError()) {
                    //Log.e(LOG_TAG, "logged out successfully");
                    //send response data to the activity
                    //success
                    mainActivity.logOutUser(true,
                            response.body().getMessage());
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //Log.e(LOG_TAG, t.getMessage());
                mainActivity.logOutUser(true,
                        "Sorry, log out failed");
            }
        });
    }

    //retrofit call to update user details
    //update user details
    public void updateUserDetails(int user_id, String username, String email,
                                  MyProfileActivity activity){
        profileUpdatedCallBack = activity;

        //Defining retrofit api service*/
        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.updateUserDetails(token, user_id, username, email);

        //calling the com.emtech.retrofitexample.api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                try{
                    if (!response.body().getError()) {
                        Log.d(LOG_TAG, response.body().getMessage());
                        Log.e(LOG_TAG, "User details updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePosted(true, response.body().getMessage());
                    }else{
                        Log.e(LOG_TAG, "User details NOT updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePosted(false, "User details NOT updated");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(LOG_TAG, "User details NOT updated - catch block");
                    //send data to parent activity
                    profileUpdatedCallBack.onProfilePosted(false, e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                //print out any error we may get
                //probably server connection
                Log.e(LOG_TAG, t.getMessage());
                Log.e(LOG_TAG, "User details NOT updated - onFailure block");
                //send data to parent activity
                profileUpdatedCallBack.onProfilePosted(false,
                        "Something went wrong, profile not updated");
            }
        });
    }

    //save the profile pic the user has added
    public void saveProfilePic(int user_id, File profilePic, MyProfileActivity activity){
        profileUpdatedCallBack = activity;

        RequestBody requestBody = RequestBody.create(MediaType.parse("*image/*"), profilePic);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", profilePic.getName(), requestBody);
        RequestBody fileName = RequestBody.create(MediaType.parse("text/plain"), profilePic.getName());

        APIService service = new LocalRetrofitApi().getRetrofitService();

        String token = "Bearer "+sessionManager.getUserToken();

        //defining the call
        Call<Result> call = service.saveProfilePic(token, user_id, fileToUpload, fileName);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try{
                    if (!response.body().getError()) {
                        //Log.d(LOG_TAG, response.body().getMessage());
                        //Log.e(LOG_TAG, "Profile pic updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePicSaved(true, response.body().getMessage());
                    }else{
                        //Log.e(LOG_TAG, "Profile pic NOT updated");
                        //send data to parent activity
                        profileUpdatedCallBack.onProfilePicSaved(false, "Profile pic not updated");
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                    //Log.e(LOG_TAG, "Profile pic NOT updated");
                    //send data to parent activity
                    profileUpdatedCallBack.onProfilePicSaved(false, "Profile pic not updated");
                }
            }
            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.e(LOG_TAG, t.getMessage());
                profileUpdatedCallBack.onProfilePicSaved(false,
                        "Sorry could not save the profile pic now");
            }
        });
    }

    /**
     * The interface that receives whether the login was successful or not
     */
    public interface SuccessfulLoginCallBack {
        void onLoginSuccessful(Boolean isLoginSuccessful, User user, String null_response_received);
    }

    //receive status of profile save
    public interface ProfileUpdatedCallBack {
        void onProfilePosted(Boolean isProfileSaved, String message);
        void onProfilePicSaved(Boolean isPicSaved, String message);
    }

}
