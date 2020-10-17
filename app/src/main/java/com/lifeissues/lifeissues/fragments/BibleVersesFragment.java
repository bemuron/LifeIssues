package com.lifeissues.lifeissues.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.button.MaterialButton;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.activities.MainActivity;
import com.lifeissues.lifeissues.activities.NoteActivity;
import com.lifeissues.lifeissues.helpers.CircleTransform;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import database.DatabaseTable;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Emo on 9/4/2017.
 */

public class BibleVersesFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private static final String TAG = BibleVersesFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private View rootView;
    private Dialog spinnerDialog, verseImageDialog;
    private Spinner versionSpinner;
    private Button launchVersion;
    private ArrayAdapter<CharSequence> adapter;
    private String bibleVerse, bibleVerse2, bibleVerse3, verseContent,
            favouriteValue, spinnerVerseSelection,issueName,
            msgVerseContent, kjvVerse, ampVerseContent, compare;
    private ImageView notFav, inFav, addNoteIcon, shareIcon, verseImageIcon, verseImage;
    public VersionSelectedListener versionSelectedListener;
    private TextView verse_content, verse_content2, verse_content3, verse2, verse3, verse;
    private CardView cardView2, cardView3;
    private int vID;
    private MaterialButton shareImageButton, saveImageButton;
    private DatabaseTable dbhelper;
    private ProgressBar imageProgressBar;
    private Bitmap bitmap;

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
        rootView = inflater.inflate(R.layout.bible_verse_view, container, false);
        dbhelper = new DatabaseTable(getActivity());

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

        Log.e(TAG, "Verse ID = "+vID);
        //load the image
        loadVerseImage(vID);

        checkForImageTask task = new checkForImageTask();
        task.execute(vID);

        //save image to phone storage
        saveVerseImage(vID);

        //share the verse image
        shareVerseImage(vID);

        setSpinnerAdapter();
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

        //verse image
        //opens the dialog to display the verse image
        verseImageIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verseImageDialog.show();
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
        verseImageIcon = (ImageView) view.findViewById(R.id.verse_image);

        spinnerDialog = new Dialog(getActivity());
        spinnerDialog.setContentView(R.layout.select_version_spinner);
        spinnerDialog.setTitle("Select Bible version");
        versionSpinner = (Spinner) spinnerDialog.findViewById(R.id.version_spinner);
        launchVersion = (Button)spinnerDialog.findViewById(R.id.select_version_button);
        versionSpinner.setOnItemSelectedListener(this);

        //dialog to display the image
        verseImageDialog = new Dialog(getActivity());
        verseImageDialog.setContentView(R.layout.verse_image_display);
        saveImageButton = verseImageDialog.findViewById(R.id.saveImageButton);
        shareImageButton = verseImageDialog.findViewById(R.id.shareImageButton);
        verseImage = verseImageDialog.findViewById(R.id.verseImageView);
        imageProgressBar = verseImageDialog.findViewById(R.id.verse_image_progress);

        verse = view.findViewById(R.id.bible_verse);
        verse2 = (TextView) view.findViewById(R.id.bible_verse2);
        verse3 = (TextView) view.findViewById(R.id.bible_verse3);
        verse_content = (TextView) view.findViewById(R.id.bible_verse_content);
        verse_content2 = (TextView) view.findViewById(R.id.bible_verse_content2);
        verse_content3 = (TextView) view.findViewById(R.id.bible_verse_content3);
        cardView2 = (CardView) view.findViewById(R.id.card_view2);
        cardView3 = (CardView) view.findViewById(R.id.card_view3);
        notFav = (ImageView) view.findViewById(R.id.fav_black);
        inFav = (ImageView) view.findViewById(R.id.fav_yellow);
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

    //downloads the verse image to the user's phone storage
    private void saveVerseImage(int verseId){
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkPermission()) {
                            //get today's date to be added to name of the image
                            Calendar c = Calendar.getInstance();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                            String formattedDate = dateFormat.format(c.getTime());

                            String path = Environment.getExternalStorageDirectory().toString();
                            File myDir = new File(Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");
                            //File myDir = new File(path + "/Life_Issues_Images");
                            myDir.mkdirs();
                            if (!myDir.mkdirs()) {
                                Log.e(TAG, "Life_Issues_Images directory not created");
                            }
                            File file = new File(myDir, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                            if (!file.exists()) {
                                Log.d("path", file.toString());
                                try {
                                    FileOutputStream fos = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                    fos.flush();
                                    fos.close();
                                    Log.d(TAG, "File saved");
                                    Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            requestPermission();
                            Log.e(TAG, "Request for permission");
                        }
                    }else{
                        //get today's date to be added to name of the image
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                        String formattedDate = dateFormat.format(c.getTime());

                        String path = Environment.getExternalStorageDirectory().toString();
                        File myDir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");
                        //File myDir = new File(path + "/Life_Issues_Images");
                        myDir.mkdirs();
                        if (!myDir.mkdirs()) {
                            //myDir.mkdirs();
                            Log.e(TAG, "Life_Issues_Images directory not created");
                        }
                        File file = new File(myDir, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                        if (!file.exists()) {
                            Log.d("path", file.toString());
                            try {
                                FileOutputStream fos = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();
                                fos.close();
                                Log.d(TAG, "File saved");
                                Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                verseImageDialog.dismiss();
            }
        });
    }

    //share the image with other apps
    private void shareVerseImage(int verseID){
        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                    Bitmap bitmap = bitmapDrawable .getBitmap();
                    String bitmapPath = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), bitmap,bibleVerse, null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    startActivity(Intent.createChooser(shareIntent,"Share Verse Image"));
                }catch (Exception e){
                    Log.e(TAG, "ERROR sharing verse image: "+e.getMessage());
                }
                verseImageDialog.dismiss();
            }
        });
        //verseImageDialog.show();
    }

    //load verse image
    private void loadVerseImage(int verseID){
            //load the verse image from the network
            try {
                Glide.with(this)
                        .load("https://www.emtechint.com/lifeissues/verse_images/" + verseID +".png")
                        //.thumbnail(0.5f)
                        //.transition(withCrossFade())
                        //.apply(new RequestOptions().fitCenter())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                imageProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                imageProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                        })
                        .into(verseImage);
            }catch (Exception e){
                Log.e(TAG, "ERROR loading verse image: "+e.getMessage());
            }
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

    //async task checks if the image exists
    //if available, load it
    private class checkForImageTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con =  (HttpURLConnection)
                        new URL("https://www.emtechint.com/lifeissues/verse_images/" + params[0] +".png").openConnection();
                con.setRequestMethod("HEAD");
                System.out.println(con.getResponseCode());
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            if (bResponse)
            {
                loadVerseImage(vID);
            }
            else
            {
                Log.e(TAG, "Image does NOT exist");
                //hide the image icon
                verseImageIcon.setVisibility(View.GONE);
            }
        }
    }

    //check if we have permission to write to external storage
    private boolean requestWriteExternalStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                //ask for permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Permission Request")
                            .setMessage("Permission is required for the Life Issues app to write to storage")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            PERMISSION_REQUEST_CODE);
                                }
                            })
                            .show();
                }else{
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
                return false;
            }else{
                return true; //permission already granted
            }
        }else{
            return true; //if version is less than 23
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission
                (getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private String[] storage_permissions =
            {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission Request")
                    .setMessage("Permission is required for the Life Issues app to read and write to storage")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    storage_permissions,
                                    PERMISSION_REQUEST_CODE);
                        }
                    })
                    .show();
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    storage_permissions,
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permission Granted, Now you can use local drive .");
            } else {
                Log.e(TAG, "Permission Denied, You cannot use local drive .");
            }
            break;
        }
    }

}
