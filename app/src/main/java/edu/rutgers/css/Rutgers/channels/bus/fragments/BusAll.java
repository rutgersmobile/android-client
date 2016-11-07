package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;
import edu.rutgers.css.Rutgers.api.model.bus.route.RouteStub;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopStub;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BusAll extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "BusAll";
    public static final String HANDLE               = "busall";

    /* ID for loader */
    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Saved instance state tags */
    private static final String SAVED_FILTER_TAG    = Config.PACKAGE_NAME+"."+HANDLE+".filter";

    /* Member data */
    private SimpleSectionedRecyclerAdapter<NextbusItem> mAdapter;
    private String mFilterString;
    private boolean mLoading;

    public BusAll() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleSectionedRecyclerAdapter<>(new ArrayList<>(),
            R.layout.row_section_header, R.layout.row_title, R.id.title);
        mAdapter.getPositionClicks()
            .compose(bindToLifecycle())
            .map(nextbusItem -> {
                final String mode = nextbusItem.getClass() == RouteStub.class ?
                    BusDisplay.ROUTE_MODE : BusDisplay.STOP_MODE;
                return BusDisplay.createArgs(nextbusItem.getTitle(), mode,
                        nextbusItem.getAgencyTag(), nextbusItem.getTag());
            })
            .subscribe(this::switchFragments, this::logError);

        // Restore filter
        if (savedInstanceState != null) {
            mFilterString = savedInstanceState.getString(SAVED_FILTER_TAG);
        }

        // Start loading all stops and routes in the background
        mLoading = true;
        NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NB).flatMap(nbRoutes ->
        NextbusAPI.getAllStops(NextbusAPI.AGENCY_NB).flatMap(nbStops ->
        NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NWK).flatMap(nwkRoutes ->
        NextbusAPI.getAllStops(NextbusAPI.AGENCY_NWK).map(nwkStops -> {
            final List<SimpleSection<NextbusItem>> nextbusItems = new ArrayList<>();
            final String userHome = RutgersUtils.getHomeCampus(getContext());
            final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

            if (nbHome) {
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NB, nbRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NB, nbStops));
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NWK, nwkStops));
            } else {
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NWK, nwkStops));
                nextbusItems.add(loadRoutes(NextbusAPI.AGENCY_NB, nbRoutes));
                nextbusItems.add(loadStops(NextbusAPI.AGENCY_NB, nbStops));
            }

            return nextbusItems;
        }))))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .subscribe(simpleSections -> {
            reset();
            mAdapter.addAll(simpleSections);
            if (mFilterString != null) {
                mAdapter.getFilter().filter(mFilterString);
            }
        }, this::logError);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress_simple);

        if (mLoading) showProgressCircle();

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);

        // Set main bus fragment as focus listener, for giving focus to search field
        BusMain mainFragment = (BusMain) getParentFragment();
        mainFragment.addSearchListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Set filter for list adapter
                mFilterString = s.toString().trim();
                mAdapter.getFilter().filter(mFilterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (StringUtils.isNotBlank(mFilterString)) outState.putString(SAVED_FILTER_TAG, mFilterString);
    }

    private void reset() {
        mAdapter.clear();
        mLoading = false;
        hideProgressCircle();
    }

    @Override
    public Link getLink() {
        return null;
    }

    private SimpleSection<NextbusItem> loadStops(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        // Get header for stops section
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_all_stops_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_all_stops_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        List<NextbusItem> nextbusItems = new ArrayList<>(stopStubs.size());
        nextbusItems.addAll(stopStubs);

        return new SimpleSection<>(header, nextbusItems);
    }

    private SimpleSection<NextbusItem> loadRoutes(@NonNull String agency, @NonNull List<RouteStub> routeStubs) {
        // Get header for routes section
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_all_routes_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_all_routes_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        List<NextbusItem> nextbusItems = new ArrayList<>(routeStubs.size());
        nextbusItems.addAll(routeStubs);

        return new SimpleSection<>(header, nextbusItems);
    }
}
