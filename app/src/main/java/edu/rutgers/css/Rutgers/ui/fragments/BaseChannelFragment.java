package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ProgressBar;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.utils.FragmentUtils;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends Fragment {

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

    //hack to prevent animations from occurring when popping from backstack
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.fDisableAnimations) {
            Animation a = new Animation() {};
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }


    final protected void showProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    final protected void hideProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

    final protected void switchFragments(Bundle args) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).switchFragments(args);
        }
    }

    final protected void showDialogFragment(DialogFragment dialogFragment, String tag) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).showDialogFragment(dialogFragment, tag);
        }
    }

}
