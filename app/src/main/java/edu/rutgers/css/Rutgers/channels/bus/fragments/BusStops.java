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

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.bus.model.NextbusAPI;
import edu.rutgers.css.Rutgers.channels.bus.model.StopGroup;
import edu.rutgers.css.Rutgers.channels.bus.model.StopStub;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.interfaces.FilterFocusListener;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class BusStops extends Fragment implements FilterFocusBroadcaster, GooglePlayServicesClient.ConnectionCallbacks {

    /* Log tag and component handle */
    private static final String TAG                 = "BusStops";
    public static final String HANDLE               = "busstops";

    /* Constants */
    private static final int REFRESH_INTERVAL = 60 * 2; // nearby stop refresh interval in seconds

    /* Member data */
    private SimpleSectionedAdapter<StopStub> mAdapter;
    private List<StopStub> mNearbyStops;
    private LocationClientProvider mLocationClientProvider;
    private FilterFocusListener mFilterFocusListener;
    private AndroidDeferredManager mDM;

    private Timer mUpdateTimer;
    private Handler mUpdateHandler;

    /* View references */
    private ProgressBar mProgressCircle;

    public BusStops() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "Attaching to activity");
        mLocationClientProvider = (LocationClientProvider) activity;
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
        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);
        mNearbyStops = new ArrayList<>();
        
        // Set up handler for nearby stop update timer
        mUpdateHandler = new Handler();
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

            /**
             * Clicking on one of the stops will launch the bus display in stop mode, which lists
             * routes going through that stop.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StopStub stopStub = (StopStub) parent.getAdapter().getItem(position);
                Bundle displayArgs = BusDisplay.createArgs(stopStub.getTitle(), BusDisplay.STOP_MODE,
                        stopStub.getAgencyTag(), stopStub.getTitle());
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

        if (mLocationClientProvider != null) mLocationClientProvider.registerListener(this);

        // Clear out everything & add in empty nearby stops section as a placeholder
        mAdapter.clear();
        mNearbyStops.clear();
        mAdapter.add(new SimpleSection<>(getString(R.string.nearby_bus_header), mNearbyStops));

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
                if (!isAdded() || getResources() == null) return;

                List<StopStub> nbResult = (List<StopStub>) results.get(0).getResult();
                List<StopStub> nwkResult = (List<StopStub>) results.get(1).getResult();

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
    public void onPause() {
        super.onPause();

        // Stop the update thread from running when screen isn't active
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mLocationClientProvider != null) mLocationClientProvider.unregisterListener(this);
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
        if (mAdapter != null) loadNearbyStops();
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
        if (!isAdded() || getResources() == null) return;

        // Get header for active stops section
        String header;
        if (NextbusAPI.AGENCY_NB.equals(agency)) header = getString(R.string.bus_nb_active_stops_header);
        else if (NextbusAPI.AGENCY_NWK.equals(agency)) header = getString(R.string.bus_nwk_active_stops_header);
        else throw new IllegalArgumentException("Invalid Nextbus agency \""+agency+"\"");

        SimpleSection<StopStub> section = new SimpleSection<>(header, stopStubs);
        mAdapter.add(section);
    }

    /**
     * Populate list with active nearby stops for an agency
     */
    private void loadNearbyStops() {
        if (!isAdded() || getResources() == null) return;

        final String noneNearbyString = getString(R.string.bus_no_nearby_stops);
        final String failedLoadString = getString(R.string.failed_load_short);

        // First clear all "nearby stop"-related rows
        clearNearbyRows();

        // Check for location services
        if (mLocationClientProvider != null && mLocationClientProvider.servicesConnected() && mLocationClientProvider.getLocationClient().isConnected()) {
            // Get last location
            Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
            if (lastLoc == null) {
                Log.w(TAG, "Could not get location");
                clearNearbyRows();
                //addNearbyRow(1, new RMenuItemRow(getString(R.string.failed_location)));
                return;
            }

            if (BuildConfig.DEBUG) Log.d(TAG, "Current location: " + lastLoc.toString());
            Log.i(TAG, "Updating nearby active stops");

            Promise<List<StopGroup>, Exception, Void> nbNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NB, (float) lastLoc.getLatitude(), (float) lastLoc.getLongitude());
            Promise<List<StopGroup>, Exception, Void> nwkNearbyStops = NextbusAPI.getActiveStopsByTitleNear(NextbusAPI.AGENCY_NWK, (float) lastLoc.getLatitude(), (float) lastLoc.getLongitude());

            // Look up nearby active bus stops
            mDM.when(nbNearbyStops, nwkNearbyStops).then(new DoneCallback<MultipleResults>() {

                @Override
                public void onDone(MultipleResults results) {
                    if (!isAdded() || getResources() == null) return;

                    List<StopGroup> nbStops = (List<StopGroup>) results.get(0).getResult();
                    List<StopGroup> nwkStops = (List<StopGroup>) results.get(1).getResult();

                    // Clear previous rows
                    clearNearbyRows();

                    if (nbStops.isEmpty() && nwkStops.isEmpty()) {
                        // If there aren't any results, put a "no stops nearby" message
                        //addNearbyRow(1, new RMenuItemRow(noneNearbyString));
                    } else {
                        // Add all the stops
                        for (StopGroup stopGroup: nbStops) addNearbyRow(new StopStub(stopGroup));
                        for (StopGroup stopGroup: nwkStops) addNearbyRow(new StopStub(stopGroup));
                    }
                }

            }).fail(new FailCallback<OneReject>() {
                @Override
                public void onFail(OneReject result) {
                    if (!isAdded() || getResources() == null) return;
                    //addNearbyRow(1, new RMenuItemRow(failedLoadString));
                }
            });
        } else {
            Log.w(TAG, "Couldn't get location provider, can't find nearby stops");
            //addNearbyRow(1, new RMenuItemRow(getString(R.string.failed_location)));
        }

    }

    private void addNearbyRow(StopStub stopStub) {
        mNearbyStops.add(stopStub);
        mAdapter.notifyDataSetChanged();
    }

    private void clearNearbyRows() {
        mNearbyStops.clear();
        mAdapter.notifyDataSetChanged();
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
