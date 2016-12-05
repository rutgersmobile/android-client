package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.bus.Prediction;
import edu.rutgers.css.Rutgers.api.model.bus.Predictions;
import edu.rutgers.css.Rutgers.api.model.bus.route.RouteStub;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopStub;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.PredictionAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

public class BusDisplay extends BaseChannelFragment {

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
    private static final String SAVED_MESSAGE_TAG   = Config.PACKAGE_NAME+"."+HANDLE+".message";

    /* Member data */
    private ArrayList<Prediction> mData;
    private PredictionAdapter mAdapter;
    private String mMode;
    private String mTag;
    private String mTitle;
    private String mAgency;
    private String mMessage;
    private SwipeRefreshLayout refreshLayout;
    private TextView messagesView;
    private View dividerView;
    private boolean mLoading = false;

    private PublishSubject<Long> refreshSubject = PublishSubject.create();

    public static class PredictionHolder {
        private final Predictions predictions;
        private final String title;

        public PredictionHolder(final Predictions predictions, final String title) {
            this.predictions = predictions;
            this.title = title;
        }

        public Predictions getPredictions() {
            return predictions;
        }

        public String getTitle() {
            return title;
        }
    }

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

        mAgency = null;
        mTag = null;

        // Attempt to restore state
        if (savedInstanceState != null) {
            mAgency = savedInstanceState.getString(SAVED_AGENCY_TAG);
            mTag = savedInstanceState.getString(SAVED_TAG_TAG);
            mMode = savedInstanceState.getString(SAVED_MODE_TAG);
            mAdapter.addAll((ArrayList<Prediction>) savedInstanceState.getSerializable(SAVED_DATA_TAG));
            mAdapter.setAgency(mAgency);
            mAdapter.setTag(mTag);
            mAdapter.setMode(mMode);
            mTitle = savedInstanceState.getString(SAVED_TITLE_TAG);
            mMessage = savedInstanceState.getString(SAVED_MESSAGE_TAG);
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
        String argMode = args.getString(ARG_MODE_TAG);
        if (BusDisplay.ROUTE_MODE.equalsIgnoreCase(argMode)) {
            mMode = BusDisplay.ROUTE_MODE;
        } else if (BusDisplay.STOP_MODE.equalsIgnoreCase(argMode)) {
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

        mAdapter.setAgency(mAgency);
        mAdapter.setTag(mTag);
        mAdapter.setMode(mMode);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start loading predictions
        mLoading = true;

        final String agency = mAgency;
        final String tag = mTag;
        final String mode = mMode;
        Observable.merge(
            Observable.interval(0, REFRESH_INTERVAL, TimeUnit.SECONDS),
            refreshSubject)
            // need to specify io scheduler because refreshSubject exists on main thread
            .observeOn(Schedulers.io())
            .flatMap(t -> {
                LOGI(TAG, "Starting load");
                if (BusDisplay.ROUTE_MODE.equals(mode)) {
                    return NextbusAPI.routePredict(agency, tag)
                        .flatMap(predictions -> NextbusAPI.getActiveRoutes(agency)
                        .flatMap(routeStubs -> {
                            for (final RouteStub route : routeStubs) {
                                if (route.getTag().equals(tag)) {
                                    return Observable.just(new PredictionHolder(predictions, route.getTitle()));
                                }
                            }
                            return Observable.error(new IllegalArgumentException("Route tag not found"));
                        }));
                } else if (BusDisplay.STOP_MODE.equals(mode)) {
                    return NextbusAPI.stopPredict(agency, tag)
                        .flatMap(predictions -> NextbusAPI.getActiveStops(agency)
                        .flatMap(stopStubs -> {
                            for (final StopStub stop : stopStubs) {
                                if (stop.getTag().equals(tag)) {
                                    return Observable.just(new PredictionHolder(predictions, stop.getTitle()));
                                }
                            }
                            return Observable.error(new IllegalArgumentException("Stop tag not found"));
                        }));
                }

                return Observable.error(
                    new IllegalArgumentException("Invalid mode (stop / route only)")
                );
            })
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(data -> {
                LOGI(TAG, "Ran subscription");
                reset();

                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                Predictions predictions = data.getPredictions();
                mTitle = data.getTitle();
                getActivity().setTitle(mTitle);

                // If there are no active routes or stops, show a message
                if (predictions.getPredictions().isEmpty()) {
                    // A stop may have no active routes; a route may have no active stops
                    int message = BusDisplay.STOP_MODE.equals(mMode)
                        ? R.string.bus_no_active_routes
                        : R.string.bus_no_active_stops;

                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                mAdapter.addAll(predictions.getPredictions());

                mMessage = StringUtils.join(predictions.getMessages(), "\n");
                if (!mMessage.isEmpty()) {
                    dividerView.setVisibility(View.VISIBLE);
                    messagesView.setVisibility(View.VISIBLE);
                    messagesView.setText(mMessage);
                }
            }, error -> {
                reset();
                LOGE(TAG, error.getMessage());
                AppUtils.showFailedLoadToast(getContext());
            });
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_refresh_progress_sticky);

        if (mLoading) showProgressCircle();

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

        if (mTitle != null) {
            getActivity().setTitle(mTitle);
        }

        messagesView = (TextView)  v.findViewById(R.id.messages);
        dividerView = v.findViewById(R.id.message_separator);

        if (mMessage != null && !mMessage.isEmpty()) {
            dividerView.setVisibility(View.VISIBLE);
            messagesView.setVisibility(View.VISIBLE);
            messagesView.setText(mMessage);
        }

        refreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> refreshSubject.onNext(0L));
        refreshLayout.setEnabled(false);
        refreshLayout.setColorSchemeResources(
                R.color.actbar_new,
                R.color.actbar_dark,
                R.color.white);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (refreshLayout != null) {
                    refreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
                }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_AGENCY_TAG, mAgency);
        outState.putString(SAVED_TAG_TAG, mTag);
        outState.putString(SAVED_MODE_TAG, mMode);
        outState.putSerializable(SAVED_DATA_TAG, mData);
        outState.putSerializable(SAVED_TITLE_TAG, mTitle);
        outState.putString(SAVED_MESSAGE_TAG, mMessage);
    }

    private void reset() {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }
}
