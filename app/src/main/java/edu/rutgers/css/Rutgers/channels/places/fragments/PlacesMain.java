package edu.rutgers.css.Rutgers.channels.places.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBufferObserver;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.PlaceAutoCompleteAdapter;
import edu.rutgers.css.Rutgers.interfaces.GoogleApiClientProvider;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.KeyValPair;
import edu.rutgers.css.Rutgers.model.RutgersAPI;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGD;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGI;
import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * <p>The main Places fragment displays nearby Rutgers locations (buildings, parks, etc.), as well as
 * a search bar allowing the user to find places by name or building code.</p>
 *
 * <p>Places selected from this fragment are displayed with {@link PlacesDisplay}.</p>
 * @author James Chambers
 */
public class PlacesMain extends BaseChannelFragment
        implements GoogleApiClient.ConnectionCallbacks, LocationListener {

    /* Log tag and component handle */
    private static final String TAG                 = "PlacesMain";
    public static final String HANDLE               = "places";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* State tags */
    private static final String SEARCHING_TAG       = "searching";
    private static final String SEARCH_TAG          = "search";

    private PublishSubject<Location> locationSubject = PublishSubject.create();

    /* Member data */
    private PlaceAutoCompleteAdapter mSearchAdapter;
    private SimpleSectionedAdapter<KeyValPair> mAdapter;
    private GoogleApiClientProvider mGoogleApiClientProvider;
    private LocationRequest mLocationRequest;
    private AutoCompleteTextView autoComp;
    private boolean searching = false;
    private String oldSearch;

    public PlacesMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main places screen. */
    public static Bundle createArgs(@NonNull String title) {
        final Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, PlacesMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
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
    public void onSaveInstanceState(Bundle outBundle) {
        super.onSaveInstanceState(outBundle);
        outBundle.putBoolean(SEARCHING_TAG, searching);
        outBundle.putString(SEARCH_TAG, autoComp.getText().toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSearchAdapter = new PlaceAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line);
        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1000);

        if (savedInstanceState != null) {
            searching = savedInstanceState.getBoolean(SEARCHING_TAG);
            oldSearch = savedInstanceState.getString(SEARCH_TAG);
        }
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_places,
                CreateArgs.builder().toolbarRes(R.id.toolbar_search).build());

        // Set title from JSON
        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.places_title);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);

        autoComp = (AutoCompleteTextView) v.findViewById(R.id.buildingSearchField);
        autoComp.setAdapter(mSearchAdapter);

        // Item selected from auto-complete list
        autoComp.setOnItemClickListener((parent1, view, position, id) -> {
            KeyValPair placeStub = (KeyValPair) parent1.getAdapter().getItem(position);
            Bundle newArgs = PlacesDisplay.createArgs(placeStub.getValue(), placeStub.getKey());
            switchFragments(newArgs);
        });
        
        // Text placed in field from soft-keyboard/autocomplete (may happen in landscape)
        autoComp.setOnEditorActionListener((view, actionId, event) -> actionId == EditorInfo.IME_ACTION_SEARCH);

        if (searching && oldSearch != null) {
            autoComp.setText(oldSearch);
        }

        // Click listener for nearby places list
        listView.setOnItemClickListener((parent12, view, position, id) -> {
            KeyValPair placeStub = (KeyValPair) parent12.getItemAtPosition(position);

            if (placeStub.getKey() != null) {
                Bundle newArgs = PlacesDisplay.createArgs(placeStub.getValue(), placeStub.getKey());
                switchFragments(newArgs);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_and_share, menu);
        MenuItem searchButton = menu.findItem(R.id.search_button_toolbar);

        if (searching) {
            searchButton.setIcon(R.drawable.ic_clear_black_24dp);
        } else {
            searchButton.setIcon(R.drawable.ic_search_white_24dp);
        }
    }

    public Link getLink() {
        return new Link("places", new ArrayList<>(), getLinkTitle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle options button
        if (item.getItemId() == R.id.search_button_toolbar) {
            searching = !searching;
            updateSearchUI();
            return true;
        }
        return false;
    }

    private void updateSearchUI() {
        if (searching) {
            autoComp.setVisibility(View.VISIBLE);
            autoComp.requestFocus();
            getToolbar().setBackgroundColor(getResources().getColor(R.color.white));
            AppUtils.openKeyboard(getActivity());
        } else {
            autoComp.setVisibility(View.GONE);
            autoComp.setText("");
            getToolbar().setBackgroundColor(getResources().getColor(R.color.actbar_new));
            AppUtils.closeKeyboard(getActivity());
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClientProvider != null) mGoogleApiClientProvider.registerListener(this);

        // Reload nearby places
        showProgressCircle();
        updateSearchUI();
        final String noneNearbyString = getString(R.string.places_none_nearby);
        final String nearbyPlacesString = getString(R.string.places_nearby);
        locationSubject.asObservable().observeOn(Schedulers.io())
            .flatMap(location -> RutgersAPI.getPlacesNear(location.getLatitude(), location.getLongitude()))
            .flatMap(Observable::from)
            .map(place -> new KeyValPair(place.getId(), place.getTitle()))
            .defaultIfEmpty(new KeyValPair(null, noneNearbyString))
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .compose(bindToLifecycle())
            .subscribe(nearbyPlaces -> {
                reset();
                mAdapter.add(new SimpleSection<>(nearbyPlacesString, nearbyPlaces));
            }, this::logError);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClientProvider != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClientProvider.getGoogleApiClient(), this);
            mGoogleApiClientProvider.unregisterListener(this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGI(TAG, "Connected to services");

        // When location services are restored, retry loading nearby places.
        // Make sure this isn't called before the activity has been attached
        // or before onCreate() has ran.
        if (mAdapter != null && isAdded()) {
            final String connectingString = getString(R.string.location_connecting);
            final String failedLocationString = getString(R.string.failed_location);
            final String nearbyPlacesString = getString(R.string.places_nearby);

            final List<KeyValPair> nearbyPlaces = new ArrayList<>();
            final SimpleSection<KeyValPair> nearbyPlacesSection = new SimpleSection<>(nearbyPlacesString, nearbyPlaces);

            // Check for location services
            if (mGoogleApiClientProvider == null || !mGoogleApiClientProvider.getGoogleApiClient().isConnected()) {
                LOGW(TAG, "Location services not connected");
                nearbyPlaces.add(new KeyValPair(null, connectingString));
                mAdapter.clear();
                mAdapter.add(nearbyPlacesSection);
                hideProgressCircle();
                return;
            }

            // Get last location
            final Location lastLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClientProvider.getGoogleApiClient());
            if (lastLoc == null) {
                LOGW(TAG, "Couldn't get location");
                nearbyPlaces.add(new KeyValPair(null, failedLocationString));
                mAdapter.clear();
                mAdapter.add(nearbyPlacesSection);
                hideProgressCircle();
                if (hasLocationPermission()) {
                    requestGPS();
                } else {
                    requestLocationUpdates();
                }
                return;
            }

            loadNearby(lastLoc);
        }
    }

    private void loadNearby(Location location) {
        showProgressCircle();

        locationSubject.onNext(location);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGI(TAG, "Suspended from services for cause: " + cause);
    }

    private void reset() {
        mAdapter.clear();
        hideProgressCircle();
    }

    private void requestLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClientProvider.getGoogleApiClient(), mLocationRequest, this);
        } catch (SecurityException e) {
            LOGE(TAG, e.getMessage());
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        loadNearby(location);
    }
}
