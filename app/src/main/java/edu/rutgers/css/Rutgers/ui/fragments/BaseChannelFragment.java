package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.ui.MainActivity;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends BaseDisplay {

    private ProgressBar mProgressCircle;

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        final View v = inflater.inflate(resource, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

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

}
