package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.RouteLoader;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BusRoutes extends BaseChannelFragment implements FilterFocusBroadcaster, LoaderManager.LoaderCallbacks<List<SimpleSection<RouteStub>>> {

    /* Log tag and component handle */
    private static final String TAG                 = "BusRoutes";
    public static final String HANDLE               = "busroutes";

    private static final int LOADER_ID              = 101;

    /* Member data */
    private SimpleSectionedAdapter<RouteStub> mAdapter;
    private FilterFocusListener mFilterFocusListener;

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
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_search_stickylist_progress);

        // Get the filter field and add a listener to it
        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        filterEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (mFilterFocusListener != null) mFilterFocusListener.focusEvent();
            }
        });
        
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

        // Set main bus fragment as focus listener, for switching to All tab
        FilterFocusListener mainFragment = (BusMain) getParentFragment();
        setFocusListener(mainFragment);
                
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear out everything
        mAdapter.clear();
        showProgressCircle();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        setFocusListener(null);
    }

    @Override
    public void setFocusListener(FilterFocusListener listener) {
        mFilterFocusListener = listener;
    }

    @Override
    public Loader<List<SimpleSection<RouteStub>>> onCreateLoader(int id, Bundle args) {
        return new RouteLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleSection<RouteStub>>> loader, List<SimpleSection<RouteStub>> data) {
        mAdapter.clear();
        mAdapter.addAll(data);
        hideProgressCircle();
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<RouteStub>>> loader) {
        mAdapter.clear();
    }
}
