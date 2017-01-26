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
import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;
import edu.rutgers.css.Rutgers.api.model.bus.Prediction;
import edu.rutgers.css.Rutgers.api.model.bus.Predictions;
import edu.rutgers.css.Rutgers.api.model.bus.VehiclePrediction;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.AedanDialogFragment;
import edu.rutgers.css.Rutgers.channels.bus.model.PredictionAdapter;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.FuncUtils;
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
    private static final String ARG_VEHICLE_TAG     = "vehicle";

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
    private String mVehicle;
    private SwipeRefreshLayout refreshLayout;
    private TextView messagesView;
    private View dividerView;

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

    public static Bundle createArgs(@NonNull String title, @NonNull String mode,
                                    @NonNull String agency, @NonNull String tag, @NonNull String vehicle) {
        Bundle bundle = createArgs(title, mode, agency, tag);
        bundle.putString(ARG_VEHICLE_TAG, vehicle);
        return bundle;
    }

    /** Create argument bundle for bus arrival time display. */
    public static Bundle createArgs(@NonNull String title, @NonNull String mode,
                                    @NonNull String agency, @NonNull String tag) {
        Bundle bundle = createArgs(mode, agency, tag);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    public static Bundle createArgs(@NonNull String mode, @NonNull String agency,
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

        // Load arguments anew
        final Bundle args = getArguments();

        mVehicle = args.getString(ARG_VEHICLE_TAG);

        mData = new ArrayList<>();
        mAdapter = new PredictionAdapter(getActivity(), mData, mVehicle != null);

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

        mAdapter
            .getAedanClicks()
            .flatMap(prediction -> {
                final ArrayList<VehiclePrediction> vehiclePredictions = new ArrayList<>();
                vehiclePredictions.addAll(prediction.getVehiclePredictions());
                final Bundle aedanArgs = AedanDialogFragment.createArgs(vehiclePredictions);
                AedanDialogFragment aedanFragment = new AedanDialogFragment();
                aedanFragment.setArguments(aedanArgs);
                aedanFragment.show(getFragmentManager(), "aedan");
                return aedanFragment.getSelection();
            })
            .filter(FuncUtils::nonNull)
            .map(vehiclePrediction ->
                BusDisplay.createArgs(mTitle, mMode, mAgency, mTag, vehiclePrediction.getVehicle())
            )
            .compose(bindToLifecycle())
            .subscribe(this::switchFragments, this::logError);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start loading predictions
        setLoading(true);

        final String agency = mAgency;
        final String tag = mTag;
        final String mode = mMode;
        final String vehicle = mVehicle;
        Observable.merge(
            Observable.interval(0, REFRESH_INTERVAL, TimeUnit.SECONDS),
            refreshSubject)
            // need to specify io scheduler because refreshSubject exists on main thread
            .flatMap(t -> {
                LOGI(TAG, "Starting load");
                Observable<Predictions> predictionsObservable;
                Observable<List<? extends NextbusItem>> activeObservable;
                if (BusDisplay.ROUTE_MODE.equals(mode)) {
                    // if we have a route
                    predictionsObservable = NextbusAPI.routePredict(agency, tag);
                    // that whooshing sound is the Java type system coming apart at the seams
                    activeObservable = NextbusAPI.getActiveRoutes(agency).map(x -> x);
                } else if (BusDisplay.STOP_MODE.equals(mode)) {
                    // else if we have a stop
                    predictionsObservable = NextbusAPI.stopPredict(agency, tag);
                    activeObservable = NextbusAPI.getActiveStops(agency).map(x -> x);
                } else {
                    // else error if we have neither
                    predictionsObservable = Observable.error(
                        new IllegalArgumentException("Invalid mode (stop / route only)")
                    );
                    activeObservable = Observable.error(
                        new IllegalArgumentException("Invalid mode (stop / route only)")
                    );
                }
                return predictionsObservable.map(predictions -> {
                    if (vehicle != null) {
                        return new Predictions(predictions.getMessages(), predictions.getPredictions(vehicle));
                    }
                    return predictions;
                }).flatMap(predictions ->
                    activeObservable.flatMap(nextbusItems -> {
                    for (final NextbusItem item : nextbusItems) {
                        if (item.getTag().equals(tag)) {
                            return Observable.just(
                                new PredictionHolder(predictions, item.getTitle())
                            );
                        }
                    }
                    return Observable.error(new IllegalArgumentException("Stop tag not found"));
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(this::logAndRetry);
            })
            .compose(bindToLifecycle())
            .subscribe(data -> {
                LOGI(TAG, "Ran subscription");
                reset();

                if (refreshLayout != null) {
                    refreshLayout.setRefreshing(false);
                }

                Predictions predictions = data.getPredictions();
                mTitle = data.getTitle();
                setTitle();

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
            }, this::handleErrorWithRetry);
    }

    private void setTitle() {
        if (mTitle != null) {
            if (mVehicle != null) {
                getActivity().setTitle(mTitle + " - " + mVehicle);
            } else {
                getActivity().setTitle(mTitle);
            }
        }
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_list_refresh_progress_sticky);

        if (mVehicle != null) {
            getFab().setVisibility(View.INVISIBLE);
        }

        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);

        setTitle();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

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

    protected void reset() {
        super.reset();
        mAdapter.clear();
    }
}
