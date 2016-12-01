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

    public static Bundle createArgs(String agency, String routeTag, String stopTag, Link link) {
        final Bundle bundle = new Bundle();
        bundle.putString(ARG_AGENCY_TAG, agency);
        bundle.putString(ARG_ROUTE_TAG, routeTag);
        bundle.putString(ARG_STOP_TAG, stopTag);
        bundle.putSerializable(ARG_LINK_TAG, link);
        return bundle;
    }

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

        final View itemView = LayoutInflater
            .from(getContext())
            .inflate(R.layout.dialog_bus_alarm, null);
        final EditText alarmSetInput = (EditText) itemView.findViewById(R.id.alarm_set_input);
        final TextView toneNameText = (TextView) itemView.findViewById(R.id.tone_set_text);

        toneNameText.setText(getRingtoneTitle(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)));

        MainActivity activity = (MainActivity) getActivity();

        activity.addOnActivityResultCallback(RINGTONE_REQUEST, (requestCode, resultCode, intent) -> {
            chosenRingtone = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            toneNameText.setText(getRingtoneTitle(chosenRingtone));
        });

        toneNameText.setOnClickListener(view -> {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            getActivity().startActivityForResult(intent, RINGTONE_REQUEST);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(WordUtils.capitalize(routeTag) + " bus at " + stopTag)
            .setView(itemView)
            .setPositiveButton("Set Alarm", (dialogInterface, i) -> {
                int minutes = 5;
                try {
                    minutes = Integer.parseInt(alarmSetInput.getText().toString());
                } catch (NumberFormatException ignored) { }
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
            .setNegativeButton("Cancel", ((dialogInterface, i) -> {}));

        return builder.create();
    }
}
