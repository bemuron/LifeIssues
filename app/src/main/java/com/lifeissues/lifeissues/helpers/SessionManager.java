package com.lifeissues.lifeissues.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import java.util.HashMap;

/*
 *This class maintains session data across the app using shared prefs.
 * We store a boolean flag isLoggedIn in shared prefs to check the login status
 *
 */
public class SessionManager {
  // LogCat tag
  private static String TAG = SessionManager.class.getSimpleName();

  // Shared Preferences
  SharedPreferences pref;

  Editor editor;
  Context _context;

  // Shared pref mode
  int PRIVATE_MODE = 0;

  // Shared preferences file name
  private static final String PREF_NAME = "FIxAppUserPref";

  private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

  private static final String TAG_TOKEN = "tagtoken";

  private static final String KEY_NOTIFICATIONS = "notifications";

  private static final String USER_TOKEN = "user_token";

  //user id
  public static final String KEY_USER_ID = "userId";

  //username
  public static final String KEY_NAME = "name";

  //email
  public static final String KEY_EMAIL = "email";

  //dob
  public static final String KEY_DOB = "dob";

  //gender
  public static final String KEY_GENDER = "gender";

  //user location
  public static final String KEY_USER_LOCATION = "location";

  //user registration date
  public static final String KEY_REG_DATE = "created_on";

  //user role (can be default - 0 or 1 - fixer)
  public static final String KEY_ROLE = "role";

  //phone number
  public static final String KEY_PHONE_NUMBER = "phone_number";

  //payment phone number
  public static final String KEY_PAY_PHONE_NUMBER = "pay_phone_number";
  public static final String KEY_IS_PAY_PHONE_VERIFIED = "is_pay_phone_verified";

  //user profile pic
  public static final String KEY_PROFILE_PIC = "profile_pic";

  //user profile pic
  public static final String KEY_USER_DESC = "user_description";

  private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";


  public SessionManager(Context context) {
    this._context = context;
    pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    editor = pref.edit();
  }

  //create login session
  public void createLoginSession(int user_id, String name, String email,
                                 String profile_pic, String reg_date, String accessToken){
    // Storing login value as TRUE
    editor.putBoolean(KEY_IS_LOGGED_IN, true);

    // Storing user id in pref
    editor.putInt(KEY_USER_ID, user_id);

    // Storing name in pref
    editor.putString(KEY_NAME, name);

    // Storing email in pref
    editor.putString(KEY_EMAIL, email);

    // Storing profile pic in pref
    editor.putString(KEY_PROFILE_PIC, profile_pic);

    // Storing registration date in pref
    editor.putString(KEY_REG_DATE, reg_date);

    // Storing user access token
    editor.putString(USER_TOKEN, accessToken);

    // commit changes
    editor.commit();
  }

  //save the user's payment phone number
  public void savePaymentNumber(String phoneNumber, Boolean is_verified){
    editor.putString(KEY_PAY_PHONE_NUMBER, phoneNumber);
    editor.putBoolean(KEY_IS_PAY_PHONE_VERIFIED, is_verified);
    // commit changes
    editor.commit();

  }

  public boolean isPayPhoneVerified(){
    return pref.getBoolean(KEY_IS_PAY_PHONE_VERIFIED, false);
  }

  /**
   * Check login method wil check user login status
   * If false it will redirect user to login page
   * Else won't do anything
   * */
  public void checkLogin(){
    // Check login status
    if(!this.isLoggedIn()){
      // user is not logged in redirect him to Login Activity
      /*Intent i = new Intent(_context, LoginActivity.class);
      // Closing all the Activities
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      // Add new Flag to start new Activity
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      // Staring Login Activity
      _context.startActivity(i);*/
    }

  }

  public void initialiseNotificationPrefs(boolean emailInMsgs, boolean emailAllTasks,
                                          boolean emailPrefTasks, boolean pushInMsgs, boolean pushAllTasks,
                                          boolean pushPrefTasks){

  }

  /**
   * Get stored session data
   * */
  public HashMap<String, String> getUserDetails(){
    HashMap<String, String> user = new HashMap<String, String>();
    // user name
    user.put(KEY_NAME, pref.getString(KEY_NAME, null));

    // user email
    user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

    // user phone number
    user.put(KEY_PHONE_NUMBER, pref.getString(KEY_PHONE_NUMBER, null));

    // user phone number
    user.put(KEY_PAY_PHONE_NUMBER, pref.getString(KEY_PAY_PHONE_NUMBER, null));

    // user profile pic
    user.put(KEY_PROFILE_PIC, pref.getString(KEY_PROFILE_PIC, null));

    // user description
    user.put(KEY_USER_DESC, pref.getString(KEY_USER_DESC, null));

    // user registration date
    user.put(KEY_REG_DATE, pref.getString(KEY_REG_DATE, null));

    // user gender
    user.put(KEY_GENDER, pref.getString(KEY_GENDER, null));

    // user dob
    user.put(KEY_DOB, pref.getString(KEY_DOB, null));

    // user location
    user.put(KEY_USER_LOCATION, pref.getString(KEY_USER_LOCATION, null));

    // return user
    return user;
  }

  //update login session details when user edits profile
  public void updateLoginSession(int user_id, String name, String email){
    // Storing login value as TRUE
    editor.putBoolean(KEY_IS_LOGGED_IN, true);

    // Storing name in pref
    editor.putString(KEY_NAME, name);

    // Storing email in pref
    editor.putString(KEY_EMAIL, email);

    // Storing user id in pref
    editor.putInt(KEY_USER_ID, user_id);

    // commit changes
    editor.commit();
  }

  //save the name of the new profile pic
  //update login session details when user edits profile
  public void saveProfilePicName(String profilePicName){
    // Storing user profile pic
    editor.putString(KEY_PROFILE_PIC, profilePicName);

    // commit changes
    editor.commit();
  }

  /**
   * Clear session details
   * */
  public void logoutUser(){
    // Clearing all data from Shared Preferences
    editor.clear();
    editor.commit();

    // After logout redirect user to Login Activity
    /*Intent i = new Intent(_context, LoginActivity.class);
    // Closing all the Activities
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

    // Add new Flag to start new Activity
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    // Starting Login Activity
    _context.startActivity(i);*/
  }

  //get user id
  public int  getUserId(){
    return pref.getInt(KEY_USER_ID, 0);
  }

  //get user role
  public int getUserRole(){
    return pref.getInt(KEY_ROLE, 0);
  }

  //clear all the data in the prefs
  public void clearPrefs(){
    // Clearing all data from Shared Preferences
    editor.clear();
    editor.apply();
  }

  /**
   * Quick check for login
   * **/
  // Get Login State
  public boolean isLoggedIn(){
    return pref.getBoolean(KEY_IS_LOGGED_IN, false);
  }

  public void setLogin(boolean isLoggedIn) {

    editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);

    // commit changes
    editor.commit();

    Log.d(TAG, "User login session modified!");
  }

  //this method will fetch the user token from shared preferences
  public String getUserToken(){
    return  pref.getString(USER_TOKEN, null);
  }

  public void setFirstTimeLaunch(boolean isFirstTime) {
    editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
    editor.commit();
  }

  public String getUserFirstName(){
    String fullName = pref.getString(KEY_NAME, null);
    return  fullName.split(" ")[0];
  }

  public boolean isFirstTimeLaunch() {
    return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
  }

  public void addNotification(String notification) {

    // get old notifications
    String oldNotifications = getNotifications();

    if (oldNotifications != null) {
      oldNotifications += "|" + notification;
    } else {
      oldNotifications = notification;
    }

    editor.putString(KEY_NOTIFICATIONS, oldNotifications);
    editor.commit();
  }

  //this method will save the device token to shared preferences
  public boolean saveDeviceToken(String token){
    editor.putString(TAG_TOKEN, token);
    editor.apply();
    return true;
  }

  //this method will fetch the device token from shared preferences
  public String getDeviceToken(){
    return  pref.getString(TAG_TOKEN, null);
  }

  public String getNotifications() {
    return pref.getString(KEY_NOTIFICATIONS, null);
  }

}
