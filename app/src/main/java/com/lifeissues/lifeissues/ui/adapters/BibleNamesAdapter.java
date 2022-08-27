package com.lifeissues.lifeissues.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.BibleName;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class BibleNamesAdapter extends PagedListAdapter<BibleName, BibleNamesAdapter.ItemViewHolder> {

    //private List<Job> jobList;
    private LayoutInflater inflater;
    private Context context;
    private BibleNamesListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;

    public BibleNamesAdapter(Context context, BibleNamesListAdapterListener listener){
        super(DIFF_CALLBACK);
        inflater = LayoutInflater.from(context);
        this.context = context;
        //this.jobList = jobs;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();

    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        public TextView name, meaning;
        public LinearLayout nameContainer;

        public ItemViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name_tv);
            meaning = view.findViewById(R.id.names_meaning_tv);
            nameContainer = view.findViewById(R.id.name_container);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.names_list_item, viewGroup, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        BibleName bibleName = getItem(position);

        // displaying text view data
        holder.name.setText(bibleName.getName());
        holder.meaning.setText(bibleName.getMeaning());

        // change the row state to activated
        holder.itemView.setActivated(selectedItems.get(position, false));

        // apply click events
        applyClickEvents(holder, position);

    }

    //handling different click events
    private void applyClickEvents(BibleNamesAdapter.ItemViewHolder holder, final int position) {
        holder.nameContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onNameRowClicked(position);
            }
        });
    }

    private static DiffUtil.ItemCallback<BibleName> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BibleName>() {
                @Override
                public boolean areItemsTheSame(BibleName oldItem, BibleName newItem) {
                    return oldItem.getNameId() == newItem.getNameId();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(BibleName oldItem, @NonNull BibleName newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public interface BibleNamesListAdapterListener {

        void onNameRowClicked(int position);

    }
}
