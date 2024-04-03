package com.lifeissues.lifeissues.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public final class User {

  // Login Table Columns names
  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_UID = "user_id";
  private static final String KEY_PROFILE_PIC = "profile_pic";
  private static final String KEY_PASSWORD = "password";
  private static final String KEY_CREATED_ON = "created_at";

  @PrimaryKey
  @ColumnInfo(name = KEY_UID)
  private int user_id;

  //@NonNull
  @ColumnInfo(name = KEY_NAME)
  private String name;

  @ColumnInfo(name = KEY_EMAIL)
  private String email;

  @ColumnInfo(name = KEY_PROFILE_PIC)
  private String profile_pic;

  @Ignore
  private String password;

  @ColumnInfo(name = KEY_CREATED_ON)
  private String created_at;

  //is users account active or deactivated
  @Ignore
  private int is_account_active;

  @Ignore
  private String access_token;

  public void setUser_id(int user_id) {
    this.user_id = user_id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }

  public void setProfile_pic(String profile_pic) {
    this.profile_pic = profile_pic;
  }

  public int getUser_id() {
    return user_id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword(){
    return password;
  }

  public String getCreated_at() {
    return created_at;
  }

  public String getProfile_pic() {
    return profile_pic;
  }

  public int getIs_account_active() {
    return is_account_active;
  }

  public void setIs_account_active(int is_account_active) {
    this.is_account_active = is_account_active;
  }

  public String getAccess_token() {
    return access_token;
  }

  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }
}
