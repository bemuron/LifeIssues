package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.activities.MyProfileActivity;

import java.io.File;

public class UserProfileActivityViewModel extends AndroidViewModel {

  //private member variable to hold reference to the repository
  private LifeIssuesRepository mRepository;

  //private LiveData member variable to cache the user profile details
  private LiveData<User> mUserDetails;

  //constructor that gets a reference to the repository and gets the categories
  public UserProfileActivityViewModel(@NonNull Application application) {
    super(application);
    mRepository = new LifeIssuesRepository(application);
    //mUserDetails = mRepository.getUserDetails();
  }

  //a getter method for all the user details. This hides the implementation from the UI
  public LiveData<User> getUserDetails(){
    return mUserDetails;
  }

  //deletes user from sqlite db - room
  public void delete() { mRepository.deleteUser();}

  //updates user profile in room db
  public void updateProfile(int user_id, String email, String created_on,
                            String profile_pic,
                            String name){
    mRepository.updateProfile(user_id, email, created_on, profile_pic, name);
  }

  //update user details
  public void updateUserProfile(int user_id, String username, String email,
                                MyProfileActivity activity){
    mRepository.updateUserProfile(user_id, username,email, activity);
  }

  //save the user's profile pic
  public void saveProfilePic(int user_id,File profilePic, MyProfileActivity activity){
    mRepository.saveProfilePic(user_id,profilePic,activity);
  }

  //log out the user
  public void logOutUser(int user_id){
    mRepository.logOutUser(user_id);
  }

  //get a visitor's profile details
  /*public void getVisitorDetails(int user_id, MyProfileActivity activity){
    mRepository.getVisitorProfile(user_id, activity);
  }*/



}
