package edu.rutgers.css.Rutgers.channels.places.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.GooglePlayServicesClient;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.places.model.Place;
import edu.rutgers.css.Rutgers.channels.places.model.PlaceAutoCompleteAdapter;
import edu.rutgers.css.Rutgers.channels.places.model.PlacesAPI;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.model.KeyValPair;
import edu.rutgers.css.Rutgers.model.SimpleSection;
import edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * <p>The main Places fragment displays nearby Rutgers locations (buildings, parks, etc.), as well as
 * a search bar allowing the user to find places by name or building code.</p>
 *
 * <p>Places selected from this fragment are displayed with {@link PlacesDisplay}.</p>
 * @author James Chambers
 */
public class PlacesMain extends BaseChannelFragment implements GooglePlayServicesClient.ConnectionCallbacks {

    /* Log tag and component handle */
    private static final String TAG                 = "PlacesMain";
    public static final String HANDLE               = "places";

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private PlaceAutoCompleteAdapter mSearchAdapter;
    private SimpleSectionedAdapter<KeyValPair> mAdapter;
    private LocationClientProvider mLocationClientProvider;

    public PlacesMain() {
        // Required empty public constructor
    }

    /** Create argument bundle for main places screen. */
    public static Bundle createArgs(@NonNull String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, PlacesMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LOGD(TAG, "Attaching to activity");
        mLocationClientProvider = (LocationClientProvider) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LOGD(TAG, "Detaching from activity");
        mLocationClientProvider = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchAdapter = new PlaceAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line);
        mAdapter = new SimpleSectionedAdapter<>(getActivity(), R.layout.row_title, R.layout.row_section_header, R.id.title);
    }
    
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_places);

        // Set title from JSON
        final Bundle args = getArguments();
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.places_title);

        final StickyListHeadersListView listView = (StickyListHeadersListView) v.findViewById(R.id.stickyList);
        listView.setAdapter(mAdapter);

        final AutoCompleteTextView autoComp = (AutoCompleteTextView) v.findViewById(R.id.buildingSearchField);
        autoComp.setAdapter(mSearchAdapter);

        // Item selected from auto-complete list
        autoComp.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValPair placeStub = (KeyValPair) parent.getAdapter().getItem(position);
                Bundle newArgs = PlacesDisplay.createArgs(placeStub.getValue(), placeStub.getKey());
                switchFragments(newArgs);
            }
            
        });
        
        // Text placed in field from soft-keyboard/autocomplete (may happen in landscape)
        autoComp.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) return true;
                else return false;
            }
            
        });

        // Clear search bar
        final ImageButton clearSearchButton = (ImageButton) v.findViewById(R.id.filterClearButton);
        clearSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                autoComp.setText("");
            }

        });

        // Click listener for nearby places list
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyValPair placeStub = (KeyValPair) parent.getItemAtPosition(position);

                if (placeStub.getKey() != null) {
                    Bundle newArgs = PlacesDisplay.createArgs(placeStub.getValue(), placeStub.getKey());
                    switchFragments(newArgs);
                }
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLocationClientProvider != null) mLocationClientProvider.registerListener(this);

        // Reload nearby places
        loadNearbyPlaces();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mLocationClientProvider != null) mLocationClientProvider.unregisterListener(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGI(TAG, "Connected to services");

        // When location services are restored, retry loading nearby places.
        // Make sure this isn't called before the activity has been attached
        // or before onCreate() has ran.
        if (mAdapter != null && isAdded()) {
            loadNearbyPlaces();
        }
    }

    @Override
    public void onDisconnected() {
        LOGI(TAG, "Disconnected from services");
    }

    private void loadNearbyPlaces() {
        if (!isAdded()) return;

        LOGI(TAG, "Updating nearby places");

        final String nearbyPlacesString = getString(R.string.places_nearby);
        final String noneNearbyString = getString(R.string.places_none_nearby);
        final String failedLoadString = getString(R.string.failed_load_short);
        final String failedLocationString = getString(R.string.failed_location);
        final String connectingString = getString(R.string.location_connecting);

        final ArrayList<KeyValPair> nearbyPlaces = new ArrayList<>();
        final SimpleSection<KeyValPair> nearbyPlacesSection = new SimpleSection<>(nearbyPlacesString, nearbyPlaces);

        mAdapter.clear();
        mAdapter.add(nearbyPlacesSection);

        // Check for location services
        if (mLocationClientProvider == null || !mLocationClientProvider.getLocationClient().isConnected()) {
            LOGW(TAG, "Location services not connected");
            nearbyPlaces.add(new KeyValPair(null, connectingString));
            mAdapter.notifyDataSetChanged();
            return;
        }

        // Get last location
        final Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
        if (lastLoc == null) {
            LOGW(TAG, "Couldn't get location");
            nearbyPlaces.add(new KeyValPair(null, failedLocationString));
            mAdapter.notifyDataSetChanged();
            return;
        }

        showProgressCircle();

        PlacesAPI.getPlacesNear(lastLoc.getLatitude(), lastLoc.getLongitude()).done(new DoneCallback<List<Place>>() {

            @Override
            public void onDone(List<Place> result) {
                mAdapter.clear();

                if (result.isEmpty()) {
                    nearbyPlaces.add(new KeyValPair(null, noneNearbyString));
                } else {
                    for (Place place: result) {
                        nearbyPlaces.add(new KeyValPair(place.getId(), place.getTitle()));
                    }
                }

                mAdapter.add(nearbyPlacesSection);
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception e) {
                LOGE(TAG, e.getMessage());
                mAdapter.clear();
                nearbyPlaces.add(new KeyValPair(null, failedLoadString));
                mAdapter.add(nearbyPlacesSection);
            }

        }).always(new AlwaysCallback<List<Place>, Exception>() {

            @Override
            public void onAlways(Promise.State state, List<Place> resolved, Exception rejected) {
                hideProgressCircle();
            }

        });

    }

}
