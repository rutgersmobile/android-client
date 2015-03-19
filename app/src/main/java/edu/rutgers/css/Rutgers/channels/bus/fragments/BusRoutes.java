package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BusRoutes extends Fragment implements FilterFocusBroadcaster {

    /* Log tag and component handle */
    private static final String TAG                 = "BusRoutes";
    public static final String HANDLE               = "busroutes";

    /* Member data */
    private SimpleSectionedAdapter<RouteStub> mAdapter;
    private FilterFocusListener mFilterFocusListener;
    private AndroidDeferredManager mDM;

    /* View references */
    private ProgressBar mProgressCircle;
    
    public BusRoutes() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDM = new AndroidDeferredManager();
        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_search_stickylist_progress, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

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
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RouteStub routeStub = (RouteStub) parent.getAdapter().getItem(position);
                Bundle displayArgs = BusDisplay.createArgs(routeStub.getTitle(), BusDisplay.ROUTE_MODE,
                        routeStub.getAgencyTag(), routeStub.getTag());
                ComponentFactory.getInstance().switchFragments(displayArgs);
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

        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getActivity());
        final boolean nbHome = userHome.equals(getString(R.string.campus_nb_full));

        // Get promises for active routes
        final Promise<List<RouteStub>, Exception, Void> nbActiveRoutes = NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NB);
        final Promise<List<RouteStub>, Exception, Void> nwkActiveRoutes = NextbusAPI.getActiveRoutes(NextbusAPI.AGENCY_NWK);

        // Synchronized load of active routes
        showProgressCircle();
        mDM.when(nbActiveRoutes, nwkActiveRoutes).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                // Abort if resources can't be accessed
                if (!isAdded() || getResources() == null) return;

                List<RouteStub> nbResult = (List<RouteStub>) results.get(0).getResult();
                List<RouteStub> nwkResult = (List<RouteStub>) results.get(1).getResult();

                if (nbHome) {
                    loadAgency(NextbusAPI.AGENCY_NB, nbResult);
                    loadAgency(NextbusAPI.AGENCY_NWK, nwkResult);
                } else {
                    loadAgency(NextbusAPI.AGENCY_NWK, nwkResult);
                    loadAgency(NextbusAPI.AGENCY_NB, nbResult);
                }
            }

        }).fail(new FailCallback<OneReject>() {

            @Override
            public void onFail(OneReject result) {
                AppUtils.showFailedLoadToast(getActivity());
            }

        }).always(new AlwaysCallback<MultipleResults, OneReject>() {
            @Override
            public void onAlways(Promise.State state, MultipleResults resolved, OneReject rejected) {
                hideProgressCircle();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        setFocusListener(null);
        mProgressCircle = null;
    }

    /**
     * Populate list with bus routes for agency, with a section header for that agency
     * @param agencyTag Agency tag for API request
     * @param routeStubs List of routes (stubs)
     */
    private void loadAgency(@NonNull String agencyTag, @NonNull List<RouteStub> routeStubs) {
        // Abort if resources can't be accessed
        if (!isAdded() || getResources() == null) return;

        // Get header for routes section
        String header;
        if (NextbusAPI.AGENCY_NB.equals(agencyTag)) header = getString(R.string.bus_nb_active_routes_header);
        else if (NextbusAPI.AGENCY_NWK.equals(agencyTag)) header = getString(R.string.bus_nwk_active_routes_header);
        else throw new IllegalArgumentException("Invalid Nextbus agency \""+agencyTag+"\"");

        mAdapter.add(new SimpleSection<>(header, routeStubs));

        /*
        if (routeStubs.isEmpty()) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_active_routes)));
        }
        */
    }

    @Override
    public void setFocusListener(FilterFocusListener listener) {
        mFilterFocusListener = listener;
    }

    private void showProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if (mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
