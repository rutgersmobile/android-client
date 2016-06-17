package edu.rutgers.css.Rutgers.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.link.Linkable;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.ui.ToggleFloatingActionButton;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * Base channel fragment. Handles progress circle display and communication with parent activity.
 */
public abstract class BaseChannelFragment extends BaseDisplay implements Linkable {

    @Getter
    private ProgressBar mProgressCircle;

    @Getter
    private Toolbar toolbar;

    @Getter
    private ToggleFloatingActionButton toggleFab;

    @Getter
    private boolean setUp;

    public static final int DEF_PROGRESS_RES = R.id.progressBar;
    public static final int DEF_TOOLBAR_RES = R.id.toolbar;
    public static final int DEF_MAIN_FAB_RES = R.id.fab;
    public static final int DEF_SHARE_FAB_RES = R.id.mini_share_fab;
    public static final int DEF_BOOKMARK_FAB_RES = R.id.mini_bookmark_fab;

    @Builder
    @Data
    public static final class CreateArgs {
        final Integer progressRes;
        final Integer toolbarRes;
        final Integer mainFabRes;
        final Integer shareFabRes;
        final Integer bookmarkFabRes;
    }

    private ActionBar actionBar;
    private ChannelManager cm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cm = ((MainActivity) getActivity()).getChannelManager();
    }

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource) {
        return createView(inflater, parent, savedInstanceState, resource, CreateArgs.builder().build());
    }

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState, int resource, final @NonNull CreateArgs args) {
        return createView(inflater,
                parent,
                savedInstanceState,
                resource,
                args.getProgressRes(),
                args.getToolbarRes(),
                args.getMainFabRes(),
                args.getShareFabRes(),
                args.getBookmarkFabRes()
        );
    }

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState,
                                    int resource, Integer progressRes, Integer toolbarRes, Integer mainFabRes,
                                    Integer shareFabRes, Integer bookmarkFabRes) {
        final View v = inflater.inflate(resource, parent, false);

        progressRes = progressRes == null ? DEF_PROGRESS_RES : progressRes;
        toolbarRes = toolbarRes == null ? DEF_TOOLBAR_RES : toolbarRes;
        mainFabRes = mainFabRes == null ? DEF_MAIN_FAB_RES : mainFabRes;
        shareFabRes = shareFabRes == null ? DEF_SHARE_FAB_RES : shareFabRes;
        bookmarkFabRes = bookmarkFabRes == null ? DEF_BOOKMARK_FAB_RES : bookmarkFabRes;

        setupProgressBar(v, progressRes);
        setupToolbar(v, toolbarRes);
        setupToggleFab(v, mainFabRes, shareFabRes, bookmarkFabRes);

        setUp = true;

        return v;
    }

    protected void setupProgressBar(final @NonNull View v, final int progressBarRes) {
        mProgressCircle = (ProgressBar) v.findViewById(progressBarRes);
    }

    protected void setupToolbar(final @NonNull View v, final int toolbarRes) {
        final Toolbar toolbar = (Toolbar) v.findViewById(toolbarRes);
        if (toolbar == null) {
            return;
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        ((MainActivity) getActivity()).syncDrawer();

        this.toolbar = toolbar;
    }

    protected void setupToggleFab(final @NonNull View v, final int mainFabRes, final int shareFabRes, final int bookmarkFabRes) {
        final FloatingActionButton mainFab = (FloatingActionButton) v.findViewById(mainFabRes);
        if (mainFab == null) {
            return;
        }

        final ToggleFloatingActionButton toggleFab = new ToggleFloatingActionButton(getContext(), mainFab, new ArrayList<FloatingActionButton>());

        final FloatingActionButton shareFab = (FloatingActionButton) v.findViewById(shareFabRes);
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleFab.isOpen())
                    startActivity(Intent.createChooser(getShareIntent(), "Share via"));
                toggleFab.closeFab();
            }
        });
        toggleFab.addFab(shareFab);

        final FloatingActionButton bookmarkFab = (FloatingActionButton) v.findViewById(bookmarkFabRes);
        bookmarkFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleFab.isOpen()) {
                    PrefUtils.addBookmark(getContext(), 0, getLink());
                    Toast.makeText(getContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                    toggleFab.closeFab();
                }
            }
        });
        toggleFab.addFab(bookmarkFab);

        this.toggleFab = toggleFab;
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

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
