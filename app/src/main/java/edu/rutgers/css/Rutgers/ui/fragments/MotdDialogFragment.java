package edu.rutgers.css.Rutgers.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Popup that shows Motd information
 */
public class MotdDialogFragment extends DialogFragment {
    private static final String TITLE = "title";
    private static final String MOTD = "motd";

    public static final String TAG = "MotdDialogFragment";

    public static MotdDialogFragment newInstance(String title, String motd) {
        MotdDialogFragment f = new MotdDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MOTD, motd);
        f.setArguments(args);
        return f;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String motd = getArguments().getString(MOTD);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(motd)
                .setNeutralButton("Close", null);
        return builder.create();
    }
}
