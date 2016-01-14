package edu.rutgers.css.Rutgers.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Linkable;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends BaseDisplay implements Linkable {

    private ProgressBar mProgressCircle;

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        final View v = inflater.inflate(resource, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrefUtils.addBookmark(getContext(), getLink());
                }
            });
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    final protected void showProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    final protected void hideProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

    final protected void switchFragments(Bundle args) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).getFragmentMediator().switchFragments(args);
        }
    }

    final protected void showDialogFragment(DialogFragment dialogFragment, String tag) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).showDialogFragment(dialogFragment, tag);
        }
    }

    public void setShareIntent() {
        Uri uri = getLink().getUri(Config.SCHEMA);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        getShareActionProvider().setShareIntent(intent);
    }

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
