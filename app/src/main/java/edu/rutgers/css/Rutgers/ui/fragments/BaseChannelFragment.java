package edu.rutgers.css.Rutgers.ui.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Linkable;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.ToggleFloatingActionButton;
import edu.rutgers.css.Rutgers.utils.PrefUtils;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends BaseDisplay implements Linkable {

    private ProgressBar mProgressCircle;
    private Toolbar toolbar;
    private ActionBar actionBar;

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        return createView(inflater, parent, savedInstanceState, resource, R.id.toolbar);
    }

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource, int tbResource) {
        final View v = inflater.inflate(resource, parent, false);

        toolbar = (Toolbar) v.findViewById(tbResource);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            ((MainActivity) getActivity()).syncDrawer();
        }

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

        final FloatingActionButton mainFab = (FloatingActionButton) v.findViewById(R.id.fab);
        if (mainFab != null) {
            final ToggleFloatingActionButton toggleFab = new ToggleFloatingActionButton(getContext(), mainFab, new ArrayList<FloatingActionButton>());

            final FloatingActionButton shareFab = (FloatingActionButton) v.findViewById(R.id.mini_share_fab);
            shareFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toggleFab.isOpen())
                        startActivity(Intent.createChooser(getShareIntent(), "Share via"));
                    toggleFab.closeFab();
                }
            });
            toggleFab.addFab(shareFab);

            final FloatingActionButton bookmarkFab = (FloatingActionButton) v.findViewById(R.id.mini_bookmark_fab);
            bookmarkFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (toggleFab.isOpen()) {
                        PrefUtils.addBookmark(getContext(), getLink());
                        Toast.makeText(getContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                        toggleFab.closeFab();
                    }
                }
            });
            toggleFab.addFab(bookmarkFab);
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

    protected ActionBar getActionBar() {
        return actionBar;
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
