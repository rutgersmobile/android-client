package edu.rutgers.css.Rutgers.channels.bus.model;

import android.app.Dialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.text.WordUtils;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.MainActivity;

/**
 * Dialog for user to customize bus alarm
 */
public class BusNotificationDialogFragment extends DialogFragment {
    public static final int RINGTONE_REQUEST = 5;

    private static final String ARG_AGENCY_TAG = "agency";
    private static final String ARG_ROUTE_TAG = "routeTag";
    private static final String ARG_STOP_TAG = "stopTag";
    private static final String ARG_LINK_TAG = "link";

    private Uri chosenRingtone = null;

    /**
     * Create arguments for Bus Alarm Dialog
     * @param agency Nextbus agency, probably "rutgers"
     * @param routeTag Nextbus route tag, ex. "a", "b", "ee"
     * @param stopTag Nextbus stop tag title, ex. "Scott Hall"
     * @param link Link back to the app that will be run when the user clicks on the created notification
     * @return The argument bundle that should be set with {@link android.support.v4.app.Fragment#setArguments(Bundle)}
     */
    public static Bundle createArgs(String agency, String routeTag, String stopTag, Link link) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_AGENCY_TAG, agency);
        bundle.putString(ARG_ROUTE_TAG, routeTag);
        bundle.putString(ARG_STOP_TAG, stopTag);
        bundle.putSerializable(ARG_LINK_TAG, link);
        return bundle;
    }

    /**
     * Get a {@link Ringtone} title from a Ringtone {@link Uri}
     * @param uri Uri to a Ringtone, probably from {@link RingtoneManager}
     * @return A readable String representation of the Ringtone
     */
    private String getRingtoneTitle(Uri uri) {
        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), uri);
        return ringtone.getTitle(getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();

        final String agency = args.getString(ARG_AGENCY_TAG);
        final String routeTag = args.getString(ARG_ROUTE_TAG);
        final String stopTag = args.getString(ARG_STOP_TAG);
        final Link link = (Link) args.getSerializable(ARG_LINK_TAG);

        // inflate the inner layout for the Dialog
        final View itemView = LayoutInflater
            .from(getContext())
            .inflate(R.layout.dialog_bus_alarm, null);
        // EditText where the user inputs a String for the time before the bus arrives that they
        // want to be alerted
        final EditText alarmSetInput = (EditText) itemView.findViewById(R.id.alarm_set_input);
        // TextView that shows what Ringtone will play when the bus arrives
        final TextView toneNameText = (TextView) itemView.findViewById(R.id.tone_set_text);

        // Set the text to be the default alarm ringtone
        toneNameText.setText(getRingtoneTitle(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)));

        // Launch a ringtone picker dialog when this TextView is clicked
        toneNameText.setOnClickListener(view -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            // If we don't call this from the Activity then our request id will get rewritten
            getActivity().startActivityForResult(intent, RINGTONE_REQUEST);
        });

        MainActivity activity = (MainActivity) getActivity();

        // Register a callback for when the Ringtone picker comes back with a result
        // Use it to set the correct text for the selected Ringtone
        activity.addOnActivityResultCallback(RINGTONE_REQUEST, (requestCode, resultCode, intent) -> {
            chosenRingtone = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            toneNameText.setText(getRingtoneTitle(chosenRingtone));
        });

        // Construct the actual Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(WordUtils.capitalize(routeTag) + " bus at " + stopTag)
            .setView(itemView)
            .setPositiveButton("Set Alarm", (dialogInterface, i) -> {
                int minutes;
                try {
                    minutes = Integer.parseInt(alarmSetInput.getText().toString());
                    if (minutes < 1) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ignored) {
                    // Default to 5 minutes if the user inputs an invalid time
                    minutes = 5;
                }

                // Fire off the service which starts the notification when
                // the user clicks the confirmation
                BusNotificationService.startAlarm(
                    getContext(),
                    agency,
                    routeTag,
                    stopTag,
                    minutes,
                    link,
                    chosenRingtone == null
                        ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        : chosenRingtone
                );
            })
            // Just close the dialog when canceled
            .setNegativeButton("Cancel", ((dialogInterface, i) -> {}));

        return builder.create();
    }
}
