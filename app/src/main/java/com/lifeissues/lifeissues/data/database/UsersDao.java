package com.lifeissues.lifeissues.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.lifeissues.lifeissues.models.User;

/**
 * Created by BE on 2/3/2018.
 */

@Dao
public interface UsersDao {

    /*
    insert user into db
     */
    @Insert
    void insertUser(User user);

    @Query("Delete from user")
    void deleteUser();

    @Query("SELECT * from user")
    //HashMap<String, String> getUserDetails();
    //Cursor getUserDetails();
    LiveData<User> getUserDetails();

    //update user details
    @Query("UPDATE user SET email = :email, created_at = :created_on," +
            "name = :name, profile_pic = :profile_pic WHERE user_id = :user_id")
    void updateProfile(int user_id, String email, String created_on,
                       String profile_pic, String name);
}
