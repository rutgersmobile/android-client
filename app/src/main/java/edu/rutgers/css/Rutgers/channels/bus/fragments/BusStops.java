package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
import edu.rutgers.css.Rutgers.channels.bus.model.loader.StopLoader;
import edu.rutgers.css.Rutgers.interfaces.GoogleApiClientProvider;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

public class BusStops extends BaseChannelFragment implements GoogleApiClient.ConnectionCallbacks,
        LoaderManager.LoaderCallbacks<List<SimpleSection<StopStub>>>, LocationListener {

    /* Log tag and component handle */
    private static final String TAG                 = "BusStops";
    public static final String HANDLE               = "busstops";

    private static final int LOADER_ID              = AppUtils.getUniqueLoaderId();
    private static final int LOCATION_REQUEST       = 1;

    /* Constants */
    private static final int REFRESH_INTERVAL = 60 * 2; // nearby stop refresh interval in seconds

    /* Member data */
    private SimpleSectionedRecyclerAdapter<StopStub> mAdapter;
    private GoogleApiClientProvider mGoogleApiClientProvider;
    private LocationRequest mLocationRequest;
    private Location lastLocation;

    public BusStops() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LOGD(TAG, "Attaching to activity");
        mGoogleApiClientProvider = (GoogleApiClientProvider) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LOGD(TAG, "Detaching from activity");
        mGoogleApiClientProvider = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new SimpleSectionedRecyclerAdapter<>(new ArrayList<>(),
            R.layout.row_section_header, R.layout.row_title, R.id.title);
        mAdapter.getPositionClicks()
            .compose(bindToLifecycle())
            .map(stopStub -> BusDisplay.createArgs(stopStub.getTitle(), BusDisplay.STOP_MODE,
                        stopStub.getAgencyTag(), stopStub.getTitle())
            )
            .subscribe(this::switchFragments, this::logError);

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(REFRESH_INTERVAL * 1000)
                .setFastestInterval(1000);
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

        if (mGoogleApiClientProvider != null) {
            mGoogleApiClientProvider.registerListener(this);
        }

        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClientProvider != null) mGoogleApiClientProvider.unregisterListener(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGI(TAG, "Connected to services");

        // Location services reconnected - retry loading nearby stops
        // Make sure this isn't called before onCreate() has ran.
        if (mAdapter != null && isAdded()) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClientProvider.getGoogleApiClient());
            if (location == null) {
                if (hasLocationPermission()) {
                    requestGPS();
                } else {
                    requestLocationUpdates();
                }
                return;
            }
            if (BuildConfig.DEBUG) LOGD(TAG, "Current location: " + location.toString());
            loadNearby(location);
        }
    }

    private void loadNearby(Location location) {
        if (location != null) {
            lastLocation = location;
            getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGI(TAG, "Suspended from services for cause: " + cause);
    }

    @Override
    public Loader<List<SimpleSection<StopStub>>> onCreateLoader(int id, Bundle args) {
        return new StopLoader(getActivity(), lastLocation);
    }

    @Override
    public void onLoadFinished(Loader<List<SimpleSection<StopStub>>> loader, List<SimpleSection<StopStub>> data) {
        if (getContext() == null) return;

        reset();

        mAdapter.addAll(data);

        // Assume an empty response is an error
        if (data.isEmpty()) {
            AppUtils.showFailedLoadToast(getContext());
        }
    }

    @Override
    public void onLoaderReset(Loader<List<SimpleSection<StopStub>>> loader) {
        reset();
    }

    private void reset() {
        mAdapter.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        loadNearby(location);
    }

    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClientProvider.getGoogleApiClient(), mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == LOCATION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        }
    }

    @Override
    public Link getLink() {
        return null;
    }
}
