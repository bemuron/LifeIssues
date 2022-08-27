package com.lifeissues.lifeissues.ui.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
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
import androidx.lifecycle.ViewModelProvider;

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
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.button.MaterialButton;
import com.lifeissues.lifeissues.R;
import com.lifeissues.lifeissues.ui.activities.MainActivity;
import com.lifeissues.lifeissues.ui.activities.NoteActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.lifeissues.lifeissues.data.database.DatabaseTable;
import com.lifeissues.lifeissues.ui.viewmodels.BibleVersesActivityViewModel;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_T;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;

/**
 * Created by Emo on 9/4/2017.
 */

public class BibleVersesFragment extends Fragment implements AdapterView.OnItemSelectedListener{
    private static final String TAG = BibleVersesFragment.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 55;
    private View rootView;
    private BibleVersesActivityViewModel viewModel;
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
    private int vID, issueId;
    private MaterialButton shareImageButton, saveImageButton;
    private ProgressBar imageProgressBar;
    private Bitmap bitmap;
    private int isFavorite;
    private InterstitialAd interstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                //.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_T)
                .build();

        MobileAds.setRequestConfiguration(requestConfiguration);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        //setup and initialize the interstitial ads
        // Create the InterstitialAd and set the adUnitId.
        //interstitialAd = new InterstitialAd(getActivity());
        // Defined in res/values/strings.xml
        //interstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        //request for the ad
        //AdRequest adRequest = new AdRequest.Builder().build();
        //load it into the object
        //interstitialAd.loadAd(adRequest);

        viewModel = new ViewModelProvider(this).get(BibleVersesActivityViewModel.class);
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
        }
        verseContent = data.getString("verseContent");
        bibleVerse = data.getString("Verse");

        vID = data.getInt("VerseID");
        issueId = data.getInt("issueID");
        //get issue name
        //getIssueNameAsync task = new getIssueNameAsync();
        //task.execute(issueId);
        issueName = data.getString("issueName");
        isFavorite = data.getInt("favValue");

        //isVerseFavoriteAsync tsk = new isVerseFavoriteAsync();
        //tsk.execute(vID, issueId);

        //Set title
        getActivity().setTitle(issueName.substring(0, 1).toUpperCase() + issueName.substring(1));
    }

    public BibleVersesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.bible_verse_view, container, false);

        getAllWidgets(rootView);
        // Load the saved state if there is one
        if(savedInstanceState != null) {
            versionSpinner.setSelection(savedInstanceState.getInt("currentVerse", 0));
            issueName = savedInstanceState.getString("issueName");
            isFavorite = savedInstanceState.getInt("favValue");
            //favouriteValue = savedInstanceState.getString("favValue");
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

        updateStar(isFavorite);

        Log.e(TAG, "Verse ID = "+vID);

        setSpinnerAdapter();

        notFav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                inFav.setVisibility(View.VISIBLE);
                notFav.setVisibility(View.INVISIBLE);
                //dbhelper.addFavourite(vID,issueId);
                viewModel.addFavorite(vID,issueId);
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

                //dbhelper.deleteFavourite(vID,issueId);
                viewModel.deleteFavourite(vID,issueId);
                //cursor.requery();
                //updateStar(favouriteValue);

                Toast.makeText(getActivity(), "Deleted from favs",
                        Toast.LENGTH_SHORT).show();
            }
        });

        //note editor with click listener to launch it
        addNoteIcon = rootView.findViewById(R.id.note_icon);
        addNoteIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                handleAddNote();
                /*if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    //doActionAfterAd("addNote", 0);
                } else {
                    Log.e(TAG,"Ad did not load");
                    handleAddNote();
                }*/

            }
        });

        //share icon
        shareIcon = (ImageView) rootView.findViewById(R.id.share_icon);
        shareIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                handleShareVerse();
                /*if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    doActionAfterAd("shareVerse", 0);
                } else {
                    Log.e(TAG,"Ad did not load");
                    handleShareVerse();
                }*/
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

        //if internet is available, load and display image
        if (isNetworkAvailable(getActivity())){
            new checkForImageTask().execute(vID);
            //save image to phone storage
            saveVerseImage(vID);

            //share the verse image
            shareVerseImage(vID);
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
        shareImageButton.setClickable(false);
        saveImageButton.setClickable(false);

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
                handleSaveImage(verseId);
                /*if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    doActionAfterAd("saveImage", verseId);
                } else {
                    Log.e(TAG,"Ad did not load");
                    handleSaveImage(verseId);
                }*/
                verseImageDialog.dismiss();
            }
        });
    }

    //share the image with other apps
    private void shareVerseImage(int verseID){
        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleShareImage();
                /*if (interstitialAd != null && interstitialAd.isLoaded()) {
                    interstitialAd.show();
                    doActionAfterAd("shareImage", verseID);
                } else {
                    Log.e(TAG,"Ad did not load");
                    handleShareImage();
                }*/
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
                                shareImageButton.setClickable(false);
                                saveImageButton.setClickable(false);
                                Toast.makeText(getActivity(),"Could not load image",Toast.LENGTH_LONG).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                shareImageButton.setClickable(true);
                                saveImageButton.setClickable(true);
                                imageProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                        })
                        .into(verseImage);
            }catch (Exception e){
                Log.e(TAG, "ERROR loading verse image: "+e.getMessage());
            }
    }

    public void updateStar(int value){
        if(value == 1){
            inFav.setVisibility(View.VISIBLE);
            notFav.setVisibility(View.INVISIBLE);
        } else{
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
        currentState.putInt("favValue", isFavorite);
        //currentState.putString("favValue", favouriteValue);
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

    //async task to get the issue name
    private class getIssueNameAsync extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Integer... arg) {
            Log.e(TAG, "Issue ID = "+arg[0]);
            return null;// dbhelper.getIssueName(arg[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Issue name = "+result);
            issueName = result;
            //Set title
            getActivity().setTitle(result.substring(0, 1).toUpperCase() + result.substring(1));
        }
    }

    //async task to check if verse is favorite
    private class isVerseFavoriteAsync extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Integer... arg) {
            //check if verse is favorite
            return null;// dbhelper.isVerseFavorite(arg[0],arg[1]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.e(TAG, "Is verse favorite = "+result);
            //isFavorite = result;
            //updateStar(result);
        }
    }

    //async task checks if the image exists
    //if available, load it
    private class checkForImageTask extends AsyncTask<Integer, Void, Boolean> {

        /*@Override
        protected void onPreExecute() {

        }*/

        @Override
        protected Boolean doInBackground(Integer... params) {

            try {
                HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con =  (HttpURLConnection)
                        new URL("https://www.emtechint.com/lifeissues/verse_images/" + params[0] +".png").openConnection();
                con.setRequestMethod("HEAD");
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
                verseImageIcon.setVisibility(View.VISIBLE);
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
        //checking for marshmallow devices and above in order to execute runtime
        //permissions
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            int permisionWriteExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permissionReadExternalStorage = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            //declare a list to hold the permissions we want to ask the user for
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (permisionWriteExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            //if the permissions list is not empty, then request for the permission
            if (!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray
                        (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }else {
                return true;
            }
        }else {
            return true;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permission Request")
                    .setMessage("Permission is required for the Life Issues app to read and write to storage")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    },
                                    PERMISSION_REQUEST_CODE);
                        }
                    })
                    .show();
        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        Log.d(TAG, "Permission callback called ----");
        //fill with actual results from the user
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            int currentAPIVersion = Build.VERSION.SDK_INT;
            Map<String, Integer> perms = new HashMap<>();
            if (currentAPIVersion >= Build.VERSION_CODES.M) {
                //initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
            }
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                //check for both permissions
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write and Read external storage permissions granted");
                    //selectJobImage(currentJobImage);
                } else {
                    Log.d(TAG, "Some permissions are not granted, ask again");
                    //permission is denied (this is the first time, when "never ask again" is not checked)
                    //so ask again explaining the use of the permissions
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Permission Request")
                                .setMessage("Permission is required for the app to write and read from storage")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(getActivity(),
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.READ_EXTERNAL_STORAGE},
                                                REQUEST_ID_MULTIPLE_PERMISSIONS);
                                    }
                                })
                                .show();
                    }
                    //permission is denied and never ask again is checked
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        Toast.makeText(getActivity(), "Go to settings and enable permissions",
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }

    //handle save image
    private void handleSaveImage(int verseId){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkPermission()) {
                    //get today's date to be added to name of the image
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    String formattedDate = dateFormat.format(c.getTime());

                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                    bitmap = bitmapDrawable .getBitmap();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentResolver resolver = getActivity().getContentResolver();
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Life_Issues_Images");
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, true);
                        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        try {
                            OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            if (fos != null) {
                                fos.close();
                            }
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, false);
                            resolver.update(imageUri, contentValues, null, null);
                            Log.d(TAG, "File saved");
                            Toast.makeText(getActivity(), "Image saved", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        //String path = getActivity().getExternalFilesDir(null).toString();
                        //File myDir = getActivity().getExternalFilesDir(
                        //      Environment.DIRECTORY_PICTURES);
                        File myDir = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");
                        if (!myDir.mkdirs()) {
                            myDir.mkdirs();
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
                } else {
                    requestPermission();
                    Log.e(TAG, "Request for permission");
                }
            }else{
                //get today's date to be added to name of the image
                Calendar c = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                String formattedDate = dateFormat.format(c.getTime());

                File myDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "/Life_Issues_Images/");

                //File myDir = getActivity().getExternalFilesDir(
                //      Environment.DIRECTORY_PICTURES);
                //File myDir = new File(path + "/Life_Issues_Images");
                //myDir.mkdirs();
                if (!myDir.mkdirs()) {
                    //myDir.mkdirs();
                    Log.e(TAG, "Life_Issues_Images directory not created");
                }
                File file = new File(myDir, "LIIMG_" + formattedDate + "_V" + verseId + ".png");
                if (!file.exists()) {
                    Log.d("path", file.toString());
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        BitmapDrawable bitmapDrawable = ((BitmapDrawable) verseImage.getDrawable());
                        Bitmap bitmap = bitmapDrawable .getBitmap();
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
    }

    //handle share image
    private void handleShareImage(){
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
    }

    //handle share verse text
    private void handleShareVerse(){
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

    //handle add note
    private void handleAddNote(){
        Intent intent = new Intent(MainActivity.getInstance(), NoteActivity.class);
        intent.putExtra("verse", verse.getText().toString());
        intent.putExtra("issueName", issueName);
        intent.putExtra("content", verse_content.getText().toString());
        startActivity(intent);
    }

    //set up the interstitial ad
/*    private void doActionAfterAd(String actionName, int verseId){

        interstitialAd.setAdListener(
                new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        Log.i(TAG,"onAdLoaded()");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        Log.e(TAG,"onAdFailedToLoad() with error: " + error);
                    }

                    @Override
                    public void onAdClosed() {
                        Log.e(TAG,"Interstitial Ad closed");
                        if (actionName.equals("saveImage")){
                            handleSaveImage(verseId);

                        }else if (actionName.equals("shareImage")){
                            handleShareImage();

                        }else if (actionName.equals("shareVerse")){
                            handleShareVerse();
                        }else if (actionName.equals("addNote")){
                            handleAddNote();
                        }
                    }
                });
    }*/

    //method to check for internet connection
    public static boolean isNetworkAvailable(Context context) {
        if(context == null)  return false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }

            else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_status", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_status", "" + e.getMessage());
                }
            }
        }
        Log.i("update_status","Network is available : FALSE ");
        return false;
    }

}
