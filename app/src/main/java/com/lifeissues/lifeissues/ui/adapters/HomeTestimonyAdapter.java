package com.lifeissues.lifeissues.ui.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.CircleTransform;
import com.lifeissues.lifeissues.models.Testimony;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HomeTestimonyAdapter extends PagedListAdapter<Testimony, HomeTestimonyAdapter.ItemViewHolder> {
    private static final String LOG_TAG = HomeTestimonyAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private Context context;
    private TestimonyListAdapterListener listener;
    private SparseBooleanArray selectedItems;
    private Testimony testimony;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public HomeTestimonyAdapter(Context context, TestimonyListAdapterListener listener){
        super(DIFF_CALLBACK);
        inflater = LayoutInflater.from(context);
        this.context = context;
        //this.jobList = jobs;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();

    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView testimony_name, testimony_posted_on, testimony_posted_by,
                iconText;
        public ImageView imgProfile;
        public LinearLayout jobContainer;
        public RelativeLayout iconContainer;

        public ItemViewHolder(View view) {
            super(view);
            testimony_name = view.findViewById(R.id.home_article_title);
            testimony_posted_on = view.findViewById(R.id.home_article_posted_on);
            testimony_posted_by= view.findViewById(R.id.home_article_posted_by);
            imgProfile = view.findViewById(R.id.home_icon_profile);
            iconText = view.findViewById(R.id.home_icon_text);
            jobContainer = view.findViewById(R.id.home_article_container);
            iconContainer = view.findViewById(R.id.home_article_icon_container);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_testimony_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        testimony = getItem(position);
        int jobStatus;
        String datePosted = null;

        //make these texts bold
        SpannableStringBuilder postedBy = new SpannableStringBuilder("Posted By: ");
        SpannableStringBuilder date = new SpannableStringBuilder("Posted On: ");

        postedBy.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, postedBy.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        date.setSpan(new android.text.style.StyleSpan(Typeface.BOLD), 0, date.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //date format for dates coming from server
        SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        //convert to UTC first to enable us convert to local time zone later
        mysqlDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        //the format we want them in
        DateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);

        long millis = 14400000; //add 4 hours to the mysql time to get the UG time

        //set the time zone
        Calendar cal = Calendar.getInstance();
        //TimeZone tz = TimeZone.getDefault();
        TimeZone timeZone = cal.getTimeZone();

        /* debug: is it local time? */
        //Log.e("Device Time zone: ", tz.getDisplayName());

        try{
            Date d = mysqlDateTimeFormat.parse(testimony.getPosted_on()); //date is in utc now
            d.setTime(d.getTime() + millis);
            myFormat.setTimeZone(timeZone);//set the local timezone
            datePosted = myFormat.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }

        // displaying text view data
        Log.e(LOG_TAG, "Testimony title " + testimony.getTestimony_name());
        holder.testimony_name.setText(testimony.getTestimony_name());
        holder.testimony_posted_on.setText(date + datePosted);
        holder.testimony_posted_by.setText(postedBy + testimony.getUser_name().split(" ")[0]);
        //holder.testimony_posted_by.setText(postedBy + testimony.getUser_name());

        // change the row state to activated
        //holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, position);

        // display profile image
        applyProfilePicture(holder, testimony);

    }

    //handling different click events
    private void applyClickEvents(HomeTestimonyAdapter.ItemViewHolder holder, final int position) {
        holder.jobContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onTestimonyRowClicked(position);
            }
        });
    }

    private void applyProfilePicture(HomeTestimonyAdapter.ItemViewHolder holder, Testimony testimony) {
        if (!TextUtils.isEmpty(testimony.getImage_name())) {
            Log.e(LOG_TAG, "Getting profile pic "+ testimony.getImage_name());
            Glide.with(context).load("https://vottademo.emtechint.com/public/assets/images/content/"+testimony.getImage_name())
                    .thumbnail(0.5f)
                    .transition(withCrossFade())
                    .apply(new RequestOptions().fitCenter()
                            .transform(new CircleTransform(context)).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
            holder.iconText.setVisibility(View.INVISIBLE);
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            holder.iconText.setText(testimony.getUser_name().substring(0, 1));
            holder.iconText.setVisibility(View.VISIBLE);
        }
    }

    /*public void setList(List<Job> job) {
        this.jobList = job;
        notifyDataSetChanged();
    }

     */
    /*@Override
    public long getItemId(int position) {
        return jobList.get(position).getJob_id();
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }*/

    private static DiffUtil.ItemCallback<Testimony> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Testimony>() {
                @Override
                public boolean areItemsTheSame(Testimony oldItem, Testimony newItem) {
                    //Log.e(LOG_TAG, "Testimony items are the same");
                    return oldItem.getTestimony_id() == newItem.getTestimony_id();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(Testimony oldItem, @NonNull Testimony newItem) {
                    //Log.e(LOG_TAG, "Testimony old list == new list");
                    return oldItem.equals(newItem);
                }
            };

    public interface TestimonyListAdapterListener {

        void onTestimonyRowClicked(int position);

    }

}
