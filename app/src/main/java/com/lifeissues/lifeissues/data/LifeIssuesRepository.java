package com.lifeissues.lifeissues.data;

import android.app.Application;
import android.app.SearchManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.lifeissues.lifeissues.data.database.BibleVersesDao;
import com.lifeissues.lifeissues.data.database.DailyVersesDao;
import com.lifeissues.lifeissues.data.database.IssuesDao;
import com.lifeissues.lifeissues.data.database.LifeIssuesDatabase;
import com.lifeissues.lifeissues.data.database.PrayerRequestsDao;
import com.lifeissues.lifeissues.data.database.TestimoniesDao;
import com.lifeissues.lifeissues.data.database.UsersDao;
import com.lifeissues.lifeissues.data.network.AuthenticateUser;
import com.lifeissues.lifeissues.data.network.TestimonyPrayerNetworkActions;
import com.lifeissues.lifeissues.helpers.AppExecutors;
import com.lifeissues.lifeissues.models.BibleVerse;
import com.lifeissues.lifeissues.models.BibleVerseResult;
import com.lifeissues.lifeissues.models.DailyVerse;
import com.lifeissues.lifeissues.models.ImageUpload;
import com.lifeissues.lifeissues.models.Issue;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.models.User;
import com.lifeissues.lifeissues.ui.activities.MyProfileActivity;
import com.lifeissues.lifeissues.ui.activities.PostPrayerRequestActivity;
import com.lifeissues.lifeissues.ui.activities.PostTestimonyActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LifeIssuesRepository {
    private static final String TAG = LifeIssuesRepository.class.getSimpleName();
    private BibleVersesDao bibleVersesDao;
    private TestimoniesDao testimoniesDao;
    private PrayerRequestsDao prayerRequestsDao;
    private IssuesDao issuesDao;
    private UsersDao mUsersDao;
    private DailyVersesDao dailyVersesDao;
    private AppExecutors mExecutors;
    private LiveData<List<BibleVerseResult>> dailyVerseList;
    private LiveData<List<Issue>> issuesList;
    private LiveData<List<Testimony>> recentTestimoniesList;
    private LiveData<List<PrayerRequest>> recentRequestsList;
    private static LifeIssuesRepository instance;
    private TestimonyPrayerNetworkActions testimonyPrayerNetworkActions;
    private AuthenticateUser authenticateUser;
    private static final HashMap<String,String> mColumnMap = buildColumnMap();

    //constructor that gets a handle to the db and initializes the member
    //variables
    public LifeIssuesRepository(Application application) {
        LifeIssuesDatabase db = LifeIssuesDatabase.getDatabase(application);
        bibleVersesDao = db.bibleVersesDao();
        testimoniesDao = db.testimoniesDao();
        prayerRequestsDao = db.prayerRequestsDao();
        issuesDao = db.issuesDao();
        mUsersDao = db.usersDao();
        dailyVersesDao = db.dailyVersesDao();
        issuesList = issuesDao.getAllIssues();
        mExecutors = AppExecutors.getInstance();
        testimonyPrayerNetworkActions = TestimonyPrayerNetworkActions.getInstance(application, mExecutors);
        recentTestimoniesList = testimonyPrayerNetworkActions.getRecentPostedTestimonies();
        recentRequestsList = testimonyPrayerNetworkActions.getRecentPostedPrayerRequests();
        authenticateUser = AuthenticateUser.getInstance(application,mExecutors);
        instance = this;

        fetchRecentTestimonies();
        fetchRecentPrayerRequests();
    }

    public static LifeIssuesRepository getInstance(){
        return  instance;
    }

    /*public BibleNamesDao getBibleNamesDao(){
        return bibleNamesDao;
    }*/

    //get the testimonies
    public TestimoniesDao getTestimoniesDao(){
        return testimoniesDao;
    }

    //get the prayer requests
    public PrayerRequestsDao getPrayerRequestsDao(){
        return prayerRequestsDao;
    }

    private void fetchRecentTestimonies(){
        mExecutors.diskIO().execute(() -> {
            //mJobDao.deleteAll();
            //if (isTaskFetchNeeded()) {
            testimonyPrayerNetworkActions.GetRecentTestimonies();
            //}
        });

        //get the recent testimonies
        recentTestimoniesList.observeForever(tasksList -> mExecutors.diskIO().execute(() -> {
            Log.e(TAG,"Task list size is  "+tasksList);
            testimoniesDao.insertTestimonies(tasksList);
        }));
    }

    private void fetchRecentPrayerRequests(){
        mExecutors.diskIO().execute(() -> {
            //mJobDao.deleteAll();
            //if (isTaskFetchNeeded()) {
            testimonyPrayerNetworkActions.GetRecentRequests();
            //}
        });

        //get the recent testimonies
        recentRequestsList.observeForever(tasksList -> mExecutors.diskIO().execute(() -> {
            prayerRequestsDao.insertPrayerRequests(tasksList);
        }));
    }

    //count all the bible verses in the db
    public int countAllBibleVerses(){
        return bibleVersesDao.countAllBibleVerses();
    }

    //get all the bible verses for an issue
    public Cursor getBibleVersesForIssue(int issueId){
        return bibleVersesDao.getBibleVerses(issueId);
    }

    public LiveData<List<BibleVerseResult>> getIssueBibleVerses(int issueId){
        //initializeData();
        return bibleVersesDao.getIssueBibleVerses(issueId);
    }

    //get a single Bible verse
    public Cursor getSingleVerse(int verseId){
        return bibleVersesDao.getSingleBibleVerse(verseId);
    }

    public LiveData<List<BibleVerseResult>> getSingleBibleVerse(int verseId){
        return bibleVersesDao.getSingleVerse(verseId);
    }

    public Cursor getRandomVerse(){
        return bibleVersesDao.getRandomVerse();
    }

    //get all fav verses
    public Cursor getAllFavouriteVerses(){
        return bibleVersesDao.getAllFavouriteVerses();
    }

    public LiveData<List<BibleVerseResult>> getAllFavoriteVerses(){
        return bibleVersesDao.getAllFavoriteVerses();
    }

    public void getContentPics(int content_id, int contentType){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.GetContentImagesForEdit(content_id, contentType));
        //return mLoginUser.getPortImages();
    }

    public LiveData<List<ImageUpload>> getContentPicsDetails(int content_id){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.GetContentPicsDetails(content_id));
        return testimonyPrayerNetworkActions.getContentPicsDetails();
    }

    //delete the image
    public void deleteContentPic(int pic_id){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.deleteContentPic(pic_id));
    }

    public void postNewContent(String title, String description, ArrayList<File> imageFilesList,
                               int categoryId, int userId, int contentType,
                               PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity){

        //call retrofit in background to post job update details
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.postNewContent(title, description,
                imageFilesList, categoryId, userId, contentType, postTestimonyActivity,prayerRequestActivity) );

    }

    public void editContent(int contentId, String title, String description, ArrayList<File> imageFilesList,
                            int categoryId, int userId, int contentType,
                            PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity){

        //call retrofit in background to post job update details
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.editContent(contentId, title, description,
                imageFilesList, categoryId, userId, contentType, postTestimonyActivity,prayerRequestActivity) );

    }

    public void reportContent(int userId, int content_id, String comment, int content_type){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.reportContent(userId, content_id, comment,content_type));
    }

    public void deleteContent(int userId, int content_id, int content_type){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.deleteContent(userId, content_id, content_type));
    }

    public void unLikeContent(int userId, int content_id, int content_type){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.unLikeContent(userId,content_id,content_type));
    }

    //user like content
    public void likeContent(int userId, int content_id, int content_type){
        mExecutors.diskIO().execute(() -> testimonyPrayerNetworkActions.likeContent(userId,content_id,content_type));
    }

    //method to call service to login user
    public void loginFixAppUser(String email, String password){
        authenticateUser.UserLogIn(email, password);
    }

    //method to register user in database
    //calls service
    public void registerFixAppUser(String name, String email, String password){
        authenticateUser.UserRegister(name, email, password);
    }

    //a wrapper for the insert() method. Must be called on a non UI thread
    //or the app will crash
    public void insertUser (User user){
        mExecutors.diskIO().execute(() ->{
            mUsersDao.insertUser(user);
        });
    }

    //delete user details from db
    //required when user logs out of app
    public void deleteUser (){
        mExecutors.diskIO().execute(() -> {
            mUsersDao.deleteUser();
        });
    }

    //updates the user details in the db
    public void updateProfile(int user_id, String email, String created_on,
                              String profile_pic, String name){
        mExecutors.diskIO().execute(() ->{
            mUsersDao.updateProfile(user_id, email, created_on, profile_pic, name);

        });
    }

    //user updates their profile
    //update user details
    public void updateUserProfile(int user_id, String username, String email,
                                  MyProfileActivity activity){
        mExecutors.diskIO().execute(() -> authenticateUser.updateUserDetails(user_id, username, email,
                activity));
    }

    //save the user's new profile pic
    public void saveProfilePic(int user_id,File profilePic, MyProfileActivity activity){
        mExecutors.diskIO().execute(() -> authenticateUser.saveProfilePic(user_id, profilePic,activity));
    }

    //log out the user
    public void logOutUser(int user_id){
        mExecutors.diskIO().execute(() -> authenticateUser.UserLogout(user_id));
    }

    //get a note
    /*public Cursor getNote(int noteId){
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
    }*/

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

    //add favorite daily Bible verse
    /*public void setFavVerse(int verse_id){
        mExecutors.diskIO().execute(() ->{
            dailyVersesDao.setFavourite(verse_id);
        });
    }*/

    //remove favorite daily Bible verse
    /*public void removeFavVerse(int verse_id){
        mExecutors.diskIO().execute(() ->{
            dailyVersesDao.removeFavourite(verse_id);
        });
    }*/

    //add daily verse to db
    /*public boolean addDailyVerse(int verse_id, String verse, String kjv, String msg, String amp,
                              int favValue, String issueName, int issue_id, String dateTaken){
        //mExecutors.diskIO().execute(() ->{
            insertedDailyVerseId = dailyVersesDao.addDailyVerse(verse_id, verse, kjv, msg, amp,
                    favValue, issueName, issue_id, dateTaken);
        //});
        return insertedDailyVerseId > 0;
    }*/

    //getting daily verse from the db
//    public Cursor getDailyVerse(String dateToday){
//        dailyVerseCursor = dailyVersesDao.getDailyVerse(dateToday);
//        return dailyVerseCursor;
//    }

    public LiveData<List<BibleVerseResult>> getDailyVerse(String date){
        //mExecutors.diskIO().execute(() ->{
        dailyVerseList = dailyVersesDao.getDailyVerse(date);
        //});
        return dailyVerseList;
    }

    //check the daily verse
    public Cursor checkDailyVerse(String dateToday){
        return dailyVersesDao.checkDailyVerse(dateToday);
    }

    //get issues list
    public LiveData<List<Issue>> getIssues(){
        //mExecutors.diskIO().execute(() ->{
            //issuesCursor = issuesDao.getIssues2();
        //});
        return issuesList;
    }

    public LiveData<List<Testimony>> getHomeTestimonies(){
        return null;
    }

    //getting a favourite issue from the db
    /*public Cursor getFavouriteIssue(String issueName){
        return issuesDao.getFavouriteIssue(issueName);
    }

    //add a favorite issue
    public boolean addFavoriteIssue(int issueId, String issueName, String verses){
        mExecutors.diskIO().execute(() -> {
            favIssueId = issuesDao.addFavouriteIssue(issueId, issueName, verses);
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
    }*/

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

    //get name suggestions
    public Cursor getNameMatches(String query, String[] columns) {
        Cursor cursor = null;
        String selection = issuesDao.KEY_ISSUE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};
        Log.e(TAG, "Search query = "+query);

        //cursor = bibleNamesDao.getWordMatches(query+"*");

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
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
        map.put(IssuesDao.KEY_ISSUE_DESCRIPTION, IssuesDao.KEY_ISSUE_DESCRIPTION);
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

    //get a name
    public Cursor getName(String rowId) {
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
