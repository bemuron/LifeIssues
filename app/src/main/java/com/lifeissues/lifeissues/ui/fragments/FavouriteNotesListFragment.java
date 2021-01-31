package com.lifeissues.lifeissues.ui.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.activities.FavouritesActivity;
import com.lifeissues.lifeissues.ui.activities.NoteActivity;
import com.lifeissues.lifeissues.ui.adapters.NoteListAdapter;
import com.lifeissues.lifeissues.helpers.Note;

import java.util.ArrayList;
import java.util.List;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

public class FavouriteNotesListFragment extends Fragment implements
        NoteListAdapter.NoteListAdapterListener {
    View rootView;
    private BibleVersesActivityViewModel viewModel;
    private Cursor c,cursor;
    private List<Note> notes = new ArrayList<>();
    private RecyclerView recyclerView;
    private NoteListAdapter mAdapter;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;
    //private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;

    Bundle extras;

    public FavouriteNotesListFragment() {
    }

    /**
     * Returns a new instance of this fragment
     */
    public static FavouriteNotesListFragment newInstance() {
        FavouriteNotesListFragment fragment = new FavouriteNotesListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_favourite_notes, container, false);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_main, false);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);

        getAllWidgets(rootView);
        setAdapter();

        //async to do stuff in background
        new getNoteTitles().execute();

        return rootView;
    }

    public void getAllWidgets(View view){
        recyclerView = (RecyclerView) view.findViewById(R.id.fav_notes_recycler_view);
        emptyView = (TextView) view.findViewById(R.id.empty_fav_notes_view);
    }

    //setting up the recycler view adapter
    private void setAdapter()
    {
        mAdapter = new NoteListAdapter(FavouritesActivity.getInstance(), notes,this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(FavouritesActivity.getInstance());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(FavouritesActivity.getInstance(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        //actionModeCallback = new ActionModeCallback();
    }

    //async task to get stuff from db
    private class getNoteTitles extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //cursor = dbhelper.getAllFavouriteNotes();
            cursor = viewModel.getAllFavouriteNotes();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getAllGoalNotes();

            //check if there is data to show otherwise display the empty view
            if (notes.isEmpty()){
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void getAllGoalNotes(){
        //swipeRefreshLayout.setRefreshing(true);
        //Cursor cursor = dbhelper.getNotes();
        if (cursor != null){
            cursor.moveToFirst();
            if (notes != null) {
                notes.clear();
            }
            while (!cursor.isAfterLast()){

                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(DatabaseTable.KEY_ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_CONTENT)));
                note.setDateCreated(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_DATE_CREATED)));
                note.setIssueName(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_ISSUE)));
                note.setVerse(cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_NOTE_VERSE)));
                String favValue = cursor.getString(cursor.getColumnIndex(DatabaseTable.KEY_FAVOURITE));
                if (favValue.equals("yes")){
                    note.setImportant(!note.isImportant());
                }
                note.setColor(getRandomMaterialColor("400"));

                notes.add(note);
                cursor.moveToNext();
            }
            cursor.close();
        }
        mAdapter.notifyDataSetChanged();
        //swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", getActivity().getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            //actionMode = getActivity().startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        Note note = notes.get(position);
        if (note.isImportant()){//issue already a fav
            note.setImportant(!note.isImportant());
            notes.set(position, note);
            viewModel.deleteFavouriteNote("no", note.getId());
        }else {
            note.setImportant(!note.isImportant());
            notes.set(position, note);
            viewModel.addFavouriteNote("yes", note.getId());
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageRowClicked(int position) {
        // verify whether action mode is enabled or not
        // if enabled, change the row state to activated
        if (mAdapter.getSelectedItemCount() > 0) {
            //enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            Note note = notes.get(position);
            note.setRead(true);
            notes.set(position, note);
            Intent intent = new Intent(FavouritesActivity.getInstance(), NoteActivity.class);
            intent.putExtra("note-ID", note.getId());
            startActivity(intent);
            //finish();
            mAdapter.notifyDataSetChanged();

        }
    }
/*
    @Override
    public void onRowLongClicked(int position) {
        // long press is performed, enable action mode
        enableActionMode(position);
    }
    */

    private void enableActionMode(int position) {
        if (actionMode == null) {
            //actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }
/*
    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
           // swipeRefreshLayout.setEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    // delete all the selected messages
                    deleteMessages();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            //swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.resetAnimationIndex();
                    // mAdapter.notifyDataSetChanged();
                }
            });
        }
    }
    */

    // deleting the messages from recycler view
    private void deleteMessages() {
        //mAdapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                mAdapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            mAdapter.removeData(selectedItemPositions.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }
}
