package com.lifeissues.lifeissues.ui.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.lifeissues.lifeissues.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnStatsDialogListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsDialogFragment extends DialogFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String NOTICE_MSG = "message";
    private static final String NOTICE_STAT_FIGURE = "figure";

    private String mMessage;
    private int mFigure;

    private OnStatsDialogListener mListener;

    public StatisticsDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param message Message for the user.
     * @param figure Title of the dialog.
     * @return A new instance of fragment NoticeDialogFragment.
     */
    public static StatisticsDialogFragment newInstance(int figure, String message) {
        StatisticsDialogFragment fragment = new StatisticsDialogFragment();
        Bundle args = new Bundle();
        args.putString(NOTICE_MSG, message);
        args.putInt(NOTICE_STAT_FIGURE, figure);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_MaterialComponents_Bridge);
        if (getArguments() != null) {
            mMessage = getArguments().getString(NOTICE_MSG);
            mFigure = getArguments().getInt(NOTICE_STAT_FIGURE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        //MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_statistics, null);

        TextView statFigureTv = view.findViewById(R.id.dialog_stat_figure);
        TextView statDescTv = view.findViewById(R.id.dialog_stat_desc);
        statFigureTv.setText(String.valueOf(mFigure));
        statDescTv.setText(mMessage);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)

        //builder.setTitle(mTitle)
          //      .setMessage(mMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(StatisticsDialogFragment.this);
                    }
                });
//                .setNegativeButton(R.string.dialog_notice_cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // Send the negative button event back to the host activity
//                        if (dialog != null) {
//                            dialog.dismiss();
//                        }
//                    }
//                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStatsDialogListener) {
            mListener = (OnStatsDialogListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStatsDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnStatsDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }
}
