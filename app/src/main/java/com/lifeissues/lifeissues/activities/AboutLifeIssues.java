package com.lifeissues.lifeissues.activities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.lifeissues.lifeissues.R;

/**
 * Created by Emo on 9/6/2016.
 */
public class AboutLifeIssues {
    static String VersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(),0).versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    public static void Show(Activity callingActivity) {
//Use a Spannable to allow for links highlighting
        SpannableString aboutText = new SpannableString(
                callingActivity.getString(R.string.about_app)+ "\n\n" +
                        callingActivity.getString(R.string.about_life));
//Generate views to pass to AlertDialog.Builder and to set the text
        View about;
        TextView tvAbout,about_app;
        try {
//Inflate the custom view
            LayoutInflater inflater = callingActivity.getLayoutInflater();
            about = inflater.inflate(R.layout.activity_about,
                    (ViewGroup) callingActivity.findViewById(R.id.about_view));
            tvAbout = (TextView) about.findViewById(R.id.about_text);
        }
        catch(InflateException e) {
//Inflater can throw exception, unlikely but default to TextView if it occurs
            about = tvAbout = new TextView(callingActivity);
        }
//Set the about text
        tvAbout.setText(aboutText);
// Now Linkify the text
        Linkify.addLinks(tvAbout, Linkify.ALL);
//Build and show the dialog
        new AlertDialog.Builder(callingActivity)
                .setTitle(callingActivity.getString(R.string.app_name)+ " v."+VersionName(callingActivity))
                .setCancelable(true)
                //.setIcon(R.drawable.logo)
                .setPositiveButton("OK", null)
                .setView(about)
                .show(); //Builder method returns allow for method chaining
    }
}
