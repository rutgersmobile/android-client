package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;

public class TextDisplay extends Fragment {

    /* Log tag and component handle */
    private static final String TAG = "TextDisplay";
    public static final String HANDLE = "text";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_DATA_TAG        = ComponentFactory.ARG_DATA_TAG;

    public TextDisplay() {
        // Required empty public constructor
    }

    /** Create argument bundle for a text display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String data) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, TextDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_DATA_TAG, data);
        return bundle;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_text_display, parent, false);
        final Bundle args = getArguments();

        final TextView textView = (TextView) v.findViewById(R.id.text);

        textView.setMovementMethod(LinkMovementMethod.getInstance());

        if(args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));

        if(args.getString(ARG_DATA_TAG) == null) {
            Log.w(TAG, "No text set");
            textView.setText(getString(R.string.failed_no_text));
        } else {
            textView.setText(Html.fromHtml(args.getString(ARG_DATA_TAG)));
        }

        return v;
    }
}