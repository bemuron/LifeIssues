package com.lifeissues.lifeissues.models;

import android.net.Uri;

import java.io.File;

public class ImageUpload {
  private int pic_id;
  private String image;
  private String image_name;
  private String title;
  private int resImg;
  private boolean isSelected;
  private Uri imagePath;
  private File imageFile;

  public int getPic_id() {
    return pic_id;
  }

  public void setPic_id(int pic_id) {
    this.pic_id = pic_id;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public String getImage_name() {
    return image_name;
  }

  public void setImage_name(String image_name) {
    this.image_name = image_name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getResImg() {
    return resImg;
  }

  public void setResImg(int resImg) {
    this.resImg = resImg;
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }

  public Uri getImagePath() {
    return imagePath;
  }

  public void setImagePath(Uri imagePath) {
    this.imagePath = imagePath;
  }

  public File getImageFile() {
    return imageFile;
  }

  public void setImageFile(File imageFile) {
    this.imageFile = imageFile;
  }
}
