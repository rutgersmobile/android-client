package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.RouteLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;

public class BusRoutes extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<List<SimpleSection<RouteStub>>> {

    /* Log tag and component handle */
    private static final String TAG                 = "BusRoutes";
    public static final String HANDLE               = "busroutes";

    private boolean loading = false;

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Member data */
    private SimpleSectionedRecyclerAdapter<RouteStub> mAdapter;

    public BusRoutes() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SimpleSectionedRecyclerAdapter<>(new ArrayList<>(),
            R.layout.row_section_header, R.layout.row_title, R.id.title);
        mAdapter.getPositionClicks()
            .compose(bindToLifecycle())
            .map(routeStub -> BusDisplay.createArgs(routeStub.getTitle(), BusDisplay.ROUTE_MODE,
                routeStub.getAgencyTag(), routeStub.getTag())
            )
            .subscribe(this::switchFragments, this::logError);

        loading = true;
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_recycler_progress_simple);

        if (loading) showProgressCircle();

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

        // Clear out everything
        mAdapter.clear();
        loading = true;
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<List<SimpleSection<RouteStub>>> onCreateLoader(int id, Bundle args) {
        return new RouteLoader(getContext());
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleSection<RouteStub>>> loader, List<SimpleSection<RouteStub>> data) {
        if (getContext() == null) {
            return;
        }

        reset();

        mAdapter.addAll(data);

        // Assume an empty response is an error
        // TODO: Is that actually reasonable?
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<RouteStub>>> loader) {
        reset();
    }

    private void reset() {
        mAdapter.clear();
        loading = false;
        hideProgressCircle();
    }

    @Override
    public Link getLink() {
        return null;
    }
}
