package com.lifeissues.lifeissues.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.DailyVerse;
import com.lifeissues.lifeissues.models.Favourite;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.models.IssueVerse;
import com.lifeissues.lifeissues.models.Note;

@Database(entities = {BibleVerse.class, DailyVerse.class, Favourite.class,
        Issue.class, IssueVerse.class, Note.class}, version = 2, exportSchema = false)
public abstract class LifeIssuesDatabase extends RoomDatabase {
    private static final String TAG = LifeIssuesDatabase.class.getSimpleName();

    private static LifeIssuesDatabase INSTANCE;
    public abstract BibleVersesDao bibleVersesDao();
    public abstract NotesDao notesDao();
    public abstract IssuesDao issuesDao();
    public abstract DailyVersesDao dailyVersesDao();
    // For Singleton instantiation
    private static final Object LOCK = new Object();

    public static LifeIssuesDatabase getDatabase (final Context context){
        if (INSTANCE == null){
            synchronized (LOCK){
                if (INSTANCE == null){
                    //create db here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LifeIssuesDatabase.class, "Life_Issues.db")
                            .createFromAsset("databases/Life_Issues.db")
                            .fallbackToDestructiveMigrationFrom()// used if we dnt want to provide migrations
                            .addMigrations(MIGRATION_1_2)
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

    //migrating from db v1(sqlite) to v2(room)
    public static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            //create new table for bible_verses
            database.execSQL("CREATE TABLE bible_verses_new (_id, verse, kjv, msg," +
                    " amp, issue_id, favourite)");

            //copy the data
            database.execSQL("INSERT INTO bible_verses_new (_id, verse, kjv, msg," +
                    "amp, issue_id, favourite) SELECT _id, verse, " +
                    "kjv, msg, amp, issue_id, favourite FROM bible_verses");

            //remove the old bible_verses table
            database.execSQL("DROP TABLE bible_verses");

            //rename the table to the correct name
            database.execSQL("ALTER TABLE bible_verses_new RENAME TO bible_verses");
        }
    };
}
