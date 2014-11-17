package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.common.GooglePlayServicesClient;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.StopGroup;
import edu.rutgers.css.Rutgers.channels.bus.model.StopStub;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuAdapter;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuItemRow;
import edu.rutgers.css.Rutgers.model.rmenu.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import edu.rutgers.css.Rutgers2.BuildConfig;
import edu.rutgers.css.Rutgers2.R;

public class BusStops extends Fragment implements FilterFocusBroadcaster, GooglePlayServicesClient.ConnectionCallbacks {

    private static final String TAG = "BusStops";
    public static final String HANDLE = "busstops";

    private static final int REFRESH_INTERVAL = 60 * 2; // nearby stop refresh interval in seconds

    private RMenuAdapter mAdapter;
    private LocationClientProvider mLocationClientProvider;
    private FilterFocusListener mFilterFocusListener;
    private AndroidDeferredManager mDM;

    private Timer mUpdateTimer;
    private Handler mUpdateHandler;

    private int mNearbyRowCount; // Keep track of number of nearby stops displayed
    private boolean mNearbyHeaderAdded;

    private ProgressBar mProgressCircle;

    public BusStops() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "Attaching to activity");
        try {
            mLocationClientProvider = (LocationClientProvider) activity;
        } catch(ClassCastException e) {
            mLocationClientProvider = null;
            Log.e(TAG, "onAttach(): " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "Detaching from activity");
        mLocationClientProvider = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDM = new AndroidDeferredManager();
        
        mNearbyRowCount = 0;
        mNearbyHeaderAdded = false;
        List<RMenuRow> mData = new ArrayList<>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);
        
        // Set up handler for nearby stop update timer
        mUpdateHandler = new Handler();
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bus_stops, parent, false);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

        // Get the filter field and add a listener to it
        EditText filterEditText = (EditText) v.findViewById(R.id.filterEditText);
        filterEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(mFilterFocusListener != null) mFilterFocusListener.focusEvent();
            }
        });

        ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            /**
             * Clicking on one of the stops will launch the bus display in stop mode, which lists
             * routes going through that stop.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RMenuItemRow clickedItem = (RMenuItemRow) parent.getAdapter().getItem(position);
                Bundle clickedArgs = clickedItem.getArgs();

                Bundle newArgs = new Bundle(clickedArgs);
                newArgs.putString("component", BusDisplay.HANDLE);
                newArgs.putString("mode", "stop");
                newArgs.putString("tag", clickedArgs.getString("title"));

                ComponentFactory.getInstance().switchFragments(newArgs);
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

        if(mLocationClientProvider != null) mLocationClientProvider.registerListener(this);

        // Clear out everything
        clearNearbyRows();
        mAdapter.clear();

        // Start the update thread when screen is active
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mUpdateHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loadNearbyStops();
                    }
                });
            }
        }, 0, 1000 * REFRESH_INTERVAL);

        // Get home campus for result ordering
        String userHome = RutgersUtils.getHomeCampus(getActivity());
        final boolean nbHome = userHome.equals(getString(R.string.campus_nb_full));

        // Get promises for active stops
        final Promise<List<StopStub>, Exception, Void> nbActiveStops = NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NB);
        final Promise<List<StopStub>, Exception, Void> nwkActiveStops = NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NWK);

        // Synchronized load of active stops
        showProgressCircle();
        mDM.when(nbActiveStops, nwkActiveStops).done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults results) {
                // Don't do anything if not attached to activity anymore
                if(!isAdded() || getResources() == null) return;

                List<StopStub> nbResult = (List<StopStub>) results.get(0).getResult();
                List<StopStub> nwkResult = (List<StopStub>) results.get(1).getResult();

                if(nbHome) {
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
    public void onPause() {
        super.onPause();

        // Stop the update thread from running when screen isn't active
        if(mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if(mLocationClientProvider != null) mLocationClientProvider.unregisterListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        setFocusListener(null);
        mProgressCircle = null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to services");

        // Location services reconnected - retry loading nearby stops
        // Make sure this isn't called before onCreate() has ran.
        if(mAdapter != null) loadNearbyStops();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Disconnected from services");
    }

    /**
     * Populate list with bus stops for agency, with a section header for that agency.
     * @param agency Agency tag
     * @param stopStubs List of stop stubs (titles/geohashes) for that agency
     */
    private void loadAgency(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        // Abort if resources can't be accessed
        if(!isAdded() || getResources() == null) return;

        // Get header for active stops section
        String header;
        if(NextbusAPI.AGENCY_NB.equals(agency)) header = getString(R.string.bus_nb_active_stops_header);
        else if(NextbusAPI.AGENCY_NWK.equals(agency)) header = getString(R.string.bus_nwk_active_stops_header);
        else throw new IllegalArgumentException("Invalid Nextbus agency \""+agency+"\"");

        mAdapter.add(new RMenuHeaderRow(header));

        if(stopStubs.isEmpty()) {
            mAdapter.add(new RMenuItemRow(getString(R.string.bus_no_active_stops)));
        } else {
            for(StopStub stopStub: stopStubs) {
                Bundle stopArgs = new Bundle();
                stopArgs.putString("title", stopStub.getTitle());
                stopArgs.putString("agency", agency);
                mAdapter.add(new RMenuItemRow(stopArgs));
            }
        }
    }
    
    /**
     * Remove rows related to nearby stops
     */
    private void clearNearbyRows() {
        for(int i = 0; i < mNearbyRowCount; i++) {
            mAdapter.remove(1);
        }
        mNearbyRowCount = 0;
        removeNearbyHeader();
    }

    /**
     * Add a nearby stop row
     * @param pos position
     * @param row value
     */
    private void addNearbyRow(int pos, RMenuRow row) {
        addNearbyHeader(); // Add header if it's not in yet
        mAdapter.insert(row, pos);
        mNearbyRowCount++;
    }

    /** Add "nearby stops" header */
    private void addNearbyHeader() {
        if(!mNearbyHeaderAdded) {
            mAdapter.insert(new RMenuHeaderRow(getString(R.string.bus_nearby_active_stops_header)), 0);
            mNearbyHeaderAdded = true;
        }
    }

    /** Remove "nearby stops" header */
    private void removeNearbyHeader() {
        if(mNearbyHeaderAdded) {
            mAdapter.remove(0);
            mNearbyHeaderAdded = false;
        }
    }

    /**
     * Populate list with active nearby stops for an agency
     */
    private void loadNearbyStops() {
        if(!isAdded() || getResources() == null) return;

        final String noneNearbyString = getString(R.string.bus_no_nearby_stops);
        final String failedLoadString = getString(R.string.failed_load_short);

        // First clear all "nearby stop"-related rows
        clearNearbyRows();

        // Check for location services
        if(mLocationClientProvider != null && mLocationClientProvider.servicesConnected() && mLocationClientProvider.getLocationClient().isConnected()) {
            // Get last location
            Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
            if(lastLoc == null) {
                Log.w(TAG, "Could not get location");
                clearNearbyRows();
                addNearbyRow(1, new RMenuItemRow(getString(R.string.failed_location)));
                return;
            }

            if(BuildConfig.DEBUG) Log.d(TAG, "Current location: " + lastLoc.toString());
            Log.i(TAG, "Updating nearby active stops");

            Promise<List<StopGroup>, Exception, Void> nbNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NB, (float) lastLoc.getLatitude(), (float) lastLoc.getLongitude());
            Promise<List<StopGroup>, Exception, Void> nwkNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NWK, (float) lastLoc.getLatitude(), (float) lastLoc.getLongitude());

            // Look up nearby active bus stops
            mDM.when(nbNearbyStops, nwkNearbyStops).then(new DoneCallback<MultipleResults>() {

                @Override
                public void onDone(MultipleResults results) {
                    if(!isAdded() || getResources() == null) return;

                    List<StopGroup> nbStops = (List<StopGroup>) results.get(0).getResult();
                    List<StopGroup> nwkStops = (List<StopGroup>) results.get(1).getResult();

                    // Clear previous rows
                    clearNearbyRows();

                    if(nbStops.isEmpty() && nwkStops.isEmpty()) {
                        // If there aren't any results, put a "no stops nearby" message
                        addNearbyRow(1, new RMenuItemRow(noneNearbyString));
                    } else {
                        // Add all the stops
                        int j = 1;
                        for(StopGroup stopGroup: nbStops) {
                            Bundle stopArgs = new Bundle();
                            stopArgs.putString("title", stopGroup.getTitle());
                            stopArgs.putString("agency", NextbusAPI.AGENCY_NB);
                            addNearbyRow(j++, new RMenuItemRow(stopArgs));
                        }

                        for(StopGroup stopGroup: nwkStops) {
                            Bundle stopArgs = new Bundle();
                            stopArgs.putString("title", stopGroup.getTitle());
                            stopArgs.putString("agency", NextbusAPI.AGENCY_NWK);
                            addNearbyRow(j++, new RMenuItemRow(stopArgs));
                        }
                    }
                }

            }).fail(new FailCallback<OneReject>() {
                @Override
                public void onFail(OneReject result) {
                    addNearbyRow(1, new RMenuItemRow(failedLoadString));
                }
            });
        } else {
            Log.w(TAG, "Couldn't get location provider, can't find nearby stops");
            addNearbyRow(1, new RMenuItemRow(getString(R.string.failed_location)));
        }

    }

    @Override
    public void setFocusListener(FilterFocusListener listener) {
        mFilterFocusListener = listener;
    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
