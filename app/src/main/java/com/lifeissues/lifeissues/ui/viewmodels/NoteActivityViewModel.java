package com.lifeissues.lifeissues.ui.viewmodels;

import android.app.Application;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.lifeissues.lifeissues.data.LifeIssuesRepository;
import com.lifeissues.lifeissues.models.Note;

public class NoteActivityViewModel extends AndroidViewModel {
    private LifeIssuesRepository issuesRepository;

    public NoteActivityViewModel(@NonNull Application application) {
        super(application);
        issuesRepository = new LifeIssuesRepository(application);
    }

    //get a note
    public Cursor getNote(int noteId){
        return issuesRepository.getNote(noteId);
    }

    //delete a note
    public void deleteNote(Note note){
        issuesRepository.deleteNote(note);
    }

    //update a note
    public void updateNote(Note note){
        issuesRepository.updateNote(note);
    }

    //create a note
    public void createNote(Note note){
        issuesRepository.createNote(note);
    }

    //get all notes
    public Cursor getNotes(){
        return issuesRepository.getNotes();
    }

    //delete favourite note
    public void deleteFavNote(String favValue, int verseId){
        issuesRepository.deleteFavNote(favValue,verseId);
    }

    //add fav note
    public void addFavNote(String favValue, int verseId){
        issuesRepository.addFavNote(favValue,verseId);
    }


}
