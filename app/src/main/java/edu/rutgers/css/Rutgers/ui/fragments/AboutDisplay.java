package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.ui.MainActivity;

/**
 * About RUMobile display fragment.
 */
public final class AboutDisplay extends BaseDisplay {

    /* Log tag and component handle */
    private static final String TAG = "AboutDisplay";
    public static final String HANDLE = "about";

    public AboutDisplay() {
        // Required empty public constructor
    }

    public static Bundle createArgs() {
        Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_HANDLE_TAG, AboutDisplay.HANDLE);
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, AboutDisplay.HANDLE);
        return args;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_about, parent, false);

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        getActivity().setTitle(R.string.about_title);

        final TextView titleText = (TextView) v.findViewById(R.id.titleText);
        final TextView versionText = (TextView) v.findViewById(R.id.versionText);
        final TextView apiText = (TextView) v.findViewById(R.id.apiText);
        final TextView bodyText = (TextView) v.findViewById(R.id.bodyText);

        titleText.setText(Html.fromHtml(getString(R.string.about_header)));
        versionText.setText(Html.fromHtml(getString(R.string.about_version, Config.VERSION)));
        apiText.setText(Config.API_MACHINE + " " + Config.API_LEVEL);
        bodyText.setText(Html.fromHtml(getString(R.string.about_text)));
        bodyText.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }

}
