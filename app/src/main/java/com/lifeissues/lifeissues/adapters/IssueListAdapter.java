package com.lifeissues.lifeissues.adapters;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.LifeIssue;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emo on 5/10/2017.
 */

public class IssueListAdapter extends RecyclerView.Adapter<IssueListAdapter.MyViewHolder> {

    private List<LifeIssue> lifeIssues;
    private LayoutInflater inflater;
    Context context;
    private IssueListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView issue_name, versesNumber, issueVerses;
        public ImageView iconImp;
        public LinearLayout issueContainer;
        //public RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            issue_name = (TextView) view.findViewById(R.id.issue_name);
            issueVerses = (TextView) view.findViewById(R.id.issue_verses);
            versesNumber = (TextView) view.findViewById(R.id.num_of_verses);
            iconImp = (ImageView) view.findViewById(R.id.icon_star);
            issueContainer = (LinearLayout) view.findViewById(R.id.issue_container);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    public IssueListAdapter(Context context, List<LifeIssue> issues, IssueListAdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.lifeIssues = issues;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.issue_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        LifeIssue issue = lifeIssues.get(position);

        // displaying text view data
        holder.issue_name.setText(issue.getIssueName());
        holder.issueVerses.setText(issue.getVerses());
        holder.issueVerses.append(",...");
        holder.versesNumber.setText(issue.getNum_of_verses());
        holder.versesNumber.append(" verses");

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, position);

        // handle star
        applyImportant(holder, issue);

    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, final int position) {
        holder.issueContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

        holder.iconImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconImportantClicked(position);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return lifeIssues.get(position).getId();
    }

    private void applyImportant(MyViewHolder holder, LifeIssue issue) {
        if (issue.isImportant()) {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp));
            holder.iconImp.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint_selected));
        } else {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_border_black_24dp));
            holder.iconImp.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint_normal));
        }
    }

    @Override
    public int getItemCount() {
        return lifeIssues.size();
    }

    public void toggleSelection(int pos) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            animationItemsIndex.delete(pos);
        } else {
            selectedItems.put(pos, true);
            animationItemsIndex.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        reverseAllAnimations = true;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        lifeIssues.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface IssueListAdapterListener {

        void onMessageRowClicked(int position);

        void onIconImportantClicked(int position);

    }

}
