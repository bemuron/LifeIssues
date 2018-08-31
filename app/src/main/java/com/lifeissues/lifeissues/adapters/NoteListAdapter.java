package com.lifeissues.lifeissues.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.helpers.CircleTransform;
import com.lifeissues.lifeissues.helpers.FlipAnimator;
import com.lifeissues.lifeissues.helpers.Note;
//import bruca.prads.model.Message;


/**
 * Created by Emo on 5/10/2017.
 */

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.MyViewHolder> {

    private List<Note> allNotes;
    private LayoutInflater inflater;
    Context context;
    private NoteListAdapterListener listener;
    private SparseBooleanArray selectedItems;

    // array used to perform multiple animation at once
    private SparseBooleanArray animationItemsIndex;
    private boolean reverseAllAnimations = false;

    // index is used to animate only the selected row
    // dirty fix, find a better solution
    private static int currentSelectedIndex = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, content, date, iconText, issueName;
        public ImageView iconImp, imgProfile;
        public LinearLayout messageContainer;
        public RelativeLayout iconContainer, iconBack, iconFront;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.text_view_note_title);
            content = (TextView) view.findViewById(R.id.text_view_note_content);
            date = (TextView) view.findViewById(R.id.text_view_note_date);
            issueName = (TextView) view.findViewById(R.id.text_view_life_issue);
            iconText = (TextView) view.findViewById(R.id.icon_text);
            iconBack = (RelativeLayout) view.findViewById(R.id.icon_back);
            iconFront = (RelativeLayout) view.findViewById(R.id.icon_front);
            iconImp = (ImageView) view.findViewById(R.id.icon_star);
            imgProfile = (ImageView) view.findViewById(R.id.icon_profile);
            messageContainer = (LinearLayout) view.findViewById(R.id.message_container);
            iconContainer = (RelativeLayout) view.findViewById(R.id.icon_container);
            //view.setOnLongClickListener(this);
        }
/*
        @Override
        public boolean onLongClick(View view) {
            listener.onRowLongClicked(getAdapterPosition());
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }
        */
    }

    public NoteListAdapter(Context context, List<Note> notes, NoteListAdapterListener listener) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.allNotes = notes;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
        animationItemsIndex = new SparseBooleanArray();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Note note = allNotes.get(position);

        // displaying text view data
        holder.title.setText(note.getTitle());
        holder.content.setText(note.getContent());
        holder.issueName.setText("Issue: "+ note.getIssueName());
        holder.issueName.append("  Verse: "+ note.getVerse());
        holder.date.setText(note.getDateCreated());

        // displaying the first letter of From in icon text
        holder.iconText.setText(note.getTitle().substring(0, 1));

        // change the row state to activated
        //holder.itemView.setActivated(selectedItems.get(position, false));

        // change the font style depending on message read status
        applyReadStatus(holder, note);

        // handle message star
        applyImportant(holder, note);

        // handle icon animation
        //applyIconAnimation(holder, position);

        // display profile image
        applyProfilePicture(holder, note);

        // apply click events
        applyClickEvents(holder, position);

    }

    //handling different click events
    private void applyClickEvents(MyViewHolder holder, final int position) {
        holder.iconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconClicked(position);
            }
        });

        holder.iconImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onIconImportantClicked(position);
            }
        });

        holder.messageContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onMessageRowClicked(position);
            }
        });
/*
        holder.messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onRowLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
        */
    }

    private void applyProfilePicture(MyViewHolder holder, Note note) {
        if (!TextUtils.isEmpty(note.getPicture())) {
            Glide.with(context).load(note.getPicture())
                    .thumbnail(0.5f)
                    .crossFade()
                    .transform(new CircleTransform(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
            holder.iconText.setVisibility(View.GONE);
        } else {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            holder.imgProfile.setColorFilter(note.getColor());
            holder.iconText.setVisibility(View.VISIBLE);
        }
    }
/*
    private void applyIconAnimation(MyViewHolder holder, int position) {
        if (selectedItems.get(position, false)) {
            holder.iconFront.setVisibility(View.GONE);
            resetIconYAxis(holder.iconBack);
            holder.iconBack.setVisibility(View.VISIBLE);
            holder.iconBack.setAlpha(1);
            if (currentSelectedIndex == position) {
                FlipAnimator.flipView(context, holder.iconBack, holder.iconFront, true);
                resetCurrentIndex();
            }
        } else {
            holder.iconBack.setVisibility(View.GONE);
            resetIconYAxis(holder.iconFront);
            holder.iconFront.setVisibility(View.VISIBLE);
            holder.iconFront.setAlpha(1);
            if ((reverseAllAnimations && animationItemsIndex.get(position, false)) || currentSelectedIndex == position) {
                FlipAnimator.flipView(context, holder.iconBack, holder.iconFront, false);
                resetCurrentIndex();
            }
        }
    }

    // As the views will be reused, sometimes the icon appears as
    // flipped because older view is reused. Reset the Y-axis to 0
    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    public void resetAnimationIndex() {
        reverseAllAnimations = false;
        animationItemsIndex.clear();
    }
*/

    @Override
    public long getItemId(int position) {
        return allNotes.get(position).getId();
    }

    private void applyImportant(MyViewHolder holder, Note note) {
        if (note.isImportant()) {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_black_24dp));
            holder.iconImp.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint_selected));
        } else {
            holder.iconImp.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_star_border_black_24dp));
            holder.iconImp.setColorFilter(ContextCompat.getColor(context, R.color.icon_tint_normal));
        }
    }

    private void applyReadStatus(MyViewHolder holder, Note note) {
        if (note.isRead()) {
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.content.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.subject));
            holder.content.setTextColor(ContextCompat.getColor(context, R.color.message));
        } else {
            holder.title.setTypeface(null, Typeface.BOLD);
            holder.content.setTypeface(null, Typeface.BOLD);
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.from));
            holder.content.setTextColor(ContextCompat.getColor(context, R.color.subject));
        }
    }

    @Override
    public int getItemCount() {
        return allNotes.size();
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
        allNotes.remove(position);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }

    public interface NoteListAdapterListener {
        void onIconClicked(int position);

        void onIconImportantClicked(int position);

        void onMessageRowClicked(int position);

        //void onRowLongClicked(int position);
    }

}
