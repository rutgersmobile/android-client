package edu.rutgers.css.Rutgers.channels.bus.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.BuildConfig;
import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.NextbusAPI;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopStub;
import edu.rutgers.css.Rutgers.interfaces.GoogleApiClientProvider;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedRecyclerAdapter;
import edu.rutgers.css.Rutgers.ui.DividerItemDecoration;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;

public class BusStops extends BaseChannelFragment implements GoogleApiClient.ConnectionCallbacks,
    LocationListener {

    /* Log tag and component handle */
    private static final String TAG                 = "BusStops";
    public static final String HANDLE               = "busstops";

    private static final int LOCATION_REQUEST       = 1;

    /* Constants */
    private static final int REFRESH_INTERVAL = 60 * 2; // nearby stop refresh interval in seconds

    /* Member data */
    private SimpleSectionedRecyclerAdapter<StopStub> mAdapter;
    private GoogleApiClientProvider mGoogleApiClientProvider;
    private LocationRequest mLocationRequest;
    private PublishSubject<Location> locationSubject = PublishSubject.create();

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

        mAdapter.getPositionClicks()
            .compose(bindToLifecycle())
            .map(stopStub -> BusDisplay.createArgs(stopStub.getTitle(), BusDisplay.STOP_MODE,
                stopStub.getAgencyTag(), stopStub.getTitle())
            )
            .subscribe(this::switchFragments, this::logError);

        if (mGoogleApiClientProvider != null) {
            mGoogleApiClientProvider.registerListener(this);
        }

        // The null value is to make sure this runs once without a location and will be updated
        // on subsequent calls to locationSubject::onNext
        Observable.merge(Observable.just(null), locationSubject.asObservable()).observeOn(Schedulers.io())
            .flatMap(location -> {
            LOGI(TAG, "Started stop load with location");
            // create our stops with nearby stops initially empty
            final List<SimpleSection<StopStub>> stops = new ArrayList<>();
            final List<StopStub> nearbyStops = new ArrayList<>();
            stops.add(new SimpleSection<>(getContext().getString(R.string.nearby_bus_header), nearbyStops));

            return location != null
                // if we have a location, use it to get nearby stops
                ? NextbusAPI.getActiveStopsByTitleNear(
                    NextbusAPI.AGENCY_NB,
                    (float) location.getLatitude(),
                    (float) location.getLongitude(),
                    Config.NEARBY_RANGE
                ).flatMap(nbNearby -> NextbusAPI.getActiveStopsByTitleNear(
                    NextbusAPI.AGENCY_NB,
                    (float) location.getLatitude(),
                    (float) location.getLongitude(),
                    Config.NEARBY_RANGE
                ).map(nwkNearby -> {
                    for (StopGroup stopGroup : nbNearby) {
                        nearbyStops.add(new StopStub(stopGroup));
                    }
                    for (StopGroup stopGroup : nwkNearby) {
                        nearbyStops.add(new StopStub(stopGroup));
                    }
                    return stops;
                }))

                // otherwise just use our empty nearby sections
                : Observable.just(stops);
        }).flatMap(stops ->
            NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NB).flatMap(nbActive ->
            NextbusAPI.getActiveStops(NextbusAPI.AGENCY_NWK).map(nwkActive -> {
                final String userHome = RutgersUtils.getHomeCampus(getContext());
                final boolean nbHome = userHome.equals(getContext().getString(R.string.campus_nb_full));

                // add in all of the active stops
                // ordered based on current home campus
                if (nbHome) {
                    stops.add(loadAgency(NextbusAPI.AGENCY_NB, nbActive));
                    stops.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActive));
                } else {
                    stops.add(loadAgency(NextbusAPI.AGENCY_NWK, nwkActive));
                    stops.add(loadAgency(NextbusAPI.AGENCY_NB, nbActive));
                }

                return stops;
            }))
        )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .subscribe(simpleSections -> {
            reset();
            mAdapter.addAll(simpleSections);
        }, this::logError);
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
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClientProvider.getGoogleApiClient()
            );
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
            locationSubject.onNext(location);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGI(TAG, "Suspended from services for cause: " + cause);
    }

    private void reset() {
        mAdapter.clear();
    }

    @Override
    public void onLocationChanged(Location location) {
        loadNearby(location);
    }

    // This is shown as an error for some reason. As far as I know it works fine
    // Permissions are all checked as expected, but Android Studio doesn't seem to like it
    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
            mGoogleApiClientProvider.getGoogleApiClient(),
            mLocationRequest,
            this
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        }
    }

    /**
     * Populate list with bus stops for agency, with a section header for that agency.
     * @param agency Agency tag
     * @param stopStubs List of stop stubs (titles/geohashes) for that agency
     */
    private SimpleSection<StopStub> loadAgency(@NonNull String agency, @NonNull List<StopStub> stopStubs) {
        String header;
        switch (agency) {
            case NextbusAPI.AGENCY_NB:
                header = getContext().getString(R.string.bus_nb_active_stops_header);
                break;
            case NextbusAPI.AGENCY_NWK:
                header = getContext().getString(R.string.bus_nwk_active_stops_header);
                break;
            default:
                throw new IllegalArgumentException("Invalid Nextbus agency \"" + agency + "\"");
        }

        return new SimpleSection<>(header, stopStubs);
    }

    @Override
    public Link getLink() {
        return null;
    }
}
