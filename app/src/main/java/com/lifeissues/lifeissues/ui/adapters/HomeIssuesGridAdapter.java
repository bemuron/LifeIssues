package com.lifeissues.lifeissues.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.Issue;

import java.util.List;
import java.util.Locale;

public class HomeIssuesGridAdapter extends RecyclerView.Adapter<HomeIssuesGridAdapter.MyViewHolder> {

    private final HomeIssuesListOnItemClickHandler mClickHandler;

    private Context mContext;
    private LayoutInflater inflater;
    //private final CategoriesViewModel mCategoriesViewModel;
    private List<Issue> mIssues;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView categoryName;
        //public LinearLayout itemContainer;
        public ConstraintLayout itemContainer;

        public MyViewHolder(View view) {
            super(view);
            categoryName = view.findViewById(R.id.cat_name);
            imageView = view.findViewById(R.id.cat_pic);
            itemContainer = view.findViewById(R.id.homeGridItemContainer);
            //view.setOnLongClickListener(this);
        }
    }

    public HomeIssuesGridAdapter(List<Issue> issues, Context c,
                                 HomeIssuesListOnItemClickHandler clickHandler){
        this.mContext = c;
        inflater = LayoutInflater.from(c);
        this.mClickHandler = clickHandler;
        this.mIssues = issues;


        //mCategoriesViewModel = categoriesViewModel;
        //setList(categories);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_issues_grid_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Issue issue = mIssues.get(position);

        //displaying the category info
        holder.categoryName.setText(issue.getIssueName());
        String imageName = issue.getImage().toLowerCase(Locale.US);
        int imageId = mContext.getResources().getIdentifier(imageName,
                "drawable", mContext.getPackageName());
        holder.imageView.setImageResource(imageId);

        //apply click events
        applyClickEvents(holder, position);

        //get the issue picture
        //applyIssuePicture(holder, issue);
    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, final int position) {
        holder.itemContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onHomeIssuesItemClick(position);
            }
        });
    }

    //get the issue pic
    private void applyIssuePicture(MyViewHolder holder, Issue issue) {
        if (!TextUtils.isEmpty(issue.getImage())) {
            Glide.with(mContext).load("https://vottademo.emtechint.com/public/assets/images/content/"+issue.getImage())
                    .thumbnail(0.5f)
                    //.crossFade()
                    //.transform(new CircleTransform(context))
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
            holder.imageView.setColorFilter(null);
        }
    }

    @Override
    public long getItemId(int i) {
        return mIssues.get(i).getIssue_id();
    }

    @Override
    public int getItemCount() {
        //return mCategories.size();
        return mIssues != null ? mIssues.size() : 0;
    }

    public void setList(List<Issue> issues) {
        this.mIssues = issues;
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onItemClick messages.
     */
    public interface HomeIssuesListOnItemClickHandler {
        void onHomeIssuesItemClick(int position);
    }

}
