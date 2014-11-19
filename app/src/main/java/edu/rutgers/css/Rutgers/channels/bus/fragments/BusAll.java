package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.RouteStub;
import edu.rutgers.css.Rutgers.channels.bus.model.StopStub;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import edu.rutgers.css.Rutgers2.R;


public class BusAll extends Fragment {

    private static final String TAG = "BusAll";
    public static final String HANDLE = "busall";

    private RMenuAdapter mAdapter;
    private String mFilterString;
    private ProgressBar mProgressCircle;
    private boolean mLoading;
    
    public BusAll() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<RMenuRow> data = new ArrayList<>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, data);

        // Restore filter
        if(savedInstanceState != null) {
            mFilterString = savedInstanceState.getString("filter");
        }

        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getActivity());
        final boolean nbHome = userHome.equals(getString(R.string.campus_nb_full));

        // Get promises for all route & stop information
        final Promise<List<RouteStub>, Exception, Void> nbRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NB);
        final Promise<List<StopStub>, Exception, Void> nbStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NB);
        final Promise<List<RouteStub>, Exception, Void> nwkRoutes = NextbusAPI.getAllRoutes(NextbusAPI.AGENCY_NWK);
        final Promise<List<StopStub>, Exception, Void> nwkStops = NextbusAPI.getAllStops(NextbusAPI.AGENCY_NWK);

        mLoading = true;

        // Synchronized load of all route & stop information
        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(nbRoutes, nbStops, nwkRoutes, nwkStops).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                mLoading = false;

                // Don't do anything if not attached to activity anymore
                if(!isAdded()) return;

                List<RouteStub> nbRoutesResult = (List<RouteStub>) results.get(0).getResult();
                List<StopStub> nbStopsResult = (List<StopStub>) results.get(1).getResult();
                List<RouteStub> nwkRoutesResult = (List<RouteStub>) results.get(2).getResult();
                List<StopStub> nwkStopsResult = (List<StopStub>) results.get(3).getResult();

                if(nbHome) {
                    loadRoutes(NextbusAPI.AGENCY_NB, nbRoutesResult);
                    loadStops(NextbusAPI.AGENCY_NB, nbStopsResult);
                    loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutesResult);
                    loadStops(NextbusAPI.AGENCY_NWK, nwkStopsResult);
                } else {
                    loadRoutes(NextbusAPI.AGENCY_NWK, nwkRoutesResult);
                    loadStops(NextbusAPI.AGENCY_NWK, nwkStopsResult);
                    loadRoutes(NextbusAPI.AGENCY_NB, nbRoutesResult);
                    loadStops(NextbusAPI.AGENCY_NB, nbStopsResult);
                }

                // Set filter after info is re-loaded
                if(mFilterString != null) {
                    mAdapter.getFilter().filter(mFilterString);
                }

            }

        }).fail(new FailCallback<OneReject>() {

            @Override
            public void onFail(OneReject result) {
                AppUtils.showFailedLoadToast(getActivity());
                Exception e = (Exception) result.getReject();
                Log.w(TAG, e.getMessage());
            }

        }).always(new AlwaysCallback<MultipleResults, OneReject>() {
            @Override
            public void onAlways(Promise.State state, MultipleResults resolved, OneReject rejected) {
                hideProgressCircle();
            }
        });
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bus_all, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);
        if(mLoading) showProgressCircle();

        // Get the filter field and add a listener to it
        final EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        filterEditText.addTextChangedListener(new TextWatcher() {

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
        filterEditText.requestFocus();

        // Get clear button and set listener
        ImageButton filterClearButton = (ImageButton) v.findViewById(R.id.filterClearButton);
        filterClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                filterEditText.setText("");
            }
        });

        // Set up list to accept clicks on route or stop rows
        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clickedItem = (RMenuItemRow) parent.getAdapter().getItem(position);
                Bundle clickedArgs = clickedItem.getArgs();
                ComponentFactory.getInstance().switchFragments(new Bundle(clickedArgs));
            }

        });
        
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFilterString != null) outState.putString("filter", mFilterString);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    private void loadStops(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        if(getResources() == null) return;

        // Get header for stops section
        String header;
        if(NextbusAPI.AGENCY_NB.equals(agency)) header = getString(R.string.bus_nb_all_stops_header);
        else if(NextbusAPI.AGENCY_NWK.equals(agency)) header = getString(R.string.bus_nwk_all_stops_header);
        else throw new IllegalArgumentException("Invalid Nextbus agency \""+agency+"\"");

        mAdapter.add(new RMenuHeaderRow(header));

        if(stopStubs.isEmpty()) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_configured_stops)));
        } else {
            for(StopStub stopStub: stopStubs) {
                Bundle stopArgs = new Bundle();
                stopArgs.putString("component", BusDisplay.HANDLE);
                stopArgs.putString("title", stopStub.getTitle());
                stopArgs.putString("tag", stopStub.getTitle());
                stopArgs.putString("mode", "stop");
                stopArgs.putString("agency", agency);
                mAdapter.add(new RMenuItemRow(stopArgs));
            }
        }
    }

    private void loadRoutes(@NonNull String agency, @NonNull List<RouteStub> routeStubs) {
        if(getResources() == null) return;

        // Get header for routes section
        String header;
        if(NextbusAPI.AGENCY_NB.equals(agency)) header = getString(R.string.bus_nb_all_routes_header);
        else if(NextbusAPI.AGENCY_NWK.equals(agency)) header = getString(R.string.bus_nwk_all_routes_header);
        else throw new IllegalArgumentException("Invalid Nextbus agency \""+agency+"\"");

        mAdapter.add(new RMenuHeaderRow(header));

        if(routeStubs.isEmpty()) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_configured_routes)));
        } else {
            for(RouteStub routeStub: routeStubs) {
                Bundle stopArgs = new Bundle();
                stopArgs.putString("component", BusDisplay.HANDLE);
                stopArgs.putString("title", routeStub.getTitle());
                stopArgs.putString("tag", routeStub.getTag());
                stopArgs.putString("mode", "route");
                stopArgs.putString("agency", agency);
                mAdapter.add(new RMenuItemRow(stopArgs));
            }
        }
    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}