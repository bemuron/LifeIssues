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
import com.lifeissues.lifeissues.models.PrayerRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BrowsePrayerRequestAdapter extends PagedListAdapter<PrayerRequest, RecyclerView.ViewHolder> {
    private static final String LOG_TAG = BrowsePrayerRequestAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private Context context;
    private int viewType;
    private BrowseRequestsListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public BrowsePrayerRequestAdapter(Context context, BrowseRequestsListAdapterListener listener){
        super(DIFF_CALLBACK);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();

    }

    class BrowsePrayerRequestsItemViewHolder extends RecyclerView.ViewHolder {

        public TextView title, summary,
                posterName, datePostedTv;
        public ImageView contentImage;
        public CardView contentContainer;
        public RelativeLayout iconContainer;

        public BrowsePrayerRequestsItemViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.content_title);
            summary = view.findViewById(R.id.content_summary);
            posterName = view.findViewById(R.id.content_poster);
            contentImage = view.findViewById(R.id.content_image_profile);
            contentContainer = view.findViewById(R.id.content_card_view);
            datePostedTv = view.findViewById(R.id.date_posted);
            iconContainer = view.findViewById(R.id.content_icon_container);
        }

        void bind(PrayerRequest prayerRequest, int position) {
            String datePosted = null;

            //date format for dates coming from server
            SimpleDateFormat mysqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

            SimpleDateFormat myFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            // displaying text view data
            title.setText(prayerRequest.getPrayer_title());
            summary.setText(prayerRequest.getDescription());
            posterName.setText("By "+prayerRequest.getUser_name().split(" ")[0]);

            try{
                Date d = mysqlDateTimeFormat.parse(prayerRequest.getPosted_on());
                d.setTime(d.getTime());
                datePosted = myFormat.format(d);
            }catch (Exception e){
                e.printStackTrace();
            }
            datePostedTv.setText(datePosted);

            contentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onRequestRowClicked(position);
                }
            });

            if (!TextUtils.isEmpty(prayerRequest.getImage_name())) {
                //Log.e(LOG_TAG, "Getting profile pic "+ job.getProfile_pic());
                Glide.with(context).load("https://vottademo.emtechint.com/public/assets/images/content/"+prayerRequest.getImage_name())
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
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.browse_content_list_item, viewGroup, false);
        return new BrowsePrayerRequestsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PrayerRequest prayerRequest = getItem(position);
        ((BrowsePrayerRequestsItemViewHolder) holder).bind(prayerRequest, position);

    }

    private static DiffUtil.ItemCallback<PrayerRequest> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PrayerRequest>() {
                @Override
                public boolean areItemsTheSame(PrayerRequest oldItem, PrayerRequest newItem) {
                    return oldItem.getPrayer_id() == newItem.getPrayer_id();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(PrayerRequest oldItem, @NonNull PrayerRequest newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface BrowseRequestsListAdapterListener {

        void onRequestRowClicked(int position);

    }
}
