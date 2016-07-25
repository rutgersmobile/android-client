package edu.rutgers.css.Rutgers.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Popup that shows Motd information
 */
public class LinkErrorDialogFragment extends DialogFragment {
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    public static final String TAG = "LinkErrorDialogFragment";

    public static final LinkErrorDialogFragment defaultError = newInstance("Link Error", "Invalid link");

    public static LinkErrorDialogFragment newInstance(String title, String message) {
        LinkErrorDialogFragment f = new LinkErrorDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        f.setArguments(args);
        return f;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String motd = getArguments().getString(MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(motd)
                .setNeutralButton("Close", null);
        return builder.create();
    }
}
