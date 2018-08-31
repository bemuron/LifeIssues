package com.lifeissues.lifeissues.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.models.Note;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import database.DatabaseTable;

/**
 * Created by Emo on 10/2/2017.
 */

public class NoteActivity extends AppCompatActivity {
    private DatabaseTable dbhelper;
    private Cursor c;
    private EditText mTitleEditText,mContentEditText;
    private int noteId;
    private Note mCurrentNote = null;
    private String verse, content, issueName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_note_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupActionBar();
        PreferenceManager.setDefaultValues(this, R.xml.pref_main, false);

        dbhelper = new DatabaseTable(this);

        //extras = getIntent().getExtras();
        //if(extras != null) {
        //noteId = extras.getInt("id");
        //if (noteId > 0) {
        //async to do stuff in background
        Intent intent = getIntent();
        noteId = intent.getIntExtra("note-ID",0);
        verse = intent.getStringExtra("verse");
        content = intent.getStringExtra("content");
        issueName = intent.getStringExtra("issueName");

        if (noteId == 0){
            displayViews();
        }else {
            new getSingleNote().execute();
        }
        //  }
        //}

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveNote()) {
                    makeToast(noteId > 0 ? "Note updated" : "Note saved");
                }
            }
        });


    }//closing onCreate

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //async task to get stuff from db
    private class getSingleNote extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //get intent from which this activity is called
            Intent intent = getIntent();
            //if (intent.getIntExtra("score_explanation-ID",0) == 0){
            c = dbhelper.getNote(noteId);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getSingleNote();
            displayNote();
        }
    }

    //display the note
    private void displayNote() {
        mTitleEditText = (EditText) findViewById(R.id.edit_text_title);
        mTitleEditText.setText(mCurrentNote.getTitle());
        mTitleEditText.setTextSize(15);

        mContentEditText = (EditText) findViewById(R.id.edit_text_note);
        mContentEditText.setText(mCurrentNote.getContent());
        mContentEditText.setTextSize(20);
    }

    private void getSingleNote() {
        //swipeRefreshLayout.setRefreshing(true);
        //Cursor cursor = dbhelper.getNotes();
        if (c != null) {
            c.moveToFirst();
            while (!c.isAfterLast()) {

                mCurrentNote = new Note();
                mCurrentNote.setId(c.getInt(c.getColumnIndex(DatabaseTable.KEY_ID)));
                mCurrentNote.setTitle(c.getString(c.getColumnIndex(DatabaseTable.KEY_NOTE_TITLE)));
                mCurrentNote.setContent(c.getString(c.getColumnIndex(DatabaseTable.KEY_NOTE_CONTENT)));
                mCurrentNote.setDateCreated(c.getString(c.getColumnIndex(DatabaseTable.KEY_DATE_CREATED)));
                c.moveToNext();
            }
            c.close();
        }
    }

    //display edit text views
    private void displayViews(){
        mTitleEditText = (EditText)findViewById(R.id.edit_text_title);
        mContentEditText = (EditText)findViewById(R.id.edit_text_note);
    }

    public void promptForDelete(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete " + mTitleEditText.getText().toString() + " ?");
        alertDialog.setMessage("Are you sure you want to delete this note?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dbhelper.delete(noteId);
                dbhelper.delete(mCurrentNote.getId());
                makeToast(mTitleEditText.getText().toString() + " deleted");
                Intent intent = new Intent(getApplicationContext(),
                        NotesListActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private boolean saveNote(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
        String formattedDate = df.format(c.getTime());

        String title = mTitleEditText.getText().toString();
        if (TextUtils.isEmpty(title)){
            mTitleEditText.setError("Title is required");
            return false;
        }

        String content = mContentEditText.getText().toString();
        if (TextUtils.isEmpty(content)){
            mContentEditText.setError("Content is required");
            return false;
        }

        if (noteId > 0) {
            dbhelper.update(noteId,mTitleEditText.getText().toString(),
                    mContentEditText.getText().toString(),formattedDate);
        }
        else {

            dbhelper.create(mTitleEditText.getText().toString(),
                    formattedDate,mContentEditText.getText().toString(), issueName,verse);
            Intent intent = new Intent(NoteActivity.this, NotesListActivity.class);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private void makeToast(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_note_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_save){
            //save goal
            if (saveNote()) {
                makeToast(noteId > 0 ? "Note updated" : "Note saved");
            }
        } else if (id == R.id.action_delete){
            //delete note
            if (noteId > 0) {
                promptForDelete();
            }else{
                makeToast("No note detected");
            }

        }

        return super.onOptionsItemSelected(item);
    }
}
