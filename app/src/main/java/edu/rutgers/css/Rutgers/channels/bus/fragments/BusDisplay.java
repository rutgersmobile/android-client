package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem.ExpandableListItemAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.bus.model.Prediction;
import edu.rutgers.css.Rutgers.api.bus.model.Predictions;
import edu.rutgers.css.Rutgers.channels.bus.model.PredictionAdapter;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.PredictionLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

public class BusDisplay extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<PredictionLoader.PredictionHolder> {

    /* Log tag and component handle */
    private static final String TAG                 = "BusDisplay";
    public static final String HANDLE               = "busdisplay";

    /* Constants */
    private static final int REFRESH_INTERVAL       = 30; // refresh interval in seconds

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;
    private static final String ARG_AGENCY_TAG      = "agency";
    private static final String ARG_MODE_TAG        = "mode";
    private static final String ARG_TAG_TAG         = "tag";

    /* Argument options */
    public static final String STOP_MODE            = "stop";
    public static final String ROUTE_MODE           = "route";

    /* Saved instance state tags */
    private static final String SAVED_AGENCY_TAG    = Config.PACKAGE_NAME+"."+HANDLE+".agency";
    private static final String SAVED_TAG_TAG       = Config.PACKAGE_NAME+"."+HANDLE+".tag";
    private static final String SAVED_MODE_TAG      = Config.PACKAGE_NAME+"."+HANDLE+".mode";
    private static final String SAVED_DATA_TAG      = Config.PACKAGE_NAME+"."+HANDLE+".data";
    private static final String SAVED_TITLE_TAG     = Config.PACKAGE_NAME+"."+HANDLE+".title";

    /* Member data */
    private ArrayList<Prediction> mData;
    private PredictionAdapter mAdapter;
    private String mMode;
    private String mTag;
    private String mTitle;
    private Handler mUpdateHandler;
    private Timer mUpdateTimer;
    private String mAgency;
    private ShareActionProvider shareActionProvider;
    private SwipeRefreshLayout refreshLayout;

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    public BusDisplay() {
        // Required empty public constructor
    }

    /** Create argument bundle for bus arrival time display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String mode,
                                    @NonNull String agency, @NonNull String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, BusDisplay.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        bundle.putString(ARG_AGENCY_TAG, agency);
        bundle.putString(ARG_MODE_TAG, mode);
        bundle.putString(ARG_TAG_TAG, tag);
        return bundle;
    }

    public static Bundle createLinkArgs(@NonNull String mode, @NonNull String agency,
                                        @NonNull String tag) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, BusDisplay.HANDLE);
        bundle.putString(ARG_AGENCY_TAG, agency);
        bundle.putString(ARG_MODE_TAG, mode);
        bundle.putString(ARG_TAG_TAG, tag);
        return bundle;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mData = new ArrayList<>();
        mAdapter = new PredictionAdapter(getActivity(), mData);

        // Set up handler for bus prediction update timer
        mUpdateHandler = new Handler();

        mAgency = null;
        mTag = null;

        // Attempt to restore state
        if (savedInstanceState != null) {
            mAgency = savedInstanceState.getString(SAVED_AGENCY_TAG);
            mTag = savedInstanceState.getString(SAVED_TAG_TAG);
            mMode = savedInstanceState.getString(SAVED_MODE_TAG);
            mAdapter.addAll((ArrayList<Prediction>) savedInstanceState.getSerializable(SAVED_DATA_TAG));
            mTitle = savedInstanceState.getString(SAVED_TITLE_TAG);
            return;
        }

        // Load arguments anew
        final Bundle args = getArguments();

        boolean missingArg = false;
        String requiredArgs[] = {ARG_AGENCY_TAG, ARG_MODE_TAG, ARG_TAG_TAG};
        for (String argTag: requiredArgs) {
            if (StringUtils.isBlank(args.getString(argTag))) {
                LOGE(TAG, "Argument \""+argTag+"\" not set");
                missingArg = true;
            }
        }
        if (missingArg) return;

        // Set route or stop display mode
        String mode = args.getString(ARG_MODE_TAG);
        if (BusDisplay.ROUTE_MODE.equalsIgnoreCase(mode)) {
            mMode = BusDisplay.ROUTE_MODE;
        } else if (BusDisplay.STOP_MODE.equalsIgnoreCase(mode)) {
            mMode = BusDisplay.STOP_MODE;
        } else {
            LOGE(TAG, "Invalid mode \""+args.getString(ARG_MODE_TAG)+"\"");
            // End here and make sure mAgency and mTag are null to prevent update attempts
            return;
        }

        // Get agency and route/stop tag
        mAgency = args.getString(ARG_AGENCY_TAG);
        mTag = args.getString(ARG_TAG_TAG);

        // Get title
        mTitle = args.getString(ARG_TITLE_TAG, getString(R.string.bus_title));

        // Start loading predictions
        showProgressCircle();
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_refresh_progress);

        final ListView listView = (ListView) v.findViewById(R.id.list);

        final ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setAbsListView(listView);
        listView.setAdapter(scaleInAnimationAdapter);

        if (mTitle != null) {
            getActivity().setTitle(mTitle);
        }

        mAdapter.setExpandCollapseListener(new ExpandableListItemAdapter.ExpandCollapseListener() {
            @Override
            public void onItemExpanded(int position) {
                // Don't expand the view if there are no predictions to display
                if (mAdapter.getItem(position).getMinutes().isEmpty()) mAdapter.collapse(position);
            }

            @Override
            public void onItemCollapsed(int position) {
            }
        });

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getActivity().getSupportLoaderManager().getLoader(LOADER_ID).forceLoad();
            }
        });
        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeResources(
                R.color.actbar_new,
                R.color.actbar_dark,
                R.color.white);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!ViewCompat.canScrollVertically(listView, -1) && refreshLayout != null) {
                    refreshLayout.setEnabled(true);
                } else if (refreshLayout != null) {
                    refreshLayout.setEnabled(false);
                }
                ;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });

        return v;
    }

    @Override
    public Link getLink() {
        final List<String> pathParts = new ArrayList<>();
        pathParts.add(mMode);
        pathParts.add(mTag);
        return new Link("bus", pathParts, getLinkTitle());
    }

    @Override
    public void onResume() {
        super.onResume();
        
        // Don't run if required args aren't loaded
        if (mAgency == null || mTag == null) return;
        
        // Start the update thread when screen is active
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        final FragmentActivity activity = getActivity();
                        if (activity != null) {
                            final LoaderManager loaderManager = activity.getSupportLoaderManager();
                            if (loaderManager != null) {
                                final Loader loader = loaderManager.getLoader(LOADER_ID);
                                if (loader != null) {
                                    loader.forceLoad();
                                }
                            }
                        }
                    }
                });
            }
        }, 0, 1000 * REFRESH_INTERVAL);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // Stop the update thread from running when screen isn't active
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_AGENCY_TAG, mAgency);
        outState.putString(SAVED_TAG_TAG, mTag);
        outState.putString(SAVED_MODE_TAG, mMode);
        outState.putSerializable(SAVED_DATA_TAG, mData);
        outState.putSerializable(SAVED_TITLE_TAG, mTitle);
    }

    @Override
    public Loader<PredictionLoader.PredictionHolder> onCreateLoader(int id, Bundle args) {
        return new PredictionLoader(getActivity(), mAgency, mTag, mMode);
    }

    @Override
    public void onLoadFinished(Loader<PredictionLoader.PredictionHolder> loader, PredictionLoader.PredictionHolder data) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        Predictions predictions = data.getPredictions();
        String title = data.getTitle();
        if (title != null) {
            mTitle = title;
            activity.setTitle(mTitle);
        }

        // If there are no active routes or stops, show a message
        if (predictions.getPredictions().isEmpty()) {
            int message;

            // A stop may have no active routes; a route may have no active stops
            if (BusDisplay.STOP_MODE.equals(mMode)) {
                message = R.string.bus_no_active_routes;
            } else {
                message = R.string.bus_no_active_stops;
            }

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }

        mAdapter.clear();
        mAdapter.addAll(predictions.getPredictions());
        hideProgressCircle();
        if (refreshLayout != null) {
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoaderReset(Loader<PredictionLoader.PredictionHolder> loader) {
        mAdapter.clear();
        hideProgressCircle();
    }
}
