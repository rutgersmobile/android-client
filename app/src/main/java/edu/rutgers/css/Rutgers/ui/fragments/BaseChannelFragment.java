package edu.rutgers.css.Rutgers.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private boolean open = false;

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        final View v = inflater.inflate(resource, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        final FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        final FloatingActionButton shareFab = (FloatingActionButton) v.findViewById(R.id.mini_share_fab);
        final FloatingActionButton bookmarkFab = (FloatingActionButton) v.findViewById(R.id.mini_bookmark_fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(shareFab, "translationY", 0);
                        shareAnimator.setDuration(1000);
                        shareAnimator.start();

                        ObjectAnimator bookmarkAnimator = ObjectAnimator.ofFloat(shareFab, "translationY", 0);
                        bookmarkAnimator.setDuration(1000);
                        bookmarkAnimator.start();

                        open = false;
                    } else {
                        ObjectAnimator shareAnimator = ObjectAnimator.ofFloat(shareFab, "translationY", -200);
                        shareAnimator.setDuration(1000);
                        shareAnimator.start();

                        ObjectAnimator bookmarkAnimator = ObjectAnimator.ofFloat(bookmarkFab, "translationY", -325);
                        bookmarkAnimator.setDuration(1000);
                        bookmarkAnimator.start();

                        open = true;
                    }
                }
            });

            shareFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        startActivity(Intent.createChooser(getShareIntent(), "Share via"));
                    }
                }
            });

            bookmarkFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (open) {
                        PrefUtils.addBookmark(getContext(), getLink());
                        Toast.makeText(getContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                    }
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

    protected Intent getShareIntent() {
        Uri uri = getLink().getUri(Config.SCHEMA);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        return intent;
    }

    public void setShareIntent() {
        final Intent intent = getShareIntent();
        final ShareActionProvider shareActionProvider = getShareActionProvider();
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(intent);
        }
    }

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
