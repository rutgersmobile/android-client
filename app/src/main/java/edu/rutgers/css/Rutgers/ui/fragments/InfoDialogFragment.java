package edu.rutgers.css.Rutgers.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import lombok.Setter;

/**
 * Generic class for creating info dialogs
 */
public class InfoDialogFragment extends DialogFragment {
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";

    public static final String TAG = "InfoDialogFragment";

    public static InfoDialogFragment gpsDialog() {
        return newInstance("GPS Permission Request",
            "To get nearby bus stops, we need access to your location. To allow this, press \"Allow\" in the following dialog. This is not required.");
    }
    @Setter
    private DialogInterface.OnDismissListener onDismissListener;

    public static InfoDialogFragment newInstance(String title, String message) {
        InfoDialogFragment f = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        onDismissListener.onDismiss(dialog);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String message = getArguments().getString(MESSAGE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setNeutralButton("Close", null);
        return builder.create();
    }
}
