package com.lifeissues.lifeissues.data;

import android.app.Application;
import android.app.SearchManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.lifeissues.lifeissues.data.database.BibleVersesDao;
import com.lifeissues.lifeissues.data.database.DailyVersesDao;
import com.lifeissues.lifeissues.data.database.IssuesDao;
import com.lifeissues.lifeissues.data.database.LifeIssuesDatabase;
import com.lifeissues.lifeissues.data.database.NotesDao;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.models.Note;

import java.util.HashMap;

public class LifeIssuesRepository {
    private static final String TAG = LifeIssuesRepository.class.getSimpleName();
    private BibleVersesDao bibleVersesDao;
    private NotesDao notesDao;
    private IssuesDao issuesDao;
    private DailyVersesDao dailyVersesDao;
    private AppExecutors mExecutors;
    private long insertedDailyVerseId, favIssueId;
    private int deletedFavId;
    private Cursor bibleVersesCursor, issuesCursor, dailyVerseCursor;
    private static LifeIssuesRepository instance;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    //constructor that gets a handle to the db and initializes the member
    //variables
    public LifeIssuesRepository(Application application) {
        LifeIssuesDatabase db = LifeIssuesDatabase.getDatabase(application);
        bibleVersesDao = db.bibleVersesDao();
        notesDao = db.notesDao();
        issuesDao = db.issuesDao();
        dailyVersesDao = db.dailyVersesDao();
        mExecutors = AppExecutors.getInstance();
        instance = this;
    }

    public static LifeIssuesRepository getInstance(){
        return  instance;
    }

    //count all the bible verses in the db
    public int countAllBibleVerses(){
        return bibleVersesDao.countAllBibleVerses();
    }

    //get all the bible verses for an issue
    public Cursor getBibleVersesForIssue(int issueId){
        return bibleVersesDao.getBibleVerses(issueId);
    }

    //get a single Bible verse
    public Cursor getSingleVerse(int verseId){
        return bibleVersesDao.getSingleBibleVerse(verseId);
    }

    public Cursor getRandomVerse(){
        return bibleVersesDao.getRandomVerse();
    }

    //get all fav verses
    public Cursor getAllFavouriteVerses(){
        return bibleVersesDao.getAllFavouriteVerses();
    }

    //get a note
    public Cursor getNote(int noteId){
        return notesDao.getNote(noteId);
    }

    //delete a note
    public void deleteNote(Note note){
        mExecutors.diskIO().execute(() -> {
            notesDao.deleteNote(note);
        });
    }

    //update a note
    public void updateNote(Note note){
        mExecutors.diskIO().execute(() -> {
            notesDao.updateNote(note);
        });
    }

    //create note
    public void createNote(Note note){
        mExecutors.diskIO().execute(() -> {
            notesDao.insertNote(note);
        });
    }

    //get all notes
    public Cursor getNotes(){
        return notesDao.getNotes();
    }

    //delete favorite notes
    public void deleteFavNote(String favValue, int verseId){
        mExecutors.diskIO().execute(() ->{
            notesDao.deleteFavouriteNote(favValue, verseId);
        });
    }

    //add favorite notes
    public void addFavNote(String favValue, int verseId){
        mExecutors.diskIO().execute(() ->{
            notesDao.addFavouriteNote(favValue,verseId);
        });
    }

    //add favorite Bible verse
    public void addFavVerse(int verse_id, int issueID){
        mExecutors.diskIO().execute(() ->{
            bibleVersesDao.addFavourite(verse_id,issueID);
        });
    }

    //remove favorite Bible verse
    public void deleteFavVerse(int verse_id, int issueID){
        mExecutors.diskIO().execute(() ->{
            bibleVersesDao.deleteFavourite(verse_id,issueID);
        });
    }

    //add daily verse to db
    public boolean addDailyVerse(int verse_id, String verse, String kjv, String msg, String amp,
                              String favValue, String issueName, int issue_id, String dateTaken){
        //mExecutors.diskIO().execute(() ->{
            insertedDailyVerseId = dailyVersesDao.addDailyVerse(verse_id, verse, kjv, msg, amp,
                    favValue, issueName, issue_id, dateTaken);
        //});
        return insertedDailyVerseId > 0;
    }

    //getting daily verse from the db
    public Cursor getDailyVerse(String dateToday){
        //mExecutors.diskIO().execute(() ->{
            dailyVerseCursor = dailyVersesDao.getDailyVerse(dateToday);
        //});
        return dailyVerseCursor;
    }

    //check the daily verse
    public Cursor checkDailyVerse(String dateToday){
        return dailyVersesDao.checkDailyVerse(dateToday);
    }

    //get issues list
    public Cursor getIssues(){
        //mExecutors.diskIO().execute(() ->{
            issuesCursor = issuesDao.getIssues();
        //});
        return issuesCursor;
    }

    //getting a favourite issue from the db
    public Cursor getFavouriteIssue(String issueName){
        return issuesDao.getFavouriteIssue(issueName);
    }

    //add a favorite issue
    public boolean addFavoriteIssue(String issueName, String verses){
        mExecutors.diskIO().execute(() -> {
            favIssueId = issuesDao.addFavouriteIssue(issueName, verses);
        });
        return favIssueId > 0;
    }

    //delete favorite issue
    public boolean deleteFavIssue(String issue){
        mExecutors.diskIO().execute(() -> {
            deletedFavId = issuesDao.deleteFavouriteIssue(issue);
        });
        return deletedFavId > 0;
    }

    //get all favorite issues
    public Cursor getAllFavoriteIssues(){
        return issuesDao.getAllFavouriteIssues();
    }

    //get all favorite notes
    public Cursor getAllFavoriteNotes(){
        return notesDao.getAllFavouriteNotes("yes");
    }

    /**
     * Returns a Cursor over all words that match the given query
     *
     * @param query The string to search for
     * @param columns The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getWordMatches(String query, String[] columns) {
        Cursor cursor;
        String selection = issuesDao.KEY_ISSUE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};
        Log.e(TAG, "Search query = "+query);

        cursor = issuesDao.getWordMatches(query+"*");
        //new getWordMatchesAsyncTask(mPhrasesDao).execute(query);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;

        //return query(selection, selectionArgs, columns);

        /*This builds a query that looks like:
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
        builder.setTables(issuesDao.FTS_VIRTUAL_ISSUES_TABLE);
        builder.setProjectionMap(mColumnMap);

        //Cursor cursor = builder.query(this.getReadableDatabase(),
        //      columns, selection, selectionArgs, null, null, null);
        Cursor cursor = null;

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    /**
     * Builds a map for all columns that may be requested, which will be given to the
     * SQLiteQueryBuilder. This is a good way to define aliases for column names, but must include
     * all columns, even if the value is the key. This allows the ContentProvider to request
     * columns w/o the need to know real column names and create the alias itself.
     */
    private static HashMap<String,String> buildColumnMap() {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put(IssuesDao.KEY_ISSUE_NAME, IssuesDao.KEY_ISSUE_NAME);
        map.put(IssuesDao.KEY_ISSUE_VERSES, IssuesDao.KEY_ISSUE_VERSES);
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
     * @param rowId id of phrase to retrieve
     * @param //columns The columns to include, if null then all are included
     * @return Cursor positioned to matching phrase, or null if not found.
     */
    public Cursor getIssue(String rowId) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] {rowId};
        Log.e(TAG, "Search rowid = "+rowId);

        Log.e(TAG, "GetIssue in repo = "+issuesDao.getIssue(rowId).getCount());
        Cursor cursor1 = issuesDao.getIssue(rowId);

        if (cursor1 == null) {
            return null;
        } else if (!cursor1.moveToFirst()) {
            cursor1.close();
            return null;
        }
        return cursor1;
        //return query(selection, selectionArgs, columns);

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }

    public Cursor getAllIssues(String[] columns) {
        Cursor cursor;
        cursor = query(null, null, columns);
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;

        /* This builds a query that looks like:
         *     SELECT <columns> FROM <table> WHERE rowid = <rowId>
         */
    }
}
