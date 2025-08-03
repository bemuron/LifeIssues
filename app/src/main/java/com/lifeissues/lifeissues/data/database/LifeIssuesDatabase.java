package com.lifeissues.lifeissues.data.database;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.DailyVerse;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.models.IssueVerse;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.models.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Increased version from 1 - 2 to migrate from db v1(sqlite) to v2(room)
 * Increased version from 2 - 3 to cater for new db structure
 * Increased version from 3 - 4 to add the user table
 * Increased version from 4 - 5 to modify daily verses table, removing _id, making notify_date primary key
 * */
@Database(entities = {BibleVerse.class, DailyVerse.class,
        Issue.class, IssueVerse.class, Testimony.class, PrayerRequest.class,
        User.class},
        version = 5, exportSchema = false)
public abstract class LifeIssuesDatabase extends RoomDatabase {
    private static final String TAG = LifeIssuesDatabase.class.getSimpleName();

    private static LifeIssuesDatabase INSTANCE;

    public abstract BibleVersesDao bibleVersesDao();
    public abstract IssuesDao issuesDao();
    public abstract IssuesVersesDao issuesVersesDao();
    public abstract DailyVersesDao dailyVersesDao();
    public abstract TestimoniesDao testimoniesDao();
    public abstract UsersDao usersDao();
    public abstract PrayerRequestsDao prayerRequestsDao();
    //private Context mContext;
    // For Singleton instantiation
    private static final Object LOCK = new Object();

    public static LifeIssuesDatabase getDatabase (final Context context){
        if (INSTANCE == null){
            synchronized (LOCK){
                if (INSTANCE == null){
                    //delete db
                    //context.deleteDatabase("Life_Issues.db");
                    //create db here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LifeIssuesDatabase.class, "Life_Issues.db")
                            .createFromAsset("databases/Life_Issues.db")
                            //.addCallback(sRoomDatabaseCallback)
                            //.createFromAsset("databases/Life_Issues.db")
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)

                            //.fallbackToDestructiveMigrationFrom()// used if we dnt want to provide migrations
                            /**
                             * uncomment during production release
                             */
                            //.fallbackToDestructiveMigrationFrom()// used if we dnt want to provide migrations
                            //and specifically want the db to be cleared
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    //populate bible_names table when the app is started.
    //Create a RoomDatabase.Callback and override onOpen()
    //if you use "onOpen()" the db will be recreated every time the app is started
    //while "onCreate()" the db will remain the same
    private static  RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){
                @Override
                public void onCreate (@NonNull SupportSQLiteDatabase db){
                    super.onCreate(db);
                    Log.e(TAG, "DB onCreate");
                    Log.e(TAG, "starting populating db");
                    new PopulateDbAsync(INSTANCE).execute();
                    Log.e(TAG, "populating db");
                }

                /*@Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    *//*Log.e(TAG, "DB onOpen");
                    Log.e(TAG, "starting populating db");
                    new PopulateDbAsync(INSTANCE).execute();
                    Log.e(TAG, "populating db");*//*
                }*/
            };

    //migrating from db v1(sqlite) to v2(room)
    public static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //create new table for bible_verses
            /*database.execSQL("CREATE TABLE bible_verses_new (_id, verse, kjv, msg," +
                    " amp, issue_id, favourite)");*/

            //copy the data
            /*database.execSQL("INSERT INTO bible_verses_new (_id, verse, kjv, msg," +
                    "amp, issue_id, favourite) SELECT _id, verse, " +
                    "kjv, msg, amp, issue_id, favourite FROM bible_verses");*/

            //remove the old bible_verses table
            //database.execSQL("DROP TABLE bible_verses");

            //rename the table to the correct name
            //database.execSQL("ALTER TABLE bible_verses_new RENAME TO bible_verses");
        }
    };

    //migrating from db v2 to v3
    //dropping un used tables for new table structure
    public static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //drop all the previous un-used tables
            //database.execSQL("DROP TABLE bible_names");
            //database.execSQL("DROP TABLE notes");
            //database.execSQL("DROP TABLE favourites");
            //create new table for testimonies
            database.execSQL("CREATE TABLE IF NOT EXISTS `testimonies` (`testimony_id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL," +
                    " `testimony_name` TEXT," +
                    "`content` TEXT, `image_name` TEXT, `testimony_status` INTEGER NOT NULL, `testimony_likes` INTEGER NOT NULL," +
                    "`posted_on` TEXT,`poster_name` TEXT,`posted_by` INTEGER NOT NULL, `is_reported` INTEGER NOT NULL, `profile_pic` TEXT," +
                    "`is_liked` INTEGER NOT NULL, `user_name` TEXT, PRIMARY KEY(`testimony_id`))");

            //create the prayer requests table
            database.execSQL("CREATE TABLE IF NOT EXISTS `prayer_requests` (`prayer_id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL," +
                    "`prayer_title` TEXT," +
                    "`description` TEXT, `image_name` TEXT, `prayer_status` INTEGER NOT NULL, `prayers_received` INTEGER NOT NULL," +
                    "`posted_on` TEXT,`poster_name` TEXT,`posted_by` INTEGER NOT NULL, `is_reported` INTEGER NOT NULL, `profile_pic` TEXT," +
                    "`is_prayed_for` INTEGER NOT NULL, `user_name` TEXT, PRIMARY KEY(`prayer_id`))");

            /*database.execSQL("CREATE TABLE bible_verses_new (_id, verse, kjv, msg," +
                    " amp, issue_id, favourite)");

            //copy the data
            database.execSQL("INSERT INTO bible_verses_new (_id, verse, kjv, msg," +
                    "amp, issue_id, favourite) SELECT _id, verse, " +
                    "kjv, msg, amp, issue_id, favourite FROM bible_verses");

            //remove the old bible_verses table
            database.execSQL("DROP TABLE bible_verses");

            //rename the table to the correct name
            database.execSQL("ALTER TABLE bible_verses_new RENAME TO bible_verses");*/
        }
    };

    //migrating from db v3 to v4
    //creating the user table
    public static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //create new table for user
            database.execSQL("CREATE TABLE IF NOT EXISTS `user` (`user_id` INTEGER NOT NULL, `name` TEXT," +
                    "`email` TEXT, `profile_pic` TEXT, `created_at` TEXT, PRIMARY KEY(`user_id`))");

        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Step 1: Create a new temporary table with the correct schema
            database.execSQL("CREATE TABLE `daily_verses_new` (`notify_date` TEXT NOT NULL, `verse_id` INTEGER NOT NULL, PRIMARY KEY(`notify_date`))");

            // Step 2: Copy data from the old table to the new one
            database.execSQL("INSERT INTO `daily_verses_new` (`notify_date`, `verse_id`) SELECT `notify_date`, `verse_id` FROM `daily_verses`");

            // Step 3: Remove the old table
            database.execSQL("DROP TABLE `daily_verses`");

            // Step 4: Rename the new table
            database.execSQL("ALTER TABLE `daily_verses_new` RENAME TO `daily_verses`");
        }
    };

    //adds the names to the bible_names table
    private static void loadDailyVerses(LifeIssuesDatabase issuesDatabase) throws IOException {

        Log.d(TAG, "Started loading daily verse.");

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        //truncate the daily verse table
        //issuesDatabase.dailyVersesDao().truncateDailyVersesTable();

        //get all the issue ids
        try (Cursor issueCursor = issuesDatabase.issuesVersesDao().verseIds()) {
            if (issueCursor != null) {
                issueCursor.moveToFirst();
                while (!issueCursor.isAfterLast()) {
                    String date = df.format(c.getTime());
                    addDailyVerse(issuesDatabase,
                            issueCursor.getInt(issueCursor.getColumnIndex("_id")),
                            date);
                    c.add(Calendar.DAY_OF_YEAR, 1);

                    issueCursor.moveToNext();
                }

            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, "Completed loading daily verse...");
    }

    private static void addDailyVerse(LifeIssuesDatabase db, int verse_id, String date){
        DailyVerse dailyVerse = new DailyVerse();
        dailyVerse.setVerseId(verse_id);
        dailyVerse.setNotifyDate(date);
        //db.dailyVersesDao().insertDailyVerseRecord(verse_id, date);
        db.dailyVersesDao().insertDailyVerse(dailyVerse);
    }

    //AsyncTask that populates the database
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final LifeIssuesDatabase lifeIssuesDatabase;

        PopulateDbAsync(LifeIssuesDatabase db){
            lifeIssuesDatabase = db;
        }

        @Override
        protected Void doInBackground(final Void... params){

            try {
                //loadNames(lifeIssuesDatabase);
                loadDailyVerses(lifeIssuesDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d(TAG, "Database content inserted");

            return null;
        }
    }
}
