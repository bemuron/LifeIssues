package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.data.network.BrowsePrayerRequestsDataFactory;
import com.lifeissues.lifeissues.data.network.BrowseTestimoniesDataFactory;
import com.lifeissues.lifeissues.data.network.BrowsedPrayerRequestsDataSource;
import com.lifeissues.lifeissues.data.network.BrowsedTestimoniesDataSource;
import com.lifeissues.lifeissues.models.ImageUpload;
import com.lifeissues.lifeissues.models.PrayerRequest;
import com.lifeissues.lifeissues.models.Testimony;
import com.lifeissues.lifeissues.ui.activities.PostPrayerRequestActivity;
import com.lifeissues.lifeissues.ui.activities.PostTestimonyActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestimonyPrayerViewModel extends AndroidViewModel {
    //private member variable to hold reference to the repository
    private LifeIssuesRepository mRepository;
    private LiveData<PagedList<Testimony>> testimoniesPagedList;
    private LiveData<PagedList<PrayerRequest>> requestsPagedList;
    public BrowseTestimoniesDataFactory browseTestimoniesDataFactory;
    public BrowsePrayerRequestsDataFactory browsePrayerRequestsDataFactory;
    private Context mContext;

    //constructor that gets a reference to the repository and gets the categories
    public TestimonyPrayerViewModel(@NonNull Application application) {
        super(application);
        mRepository = new LifeIssuesRepository(application);

        browseTestimoniesDataFactory = new BrowseTestimoniesDataFactory();
        browsePrayerRequestsDataFactory = new BrowsePrayerRequestsDataFactory();

        //Getting PagedList config
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(BrowsedTestimoniesDataSource.PAGE_SIZE).build();

        //Building the paged list
        testimoniesPagedList = (new LivePagedListBuilder(browseTestimoniesDataFactory, pagedListConfig))
                .build();

        //Getting PagedList config
        PagedList.Config pagedList =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(BrowsedPrayerRequestsDataSource.PAGE_SIZE).build();

        //Building the paged list
        requestsPagedList = (new LivePagedListBuilder(browsePrayerRequestsDataFactory, pagedList))
                .build();

    }

    //pull a fresh list of all testimonies
    public void refreshTestimoniesList(){
        browseTestimoniesDataFactory.getBrowsedTestimoniesLiveDataSource().invalidate();
    }

    public LiveData<PagedList<Testimony>> getBrowsedTestimoniesLiveData() {
        return testimoniesPagedList;
    }

    public void refreshRequestsList(){
        browsePrayerRequestsDataFactory.getBrowsedPrayerRequestsLiveDataSource().invalidate();
    }

    public LiveData<PagedList<PrayerRequest>> getBrowsedRequestsLiveData() {
        return requestsPagedList;
    }

    public void getContentPics(int content_id, int contentType){
        mRepository.getContentPics(content_id,contentType);
    }

    public void deleteContentPic(int pic_id){
        mRepository.deleteContentPic(pic_id);
    }

    //method to post new content (Testimony or Prayer Request)
    //content type: Testimony = 1, Prayer Request = 2
    public void postNewContent(String title, String description, ArrayList<File> imageFilesList,
                                     int categoryId, int userId, int contentType,
                                     PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity){
        mRepository.postNewContent(title, description, imageFilesList, categoryId, userId,
                contentType, postTestimonyActivity,prayerRequestActivity);
    }

    public void editContent(int contentId, String title, String description, ArrayList<File> imageFilesList,
                            int categoryId, int userId, int contentType,
                            PostTestimonyActivity postTestimonyActivity, PostPrayerRequestActivity prayerRequestActivity) {
        mRepository.editContent(contentId, title, description, imageFilesList, categoryId, userId,
                contentType, postTestimonyActivity, prayerRequestActivity);
    }

    //report a testimony or prayer request
    public void reportContent(int userId, int content_id, String comment, int content_type){
        mRepository.reportContent(userId, content_id, comment,content_type);
    }

    //delete content
    public void deleteContent(int userId, int content_id, int content_type){
        mRepository.deleteContent(userId, content_id, content_type);
    }

    //user like content if testimony. Prayed for if prayer request
    public void likeContent(int userId, int content_id, int content_type){
        mRepository.likeContent(userId, content_id, content_type);
    }

    //user unlike content if testimony. Prayed for if prayer request
    public void unLikeContent(int userId, int content_id, int content_type){
        mRepository.unLikeContent(userId, content_id, content_type);
    }

    public LiveData<List<ImageUpload>> getContentPicsDetails(int content_id){
        return mRepository.getContentPicsDetails(content_id);
    }
/*



    //creating livedata for PagedList  and PagedKeyedDataSource
    private LiveData<PagedList<Testimony>> testimoniesPagedList, likedTestimoniesPagedList,
            postedTestimoniesPagedList;
    private LiveData<PagedList<PrayerRequest>> prayersPagedList, prayedForPagedList, postedPrayerRequestsPagedList,
            draftHeritagePagedList;

    private DataSource<Integer, Testimony> testimonyDataSource, likedTestimoniesDataSource,
            postedTestimoniesDataSource;
    private DataSource<Integer, PrayerRequest> prayerDataSource, prayedForDataSource, postedPrayersDataSource;

    public PostedTestimoniesListDataFactory postedTestimoniesListDataFactory;
    public LikedTestimoniesListDataFactory likedTestimoniesListDataFactory;
    public TestimoniesListDataFactory testimoniesListDataFactory;

    public PrayerRequestListDataFactory prayerRequestListDataFactory;
    public PostedPrayerRequestListDataFactory postedPrayerRequestListDataFactory;
    public PrayedForListDataFactory prayedForListDataFactory;



    //constructor that gets a reference to the repository and gets the categories
    public TestimonyPrayerViewModel(LifeIssuesRepository repository, Context context) {
        //super(application);
        mRepository = repository;
        mContext = context;

        //getting our data source factory
        testimoniesListDataFactory = new TestimoniesListDataFactory(mContext);
        postedTestimoniesListDataFactory = new PostedTestimoniesListDataFactory();
        likedTestimoniesListDataFactory = new LikedTestimoniesListDataFactory();

        prayerRequestListDataFactory = new PrayerRequestListDataFactory();
        postedPrayerRequestListDataFactory = new PostedPrayerRequestListDataFactory();
        prayedForListDataFactory = new PrayedForListDataFactory();

        //getting the live data source from data source factory
        testimonyDataSource = testimoniesListDataFactory.getBrowsedTestimoniesLiveDataSource();
        postedTestimoniesPagedList = postedTestimoniesListDataFactory.getPostedTestimoniesLiveDataSource();
        likedTestimoniesDataSource = likedTestimoniesListDataFactory.getLikedTestimoniesLiveDataSource();

        prayerDataSource = prayerRequestListDataFactory.getBrowsedPrayerrequestsLiveDataSource();
        postedPrayersDataSource = postedPrayerRequestListDataFactory.getPostedPrayerRequestsLiveDataSource();
        prayedForDataSource = prayedForListDataFactory.getPrayedForLiveDataSource();

        //Getting PagedList config for testimonies
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(TestimonyListDataSource.PAGE_SIZE).build();

        //Building the paged list
        testimoniesPagedList = new LivePagedListBuilder<>(testimoniesListDataFactory, pagedListConfig)
                .build();

        //Getting PagedList config for posted testimoines
        PagedList.Config postedTestimoniesPagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(PostedTestimoniesListDataSource.PAGE_SIZE).build();

        //Building the paged list
        postedTestimoniesPagedList = new LivePagedListBuilder<>(postedTestimoniesListDataFactory, postedTestimoniesPagedListConfig)
                .build();

        //Getting PagedList config for liked testimonies
        PagedList.Config likedTestimoniesPagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(LikedTestimonyListDataSource.PAGE_SIZE).build();

        //Building the paged list
        likedTestimoniesPagedList = new LivePagedListBuilder<>(likedTestimoniesListDataFactory, likedTestimoniesPagedListConfig)
                .build();

        //Getting PagedList config for prayer requests
        PagedList.Config prayerRequestsPagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(PrayerRequestsListDataSource.PAGE_SIZE).build();

        //Building the paged list
        prayersPagedList = (new LivePagedListBuilder<>(prayerRequestListDataFactory, prayerRequestsPagedListConfig))
                .build();

        //Getting PagedList config for posted prayer requests
        PagedList.Config postedPrayerRequestsPagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(PostedPrayerRequestsListDataSource.PAGE_SIZE).build();

        //Building the paged list
        postedPrayerRequestsPagedList = (new LivePagedListBuilder<>(postedPrayerRequestListDataFactory, postedPrayerRequestsPagedListConfig))
                .build();

        //Getting PagedList config for liked heritage
        PagedList.Config likedHeritagePagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(PrayedForListDataSource.PAGE_SIZE).build();

        //Building the paged list
        prayedForPagedList = (new LivePagedListBuilder<>(prayedForListDataFactory, likedHeritagePagedListConfig))
                .build();
    }

    public LiveData<PagedList<Testimony>> getTestimoniesList() {
        return testimoniesPagedList;
    }

    //get the our prayer requests posts
    public LiveData<PagedList<PrayerRequest>> getPrayerRequestList() {
        return prayersPagedList;
    }

    //get the prayer request details
    public LiveData<PrayerRequest> getPrayerRequestDetails(int userId, int prayerId){
        return mRepository.getPrayerRequestDetails(userId, prayerId);
    }

    //get the testimony details
    public LiveData<Testimony> getTestimonyDetails(int userId, int testimonyId){
        return mRepository.getTestimonyDetails(userId, testimonyId);
    }

    //get the posted testimony details
    public LiveData<PagedList<Testimony>> getPostedTestimoniesList() {
        return postedTestimoniesPagedList;
    }

    //get the liked testimonies details
    public LiveData<PagedList<Testimony>> getLikedTestimoniesList() {
        return likedTestimoniesPagedList;
    }

    //get the liked prayer request details
    public LiveData<PagedList<PrayerRequest>> getPrayedForList() {
        return prayedForPagedList;
    }

    //get the posted prayer requests details
    public LiveData<PagedList<PrayerRequest>> getPostedPrayerRequestsList() {
        return postedPrayerRequestsPagedList;
    }

    //pull a fresh list of posted testimonies
    public void refreshPostedTestimoniesList(){
        postedTestimoniesListDataFactory.getPostedTestimoniesLiveDataSource().invalidate();
    }

    //pull a fresh list of liked testimonies
    public void refreshLikedTestimoniesList(){
        likedTestimoniesListDataFactory.getLikedTestimoniesLiveDataSource().invalidate();
    }

    //pull a fresh list of all testimonies
    public void refreshTestimoniesList(){
        testimoniesListDataFactory.getBrowsedTestimoniesLiveDataSource().invalidate();
    }

    //pull a fresh list of prayer requests
    public void refreshPrayerRequestList(){
        prayerRequestListDataFactory.getBrowsedPrayerRequestLiveDataSource().invalidate();
    }

    //pull a fresh list of liked prayer requests
    public void refreshLikedPrayerRequestsList(){
        prayedForListDataFactory.getLikedPrayerRequestsLiveDataSource().invalidate();
    }

    //pull a fresh list of posted prayer requests
    public void refreshPostedPrayerRequestsList(){
        postedPrayerRequestListDataFactory.getPostedPrayerRequestsLiveDataSource().invalidate();
    }

    //user like testimony
    public void likeTestimony(int userId, int testimonyId, TestimonyDetailsActivity activityInstance){
        mRepository.likeTestimony(userId, testimonyId, activityInstance);
    }

    //user unlike testimony
    public void unLikeTestimony(int userId, int testimonyId, TestimonyDetailsActivity activityInstance){
        mRepository.unLikeTestimony(userId, testimonyId, activityInstance);
    }

    //user like prayer request
    public void likePrayerRequest(int userId, int heritageId, PrayerRequestDetailsActivity activityInstance){
        mRepository.likePrayerRequest(userId, heritageId, activityInstance);
    }

    //user unlike prayer request
    public void unLikePrayerRequest(int userId, int heritageId, PrayerRequestDetailsActivity activityInstance){
        mRepository.unLikePrayerRequest(userId, heritageId, activityInstance);
    }

    //user report a testimony
    public void reportIssue(int userId, String issue_category, int issue_id, String comment, TestimonyDetailsActivity activity){
        mRepository.reportIssue(userId, issue_category, issue_id, comment, activity);
    }

    //user report prayer request post
    public void reportIssue(int userId, String issue_category, int issue_id, String comment, PrayerRequestDetailsActivity activity){
        mRepository.reportIssue(userId, issue_category, issue_id, comment, activity);
    }
*/

}
