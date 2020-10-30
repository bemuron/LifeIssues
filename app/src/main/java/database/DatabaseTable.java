package database;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.lifeissues.lifeissues.R;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Contains logic to return specific words from the dictionary, and
 * load the dictionary table when it needs to be created.
 */
public class DatabaseTable extends SQLiteAssetHelper {
    private static final String TAG = "IssueDatabase";

    private static final String DATABASE_NAME = "Life_Issues.db";
    private static final int DATABASE_VERSION = 1;

    //Table Names
    private static final String FTS_VIRTUAL_ISSUES_TABLE = "issues";
    //public static final String ARTICLES_TABLE = "articles";
    public static final String FAVOURITES_TABLE = "favourites";
    public static final String BIBLE_VERSES_TABLE = "bible_verses";
    public static final String DAILY_VERSES_TABLE = "daily_verses";
    public static final String NOTES_TABLE = "notes";
    public static final String ISSUES_VERSES_TABLE = "issues_verses";

    //Common column names
    public static final String KEY_ID = "_id";
    public static final String KEY_ISSUE_ID = "issue_id";
    public static final String KEY_ISSUE_CAT = "issue_category";
    public static final String KEY_FAVOURITE = "favourite";

    //Issues table column names
    public static final String KEY_ISSUE_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1;
    public static final String KEY_ISSUE_VERSES = SearchManager.SUGGEST_COLUMN_TEXT_2;

    //Favourites table column names
    public static final String KEY_FAV_ISSUE_NAME = "issue_name";
    public static final String KEY_FAV_ISSUE_VERSES = "issue_verses";

    //Notepad table column names
    public static final String KEY_NOTE_TITLE = "title";
    public static final String KEY_NOTE_CONTENT = "content";
    public static final String KEY_NOTE_VERSE = "verse";
    public static final String KEY_NOTE_ISSUE = "life_issue";
    public static final String KEY_DATE_CREATED = "date_created";
    public static final String KEY_DATE_UPDATED = "date_updated";

    //Bible verses and Daily verses table column names
    public static final String KEY_ISSUE_NAME_ID = "issue_name";
    public static final String KEY_DATE_TAKEN = "date_taken";
    public static final String KEY_VERSE = "verse";
    public static final String KEY_KJV = "kjv";
    public static final String KEY_MSG = "msg";
    public static final String KEY_AMP = "amp";
    public static final String KEY_TEXT = "issue_text";

    //issues_verses table
    public static final String KEY_VERSE_ID = "verse_id";
    public static final String KEY_IS_FAVORITE = "is_favorite";

    //private final IssuesOpenHelper mIssuesOpenHelper;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    /**
     * Constructor
     * @param context The Context within which to work, used to create the DB
     */
    /*
    public DatabaseTable(Context context) {
        mIssuesOpenHelper = new IssuesOpenHelper(context);
    }
*/
    public DatabaseTable(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_ISSUES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + BIBLE_VERSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_VERSES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NOTES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FAVOURITES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ISSUES_VERSES_TABLE);
        onCreate(db);
    }
    /**
     * Builds a map for all columns that may be requested, which will be given to the 
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include 
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(KEY_ISSUE_NAME, KEY_ISSUE_NAME);
        map.put(KEY_ISSUE_VERSES, KEY_ISSUE_VERSES);
        map.put(BaseColumns._ID, "rowid AS " +
                BaseColumns._ID);
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " +
                SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        return map;
    }

    /**
     * Returns a Cursor positioned at the word specified by rowId
     *
     * @param rowId id of word to retrieve
     * @param columns The columns to include, if null then all are included
     * @return Cursor positioned to matching word, or null if not found.
     */
    public Cursor getWord(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    public Cursor getAllIssues(String[] columns) {
        return query(null, null, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }


    public Cursor getIssues(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(FTS_VIRTUAL_ISSUES_TABLE,new String[]{"rowid",DatabaseTable.KEY_ISSUE_NAME,
                        DatabaseTable.KEY_ISSUE_VERSES,}
                ,null,null,null,null,null,null);

        return c;
    }

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getWordMatches(String query, String[] columns) {
        String selection = KEY_ISSUE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE <COL_WORD> MATCH 'query*'
         * which is an FTS3 search for the query text (plus a wildcard) inside the word column.
         *
         * - "rowid" is the unique id for all rows but we need this value for the "_id" column in
         *    order for the Adapters to work, so the columns need to make "_id" an alias for "rowid"
         * - "rowid" also needs to be used by the SUGGEST_COLUMN_INTENT_DATA alias in order
         *   for suggestions to carry the proper intent data.
         *   These aliases are defined in the IssuesProvider when queries are made.
         * - This can be revised to also search the definition text with FTS3 by changing
         *   the selection clause to use FTS_VIRTUAL_TABLE instead of COL_WORD (to search across
         *   the entire table, but sorting the relevance could be difficult.
         */
    }

    /**
     * Performs a database query.
     * @param selection The selection clause
     * @param selectionArgs Selection arguments for "?" components in the selection
     * @param columns The columns to return
     * @return A Cursor over all rows matching the query
     */
    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        /* The SQLiteBuilder provides a map for all possible columns requested to
         * actual columns in the database, creating a simple column alias mechanism
         * by which the ContentProvider does not need to know the real column names
         */
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_ISSUES_TABLE);
        //builder.setTables(FTS_VIRTUAL_BIBLE_VERSES_TABLE);
        builder.setProjectionMap(mColumnMap);

        Cursor cursor = builder.query(this.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    //get all favourite issues
    public Cursor getAllFavouriteIssues(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(FAVOURITES_TABLE,new String[]{KEY_ID,KEY_FAV_ISSUE_NAME,KEY_FAV_ISSUE_VERSES
        },null,null,null,null,null,null);

        return c;
    }

    //getting a favourite issue from the db
    public Cursor getFavouriteIssue(String issueName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + FAVOURITES_TABLE + " WHERE " + KEY_FAV_ISSUE_NAME + " =?";
        Cursor c = db.rawQuery(sql,new String[]{issueName});
        return c;
    }

    //get issue name
    public String getIssueName(int issueID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + KEY_ISSUE_NAME + " FROM " + FTS_VIRTUAL_ISSUES_TABLE + " WHERE rowid =?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(issueID)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DatabaseTable.KEY_ISSUE_NAME));
    }

    //get issue ID
    //used for the random verse
    public String getIssueID(int verseID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + KEY_ISSUE_ID + " FROM " + ISSUES_VERSES_TABLE + " WHERE verse_id =? LIMIT 1";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(verseID)});
        c.moveToFirst();
        return c.getString(c.getColumnIndex(DatabaseTable.KEY_ISSUE_ID));
    }

    //adding a favourite issue to the db
    public boolean addFavouriteIssue(String issueName, String verses){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        //These Fields should be your String values of actual column names
        cv.put(KEY_FAV_ISSUE_VERSES,verses);
        cv.put(KEY_FAV_ISSUE_NAME,issueName);

        long id = db.insert(FAVOURITES_TABLE, null, cv);
        // Inserting Row
        db.close(); // Closing database connection

        Log.d(TAG, "New favourite inserted into sqlite: " + id);
        return true;
    }

    //deleting a favourite
    public int deleteFavouriteIssue(String issue){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = KEY_FAV_ISSUE_NAME +" = ?";
        String[] whereParams = new String[]{issue};
        int delete_fav = db.delete(FAVOURITES_TABLE,where,whereParams);

        return delete_fav;
    }

    //adding a favourite to the db
    public int addFavourite(int verse_id, int issueID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_IS_FAVORITE,"1"); //These Fields should be your String values of actual column names
        return db.update(ISSUES_VERSES_TABLE, cv, "verse_id="+verse_id +" and issue_id="+issueID, null);
    }

    //getting a favourite from the db
    public Cursor getFavourite(String value) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + BIBLE_VERSES_TABLE + " WHERE " + KEY_FAVOURITE + " =?";
        Cursor c = db.rawQuery(sql,new String[]{value});
        return c;
    }
/*
    public int deleteFavourite(String name_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "_id = ?";
        String[] whereParams = new String[]{name_id};
        int delete_fav = db.delete(BIBLE_VERSES_TABLE,where,whereParams);

        return delete_fav;
    }
*/
    //deleting a favourite from the db
    public int deleteFavourite(int verse_id, int issueID){
        SQLiteDatabase db = this.getWritableDatabase();
        /*Cursor c = db.rawQuery("update issues_verses " +
                        "set is_favorite = 0 " +
                        "WHERE issue_id =? and verse_id =?",
                new String[]{String.valueOf(issueID),String.valueOf(verse_id)});*/
        ContentValues cv = new ContentValues();
        cv.put(KEY_IS_FAVORITE,"0"); //These Fields should be your String values of actual column names
        return db.update(ISSUES_VERSES_TABLE, cv, "verse_id="+verse_id +" and issue_id="+issueID, null);
    }

    //getting specific category content from the db
    /*public Cursor getBibleVerses(String issue_name){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(BIBLE_VERSES_TABLE,new String[]{KEY_ID,KEY_VERSE,KEY_KJV,KEY_MSG,KEY_AMP,
                        KEY_FAVOURITE,KEY_ISSUE_ID},
                KEY_ISSUE_ID + " =? ",new String[]{issue_name},null,null,null,null);
        return c;
    }*/

    //getting specific category content from the db
    public Cursor getBibleVerses(int issueID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
                        "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
                        "issues i on i.ROWID = iv.issue_id WHERE iv.issue_id =?",
                new String[]{String.valueOf(issueID)});
        return c;
    }

    //getting random verse from the db
    public Cursor getRandomVerse(int verse_id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
                        "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
                        "issues i on i.ROWID = iv.issue_id WHERE b._id =? LIMIT 1",
                new String[]{String.valueOf(verse_id)});
        return c;
    }

    //getting all bible verses from the db
    public Cursor getAllBibleVerses(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select b._id, iv.issue_id, i.suggest_text_1, b.verse, b.kjv, b.msg, b.amp, " +
                        "iv.is_favorite from bible_verses b JOIN issues_verses iv on iv.verse_id = b._id JOIN " +
                        "issues i on i.ROWID = iv.issue_id", null);

        return c;
    }

    //count all bible verses in the db
    public int countAllBibleVerses(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select count(_id) as versesNumber from bible_verses", null);
        c.moveToFirst();
        return Integer.parseInt(c.getString(c.getColumnIndex("versesNumber")));
    }

    public Cursor getAllFavouriteVerses() {
        SQLiteDatabase db = this.getReadableDatabase();
        //String sql = "SELECT * FROM " + BIBLE_VERSES_TABLE + " WHERE " + KEY_FAVOURITE+ " =?";
        String sql = "select b._id," +
                "iv.issue_id," +
                "i.suggest_text_1," +
                "b.verse, b.kjv," +
                "b.msg," +
                "b.amp," +
                "iv.is_favorite " +
                "from bible_verses b " +
                "JOIN issues_verses iv on iv.verse_id = b._id " +
                "JOIN issues i on i.ROWID = iv.issue_id " +
                "WHERE iv.is_favorite =?";
        Cursor cursor = db.rawQuery(sql, new String[]{"1"});

        return cursor;
    }

    //checking if a specific verse is favorite
    public boolean isVerseFavorite(int verseID, int issueID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("select _id " +
                        "from bible_verses " +
                        "WHERE _id in (select verse_id from issues_verses where issue_id =? and verse_id =? and is_favorite =?)",
                new String[]{String.valueOf(issueID),String.valueOf(verseID),"1"});
        return c.getCount() == 1;
    }

    //checking if a daily verse for today has already been entered
    public Cursor checkDailyVerse(String dateToday) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + DAILY_VERSES_TABLE + " WHERE "
                + KEY_DATE_TAKEN + " = ?";
        Cursor c = db.rawQuery(sql,new String[]{dateToday});
        return c;
    }

    //adding a daily verse to the db
    public boolean addDailyVerse(int verse_id, String verse, String kjv, String msg, String amp,
                             String favValue, String issueName, int issue_id, String dateTaken){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID,verse_id); //These Fields should be your String values of actual column names
        cv.put(KEY_VERSE,verse);
        cv.put(KEY_KJV,kjv);
        cv.put(KEY_MSG,msg);
        cv.put(KEY_AMP,amp);
        cv.put(KEY_FAVOURITE,favValue);
        cv.put(KEY_ISSUE_NAME_ID,issueName);
        cv.put(KEY_ISSUE_ID,issue_id);
        cv.put(KEY_DATE_TAKEN,dateTaken);

        long id = db.insert(DAILY_VERSES_TABLE, null, cv);
        // Inserting Row
        //db.close(); // Closing database connection

        Log.d(TAG, "New verse inserted into sqlite: " + id);
        return true;
    }

    //getting daily verse from the db
    public Cursor getDailyVerse(String dateToday){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(DAILY_VERSES_TABLE,new String[]{KEY_ID,KEY_VERSE,KEY_KJV,KEY_MSG,KEY_AMP,
                        KEY_ISSUE_NAME_ID,KEY_ISSUE_ID,KEY_FAVOURITE,KEY_DATE_TAKEN},
                KEY_DATE_TAKEN + " =? ",new String[]{dateToday},null,null,null,null);
        return c;
    }

    //-----------------Notes table methods---------------//

    //getting all the goal-notes out of the db
    public  Cursor getNotes(){
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + NOTES_TABLE + ";";
        Cursor c =db.rawQuery(sql, null);
        return c;
    }

    public boolean create(String name, String dates, String note_content, String issue,
                          String verse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TITLE, name);
        values.put(KEY_NOTE_CONTENT, note_content);
        values.put(KEY_DATE_CREATED, dates);
        values.put(KEY_NOTE_ISSUE, issue);
        values.put(KEY_NOTE_VERSE, verse);
        db.insert(NOTES_TABLE, null, values);
        return true;
    }

    public boolean update(int id, String name, String note_content,
                          String dates) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_TITLE, name);
        values.put(KEY_NOTE_CONTENT, note_content);
        values.put(KEY_DATE_UPDATED, dates);
        db.update(NOTES_TABLE, values, KEY_ID + " =?",
                new String[] { Integer.toString(id) });
        return true;
    }

    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NOTES_TABLE, KEY_ID + " =?",
                new String[] { Integer.toString(id) });
    }

    public Cursor getNote(int id) {

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + NOTES_TABLE + " WHERE " + KEY_ID + " =?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});

        return cursor;
    }

    public Cursor getAllFavouriteNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + NOTES_TABLE + " WHERE " + KEY_FAVOURITE + " =?";
        Cursor cursor = db.rawQuery(sql, new String[]{"yes"});

        return cursor;
    }

    //adding a favourite note to the db
    public int addFavouriteNote(int verse_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_FAVOURITE, "yes"); //These Fields should be your String values of actual column names
        return db.update(NOTES_TABLE, cv, "_id=" + verse_id, null);
    }

    //deleting a favourite note from the db
    public int deleteFavouriteNote(int verse_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_FAVOURITE,"no"); //These Fields should be your String values of actual column names
        return db.update(NOTES_TABLE, cv, "_id="+verse_id, null);
    }

}
