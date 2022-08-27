package com.lifeissues.lifeissues.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.app.AppController;
import com.lifeissues.lifeissues.models.BibleName;
import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.DailyVerse;
import com.lifeissues.lifeissues.models.Favourite;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.models.IssueVerse;
import com.lifeissues.lifeissues.models.Note;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Database(entities = {BibleVerse.class, BibleName.class, DailyVerse.class, Favourite.class,
        Issue.class, IssueVerse.class, Note.class}, version = 2, exportSchema = false)
public abstract class LifeIssuesDatabase extends RoomDatabase {
    private static final String TAG = LifeIssuesDatabase.class.getSimpleName();

    private static LifeIssuesDatabase INSTANCE;

    public abstract BibleVersesDao bibleVersesDao();
    public abstract BibleNamesDao bibleNamesDao();
    public abstract NotesDao notesDao();
    public abstract IssuesDao issuesDao();
    public abstract DailyVersesDao dailyVersesDao();
    //private Context mContext;
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
                            //.addCallback(sRoomDatabaseCallback)
                            //.createFromAsset("databases/Life_Issues.db")
                            .addMigrations(MIGRATION_1_2)

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
                    /*Log.e(TAG, "starting populating db");
                    new PopulateDbAsync(INSTANCE).execute();
                    Log.e(TAG, "populating db");*/
                }

                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    Log.e(TAG, "DB onOpen");
                    Log.e(TAG, "starting populating db");
                    new PopulateDbAsync(INSTANCE).execute();
                    Log.e(TAG, "populating db");
                }
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

    //adds the names to the bible_names table
    private static void loadNames(LifeIssuesDatabase issuesDatabase) throws IOException {
        Log.d(TAG, "Loading names...");
        final Resources resources = AppController.getContext().getResources();
        InputStream inputStream = resources.openRawResource(R.raw.dictionary);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            issuesDatabase.bibleNamesDao().deleteAll();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = TextUtils.split(line, "-");
                if (strings.length < 2) continue;
                long id = addWord(issuesDatabase, strings[0].trim(), strings[1].trim());
                if (id < 1) {
                    Log.e(TAG, "unable to add name: " + strings[0].trim());
                }
            }
        } finally {
            reader.close();
        }
        Log.d(TAG, "DONE loading names.");
    }

    /**
     * Add a word to the dictionary.
     * @return rowId or -1 if failed
     */
    public static long addWord(LifeIssuesDatabase database, String name, String meaning) {
        BibleName bibleName = new BibleName();
        bibleName.setName(name);
        bibleName.setMeaning(meaning);
        return database.bibleNamesDao().insertName(bibleName);
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
                loadNames(lifeIssuesDatabase);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.d(TAG, "Database content inserted");

            return null;
        }
    }
}
