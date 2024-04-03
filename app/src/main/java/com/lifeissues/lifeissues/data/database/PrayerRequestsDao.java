package com.lifeissues.lifeissues.data.database;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;

import java.util.List;

@Dao
public interface PrayerRequestsDao {
    /*
    insert prayer request into db
     */
    //@Insert
    //long insertPrayerRequest(PrayerRequest prayerRequest);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPrayerRequests(List<PrayerRequest> prayerRequests);

    @Query("Delete from prayer_requests")
    void deleteAll();

    //count all the prayer requests we have
    @Query("SELECT COUNT(prayer_id) FROM prayer_requests")
    int prayerRequestsNumber();

    @Query("SELECT prayer_id,category_id,prayer_status,prayer_title,prayers_received," +
            "is_prayed_for,profile_pic,posted_on,posted_by,poster_name,user_name,is_reported" +
            " FROM prayer_requests ORDER BY posted_on DESC")
    DataSource.Factory<Integer, PrayerRequest> getAllPrayerRequests();
}
