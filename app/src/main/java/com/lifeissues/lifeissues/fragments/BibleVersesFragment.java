package com.lifeissues.lifeissues.fragments;

import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.lifeissues.lifeissues.activities.BibleVerses;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.MainActivity;
import com.lifeissues.lifeissues.activities.NoteActivity;

import java.util.ArrayList;

import database.DatabaseTable;

/**
 * Created by Emo on 9/4/2017.
 */

public class BibleVersesFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private View rootView;
    private Dialog spinnerDialog;
    private Spinner versionSpinner;
    private Button launchVersion;
    private ArrayAdapter<CharSequence> adapter;
    private String bibleVerse, bibleVerse2, bibleVerse3, verseContent,
            favouriteValue, spinnerVerseSelection,issueName,
            msgVerseContent, kjvVerse, ampVerseContent, compare;
    private ImageView notFav, inFav, addNoteIcon, shareIcon;
    public VersionSelectedListener versionSelectedListener;
    private TextView verse_content, verse_content2, verse_content3, verse2, verse3, verse;
    private CardView cardView2, cardView3;
    private int vID;
    private DatabaseTable dbhelper;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //setUserVisibleHint(false);

        //getting arguments from the bundle object
        Bundle data = getArguments();

        //getting the title and content
        compare = data.getString("compare_mode");
        if (compare != null){
            msgVerseContent = data.getString("msgContent");
            bibleVerse2 = data.getString("msgVerse");
            ampVerseContent = data.getString("ampContent");
            bibleVerse3 = data.getString("ampVerse");
            verseContent = data.getString("verseContent");
            bibleVerse = data.getString("Verse");
        } else {
            verseContent = data.getString("verseContent");
            bibleVerse = data.getString("Verse");
        }

        vID = data.getInt("VerseID");
        issueName = data.getString("issueName");
        favouriteValue = data.getString("favValue");
    }

    public BibleVersesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getAllWidgets(rootView);
        // Load the saved state if there is one
        if(savedInstanceState != null) {
            versionSpinner.setSelection(savedInstanceState.getInt("currentVerse", 0));
            issueName = savedInstanceState.getString("issueName");
            favouriteValue = savedInstanceState.getString("favValue");
            verseContent = savedInstanceState.getString("verseContent");
            bibleVerse = savedInstanceState.getString("Verse");
            vID = savedInstanceState.getInt("VerseID");

            if (compare != null){
                msgVerseContent = savedInstanceState.getString("msgContent");
                bibleVerse2 = savedInstanceState.getString("msgVerse");
                ampVerseContent = savedInstanceState.getString("ampContent");
                bibleVerse3 = savedInstanceState.getString("ampVerse");
            }
        }

        rootView = inflater.inflate(R.layout.bible_verse_view, container, false);
        dbhelper = new DatabaseTable(getActivity());

        setSpinnerAdapter();
        verse = (TextView) rootView.findViewById(R.id.bible_verse);
        verse2 = (TextView) rootView.findViewById(R.id.bible_verse2);
        verse3 = (TextView) rootView.findViewById(R.id.bible_verse3);
        verse_content = (TextView) rootView.findViewById(R.id.bible_verse_content);
        verse_content2 = (TextView) rootView.findViewById(R.id.bible_verse_content2);
        verse_content3 = (TextView) rootView.findViewById(R.id.bible_verse_content3);
        cardView2 = (CardView) rootView.findViewById(R.id.card_view2);
        cardView3 = (CardView) rootView.findViewById(R.id.card_view3);

        notFav = (ImageView) rootView.findViewById(R.id.fav_black);
        inFav = (ImageView) rootView.findViewById(R.id.fav_yellow);
        updateStar(favouriteValue);

        notFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.VISIBLE);
                notFav.setVisibility(View.INVISIBLE);
                dbhelper.addFavourite(vID);
                //adapter.notifyDataSetChanged();
                // new checkFavourite().execute();
                //favouriteValue = cursor.getString(6);
                //Toast.makeText(getActivity(), favouriteValue,
                  //      Toast.LENGTH_SHORT).show();

                //updateStar(favouriteValue);
                Toast.makeText(getActivity(), "Added to favs",
                        Toast.LENGTH_SHORT).show();
                //c1.close();
            }
        });

        inFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.INVISIBLE);
                notFav.setVisibility(View.VISIBLE);

                dbhelper.deleteFavourite(vID);
                //cursor.requery();
                //updateStar(favouriteValue);

                Toast.makeText(getActivity(), "Deleted from favs",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //note editor with click listener to launch it
        addNoteIcon = (ImageView) rootView.findViewById(R.id.note_icon);
        addNoteIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.getInstance(), NoteActivity.class);
                intent.putExtra("verse", verse.getText().toString());
                intent.putExtra("issueName", issueName);
                intent.putExtra("content", verse_content.getText().toString());
                startActivity(intent);

            }
        });

        //share icon
        shareIcon = (ImageView) rootView.findViewById(R.id.share_icon);
        shareIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent sharingIntent = new Intent();
                sharingIntent.setAction(Intent.ACTION_SEND);
                String shareBody = "\n" + verseContent +
                        "\n" + bibleVerse +
                        "\n Life Issues App.";
                //sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                sharingIntent.setType("text/plain");
                startActivity(Intent.createChooser(sharingIntent, "Share verse"));

            }
        });

        //Set title
        getActivity().setTitle(issueName.substring(0, 1).toUpperCase() + issueName.substring(1));

        //set the texts
        //if we are in compare mode, display all versions
        if (compare != null){
            //make the views visible
            cardView2.setVisibility(View.VISIBLE);
            cardView3.setVisibility(View.VISIBLE);
            //verse_content2.setVisibility(View.VISIBLE);
            //verse_content3.setVisibility(View.VISIBLE);

            //set the texts
            verse.setText(bibleVerse);
            verse2.setText(bibleVerse2);
            verse3.setText(bibleVerse3);
            verse_content.setText(verseContent);
            verse_content2.setText(msgVerseContent);
            verse_content3.setText(ampVerseContent);
        }else {
            verse.setText(bibleVerse);
            verse_content.setText(verseContent);
        }


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
    }

    public void getAllWidgets(View view){
        spinnerDialog = new Dialog(getActivity());
        spinnerDialog.setContentView(R.layout.select_version_spinner);
        spinnerDialog.setTitle("Select Bible version");

        versionSpinner = (Spinner) spinnerDialog.findViewById(R.id.version_spinner);

        launchVersion = (Button)spinnerDialog.findViewById(R.id.select_version_button);

        versionSpinner.setOnItemSelectedListener(this);
    }

    //setting up version spinner adapter
    public void setSpinnerAdapter(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.versionsMenu, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        versionSpinner.setAdapter(adapter);
    }

    //Bible Verses must implement this interface
    public interface VersionSelectedListener {
        void onSpinnerSelection(String version);
    }

    @Override
    public void onAttach(Context context) { //Try Context context as the parameter. It is not deprecated
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            versionSelectedListener = (VersionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement VersionSelectedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.bible_verses_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_selectVersion) {
            //Toast.makeText(getActivity(), "select version", Toast.LENGTH_SHORT).show();
            launchBibleVersion();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        int parentId = parent.getId();

        int versionSpinnerItemId = versionSpinner.getId();
        if (parentId == versionSpinnerItemId){
            spinnerVerseSelection = (String)parent.getItemAtPosition(pos);

            versionSpinner.setSelection(adapter.getPosition(spinnerVerseSelection));
        }
        //Toast.makeText(getActivity(), ""+parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //dialog to launch selected version
    private void launchBibleVersion(){
        launchVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    versionSelectedListener.onSpinnerSelection(spinnerVerseSelection);

                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();

                }catch (Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }

                spinnerDialog.dismiss();
            }
        });
        spinnerDialog.show();
    }

    public void updateStar(String value){
        if(value.equals("yes")){
            inFav.setVisibility(View.VISIBLE);
            notFav.setVisibility(View.INVISIBLE);
        } else if (value.equals("no")) {
            inFav.setVisibility(View.INVISIBLE);
            notFav.setVisibility(View.VISIBLE);
        }
    }

    public void updateView(){
        verse_content.setText("");
    }

    /**
     * Save the current state of this fragment
     */
    @Override
    public void onSaveInstanceState(Bundle currentState) {
        currentState.putInt("currentVerse", versionSpinner.getSelectedItemPosition());
        currentState.putString("issueName", issueName);
        currentState.putString("favValue", favouriteValue);
        currentState.putString("verseContent", verseContent);
        currentState.putString("Verse", bibleVerse);
        currentState.putInt("VerseID", vID);

        if (compare != null){
            currentState.putString("msgContent", msgVerseContent);
            currentState.putString("msgVerse", bibleVerse2);
            currentState.putString("ampContent", ampVerseContent);
            currentState.putString("ampVerse", bibleVerse3);
        }

        //calling super makes the fragment store its state in arrays that are not cleaned up
        //leading to memory leaks
        //super.onSaveInstanceState(currentState);
    }

}
