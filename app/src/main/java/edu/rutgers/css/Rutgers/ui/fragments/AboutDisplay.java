package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;

/**
 * About RUMobile display fragment.
 */
public class AboutDisplay extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "AboutDisplay";
    public static final String HANDLE = "about";

    public AboutDisplay() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_about, parent, false);

        final TextView titleText = (TextView) v.findViewById(R.id.titleText);
        final TextView versionText = (TextView) v.findViewById(R.id.versionText);
        final TextView bodyText = (TextView) v.findViewById(R.id.bodyText);

        titleText.setText(Html.fromHtml(getString(R.string.about_header)));
        versionText.setText(Html.fromHtml(getString(R.string.about_version, Config.VERSION)));
        bodyText.setText(Html.fromHtml(getString(R.string.about_text)));
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

}
