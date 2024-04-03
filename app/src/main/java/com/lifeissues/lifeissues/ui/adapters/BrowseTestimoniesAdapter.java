package com.lifeissues.lifeissues.ui.adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.Testimony;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BrowseTestimoniesAdapter extends PagedListAdapter<Testimony, RecyclerView.ViewHolder> {
    private static final String LOG_TAG = BrowseTestimoniesAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_BROWSE_ADVERTS = 1;
    private static final int VIEW_TYPE_POSTER_ADVERTS = 2;
    private static final int VIEW_TYPE_ALL_SIMILAR_ADVERTS = 3;
    private LayoutInflater inflater;
    private Context context;
    private int viewType;
    private BrowseTestimoniesListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public BrowseTestimoniesAdapter(Context context, BrowseTestimoniesListAdapterListener listener, int viewType){
        super(DIFF_CALLBACK);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
        this.viewType = viewType;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();

    }

    class BrowseTestimoniesItemViewHolder extends RecyclerView.ViewHolder {

        public TextView title, summary,
                posterName,datePostedTv;
        public ImageView contentImage;
        public CardView contentContainer;
        public RelativeLayout iconContainer;

        public BrowseTestimoniesItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.content_title);
            summary = view.findViewById(R.id.content_summary);
            posterName = view.findViewById(R.id.content_poster);
            contentImage = view.findViewById(R.id.content_image_profile);
            contentContainer = view.findViewById(R.id.content_card_view);
            datePostedTv = view.findViewById(R.id.date_posted);
            iconContainer = view.findViewById(R.id.content_icon_container);
        }

        void bind(Testimony testimony, int position) {
            String datePosted = null;

            //date format for dates coming from server
            SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

            SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // displaying text view data
            title.setText(testimony.getTestimony_name());
            summary.setText(testimony.getContent());
            posterName.setText("By "+testimony.getUser_name().split(" ")[0]);

            try{
                Date d = mysqlDateTimeFormat.parse(testimony.getPosted_on());
                d.setTime(d.getTime());
                datePosted = myFormat.format(d);
            }catch (Exception e){
                e.printStackTrace();
            }
            datePostedTv.setText(datePosted);

            contentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTestimonyRowClicked(position);
                }
            });

            if (!TextUtils.isEmpty(testimony.getImage_name())) {
                //Log.e(LOG_TAG, "Getting profile pic "+ job.getProfile_pic());
                Glide.with(context).load("https://vottademo.emtechint.com/public/assets/images/content/"+testimony.getImage_name())
                        .thumbnail(0.5f)
                        .transition(withCrossFade())
                        .into(contentImage);
                contentImage.setColorFilter(null);
                //holder.iconText.setVisibility(View.INVISIBLE);
            } else {
                iconContainer.setVisibility(View.GONE);
                //contentImage.setImageResource(R.drawable.bg_circle);
                //contentImage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
//        if (viewType == 1) {
//            return VIEW_TYPE_BROWSE_ADVERTS;
//        } else if(viewType == 2) {
//            return VIEW_TYPE_POSTER_ADVERTS;
//        }
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.browse_content_list_item, viewGroup, false);
        return new BrowseTestimoniesItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Testimony testimony = getItem(position);
        ((BrowseTestimoniesItemViewHolder) holder).bind(testimony, position);

    }

    /*public void setList(List<Job> job) {
        this.jobList = job;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return jobList.get(position).getJob_id();
    }*/

    /*@Override
    public int getItemCount() {
        return jobList.size();
    }*/

    private static DiffUtil.ItemCallback<Testimony> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Testimony>() {
                @Override
                public boolean areItemsTheSame(Testimony oldItem, Testimony newItem) {
                    return oldItem.getTestimony_id() == newItem.getTestimony_id();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(Testimony oldItem, @NonNull Testimony newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface BrowseTestimoniesListAdapterListener {

        void onTestimonyRowClicked(int position);

    }
}
