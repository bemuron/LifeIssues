package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.models.User;

import java.util.HashMap;

public class LoginRegisterActivityViewModel extends AndroidViewModel {
    private static final String TAG = LoginRegisterActivityViewModel.class.getSimpleName();
    //private member variable to hold reference to the repository
    private LifeIssuesRepository mRepository;

    private HashMap<String, String> mUser;

    private Cursor userDetailsCursor;

    //constructor that gets a reference to the repository and gets the categories
    public LoginRegisterActivityViewModel(@NonNull Application application) {
        super(application);
        mRepository = new LifeIssuesRepository(application);
    }

    //call repository method to handle posting data to server
    public void loginUser(String email, String password){
        mRepository.loginFixAppUser(email, password);
    }

    //returning if login is successful or not
    public void OnSuccessfulLogin(Boolean isLoginSuccessful){

    }

    //call repository method to handle posting user reg details to server
    public void registerUser(String name, String email, String password){
        mRepository.registerFixAppUser(name, email, password);
    }

    public void insert(User user) { mRepository.insertUser(user); }

    public void delete() { mRepository.deleteUser();}
}
