package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.bus.route.RouteStub;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.FuncWrapper;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BusRoutes extends BaseChannelFragment {

    /* Log tag and component handle */
    private static final String TAG                 = "BusRoutes";
    public static final String HANDLE               = "busroutes";

    /* Member data */
    private SimpleSectionedRecyclerAdapter<RouteStub> mAdapter;

    public BusRoutes() {
        // Required empty public constructor
    }

    @Override
    public String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SimpleSectionedRecyclerAdapter<>(new ArrayList<>(),
            R.layout.row_section_header, R.layout.row_title, R.id.title);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress_simple);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext()));
        recyclerView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mAdapter
            .getPositionClicks()
            .compose(bindToLifecycle())
            .map(routeStub -> BusDisplay.createArgs(
                routeStub.getTitle(),
                BusDisplay.ROUTE_MODE,
                routeStub.getAgencyTag(),
                routeStub.getTag()
            ))
            .subscribe(this::switchFragments, this::logError);

        // Clear out everything
        setLoading(true);
        NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NB).flatMap(nbActive ->
        NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NWK).map(nwkActive -> {
            final List<SimpleSection<RouteStub>> routes = new ArrayList<>();
            final String userHome = RutgersUtils.getHomeCampus(getContext());
            if (userHome.equals(getContext().getString(R.string.campus_nb_full))) {
                routes.add(loadAgency(NextbusAPI.AGENCY_NB, nbActive));
                routes.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActive));
            } else {
                routes.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActive));
                routes.add(loadAgency(NextbusAPI.AGENCY_NB, nbActive));
            }
            return routes;
        }))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .retryWhen(this::logAndRetry)
        .subscribe(sections -> {
            reset();
            mAdapter.addAll(sections);
        }, this::handleErrorWithRetry);
    }

    protected void reset() {
        super.reset();
        mAdapter.clear();
    }

    /**
     * Populate list with bus routes for agency, with a section header for that agency
     * @param agencyTag Agency tag for API request
     * @param routeStubs List of routes (stubs)
     */
    private SimpleSection<RouteStub> loadAgency(@NonNull String agencyTag, @NonNull List<RouteStub> routeStubs) {
        // Get header for routes section
        String header;
        switch (agencyTag) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_active_routes_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_active_routes_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agencyTag + "\"");
        }

        return new SimpleSection<>(header, routeStubs);
    }

    @Override
    public Link getLink() {
        return null;
    }
}
