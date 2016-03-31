package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.RouteLoader;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BusRoutes extends BaseChannelFragment implements LoaderManager.LoaderCallbacks<List<SimpleSection<RouteStub>>> {

    /* Log tag and component handle */
    private static final String TAG                 = "BusRoutes";
    public static final String HANDLE               = "busroutes";

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();

    /* Member data */
    private SimpleSectionedAdapter<RouteStub> mAdapter;

    public BusRoutes() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_stickylist_progress_simple);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                RouteStub routeStub = (RouteStub) parent.getAdapter().getItem(position);
                Bundle displayArgs = BusDisplay.createArgs(routeStub.getTitle(), BusDisplay.ROUTE_MODE,
                        routeStub.getAgencyTag(), routeStub.getTag());
                switchFragments(displayArgs);
            }

        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear out everything
        mAdapter.clear();
        showProgressCircle();
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

        mAdapter.clear();
        mAdapter.addAll(data);
        hideProgressCircle();

        // Assume an empty response is an error
        // TODO: Is that actually reasonable?
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<RouteStub>>> loader) {
        mAdapter.clear();
    }

    @Override
    public Link getLink() {
        return null;
    }
}
