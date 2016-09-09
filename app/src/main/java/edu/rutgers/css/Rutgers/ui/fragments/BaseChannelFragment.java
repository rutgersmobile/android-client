package edu.rutgers.css.Rutgers.ui.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.link.Linkable;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import io.github.yavski.fabspeeddial.FabSpeedDial;
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
    private boolean setUp;

    public static final int DEF_PROGRESS_RES = R.id.progressCircle;
    public static final int DEF_TOOLBAR_RES = R.id.toolbar;
    public static final int DEF_FAB_RES = R.id.fab_speed_dial;

    protected static final int LOCATION_REQUEST = 101;

    @Builder
    @Data
    public static final class CreateArgs {
        final Integer progressRes;
        final Integer toolbarRes;
        final Integer fabRes;
    }

    @Getter
    private ActionBar actionBar;

    public static final String TAG = "BaseChannelFragment";

    protected void safeForceLoad(int loaderId) {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            final LoaderManager loaderManager = activity.getSupportLoaderManager();
            if (loaderManager != null) {
                final Loader loader = loaderManager.getLoader(loaderId);
                if (loader != null) {
                    loader.forceLoad();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                args.getFabRes()
        );
    }

    final protected View createView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState,
                                    int resource, Integer progressRes, Integer toolbarRes, Integer fabRes) {
        final View v = inflater.inflate(resource, parent, false);

        progressRes = progressRes == null ? DEF_PROGRESS_RES : progressRes;
        toolbarRes = toolbarRes == null ? DEF_TOOLBAR_RES : toolbarRes;
        fabRes = fabRes == null ? DEF_FAB_RES : fabRes;

        setupProgressBar(v, progressRes);
        setupToolbar(v, toolbarRes);
        setupToggleFab(v, fabRes);

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

    protected void setupToggleFab(final @NonNull View v, final int fabRes) {
        final FabSpeedDial fab = (FabSpeedDial) v.findViewById(fabRes);
        if (fab == null) {
            return;
        }

        fab.setMenuListener(new FabSpeedDial.MenuListener() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_share:
                        startActivity(Intent.createChooser(getShareIntent(), "Share via"));
                        return true;
                    case R.id.action_bookmark:
                        PrefUtils.addBookmark(getContext(), 0, getLink());
                        Toast.makeText(getContext(), R.string.bookmark_added, Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onMenuClosed() { }
        });
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

    protected boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    protected void requestGPS() {
        final PackageManager pm = getContext().getPackageManager();
        if (PrefUtils.getGPSRequest(getContext())
                && (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)
                || pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS))) {
            PrefUtils.setGPSRequest(getContext(), false);
            InfoDialogFragment f = InfoDialogFragment.gpsDialog();
            f.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST);
                }
            });
            f.show(getFragmentManager(), BaseChannelFragment.TAG);
        }
    }

    public String getLinkTitle() {
        return getActivity().getTitle().toString();
    }
}
