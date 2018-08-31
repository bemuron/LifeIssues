package com.lifeissues.lifeissues.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.FavouriteVerse;
import com.lifeissues.lifeissues.models.LifeIssue;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Emo on 5/10/2017.
 */

public class FavouriteVersesListAdapter extends RecyclerView.Adapter<FavouriteVersesListAdapter.MyViewHolder> {

    private List<FavouriteVerse> favouriteVerses;
    private LayoutInflater inflater;
    Context context;
    private FavVersesListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView issue_name, bibleVerse;
        public LinearLayout verseContainer;
        //public RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            issue_name = (TextView) view.findViewById(R.id.issue_name);
            bibleVerse = (TextView) view.findViewById(R.id.verse_name);
            verseContainer = (LinearLayout) view.findViewById(R.id.verse_container);
            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
    }

    public FavouriteVersesListAdapter(Context context, List<FavouriteVerse> favVerse, FavVersesListAdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.favouriteVerses = favVerse;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fav_verse_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        FavouriteVerse verse = favouriteVerses.get(position);

        // displaying text view data
        holder.bibleVerse.setText(verse.getVerse());
        holder.issue_name.setText(verse.getIssueName());

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, position);

    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, final int position) {
        holder.verseContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return favouriteVerses.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return favouriteVerses.size();
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
        favouriteVerses.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface FavVersesListAdapterListener {

        void onMessageRowClicked(int position);

    }

}
