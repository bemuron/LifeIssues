package com.lifeissues.lifeissues.data.database;

import androidx.room.Dao;
import com.lifeissues.lifeissues.models.Testimony;

import androidx.paging.DataSource;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TestimoniesDao {
    /*
    insert name into db
     */
    //@Insert
    //long insertName(Testimony testimony);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTestimonies(List<Testimony> testimonies);

    @Query("Delete from testimonies")
    void deleteAll();

    //count all the testimonies we have
    @Query("SELECT COUNT(testimony_id) FROM testimonies")
    int testimoniesNumber();

    @Query("SELECT testimony_id,category_id,testimony_name,content,image_name,posted_by,posted_on," +
            "is_liked,profile_pic,testimony_status,is_reported,testimony_likes,user_name FROM testimonies " +
            "ORDER BY posted_on DESC")
    DataSource.Factory<Integer, Testimony> getAllTestimonies();
}
