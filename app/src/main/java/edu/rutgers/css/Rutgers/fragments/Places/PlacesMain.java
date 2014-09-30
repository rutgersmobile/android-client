package edu.rutgers.css.Rutgers.fragments.Places;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.android.gms.common.GooglePlayServicesClient;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.adapters.PlaceAutoCompleteAdapter;
import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers.interfaces.LocationClientProvider;
import edu.rutgers.css.Rutgers.items.KeyValPair;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * <p>The main Places fragment displays nearby Rutgers locations (buildings, parks, etc.), as well as
 * a search bar allowing the user to find places by name or building code.</p>
 *
 * <p>Places selected from this fragment are displayed with {@link edu.rutgers.css.Rutgers.fragments.Places.PlacesDisplay}.</p>
 * @author James Chambers
 */
public class PlacesMain extends Fragment implements GooglePlayServicesClient.ConnectionCallbacks {

	private static final String TAG = "PlacesMain";
    public static final String HANDLE = "places";

	private PlaceAutoCompleteAdapter mSearchAdapter;
	private ArrayList<RMenuRow> mNearbyData;
    private RMenuAdapter mNearbyAdapter;
    private ProgressBar mProgressCircle;
    private LocationClientProvider mLocationClientProvider;

	public PlacesMain() {
		// Required empty public constructor
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "Attaching to activity");
        try {
            mLocationClientProvider = (LocationClientProvider) activity;
        } catch(ClassCastException e) {
            mLocationClientProvider = null;
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "Detaching from activity");
        mLocationClientProvider = null;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSearchAdapter = new PlaceAutoCompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line);

        // Get nearby places & populate nearby places list
        mNearbyData = new ArrayList<RMenuRow>();
        mNearbyAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mNearbyData);
    }
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_places, parent, false);
		Bundle args = getArguments();

		// Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.places_title);

        mProgressCircle = (ProgressBar) v.findViewById(R.id.progressCircle);

        ListView listView = (ListView) v.findViewById(R.id.listView);
        listView.setAdapter(mNearbyAdapter);

		final AutoCompleteTextView autoComp = (AutoCompleteTextView) v.findViewById(R.id.buildingSearchField);
		autoComp.setAdapter(mSearchAdapter);

		// Item selected from auto-complete list
		autoComp.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Launch Places display fragment
				Bundle args = new Bundle();
				KeyValPair placeStub = (KeyValPair) parent.getAdapter().getItem(position);

				args.putString("component", PlacesDisplay.HANDLE);
				args.putString("placeKey", placeStub.getKey());
				args.putString("title", placeStub.getValue());
				
				ComponentFactory.getInstance().switchFragments(args);
			}
			
		});
		
		// Text placed in field from soft-keyboard/autocomplete (may happen in landscape)
		autoComp.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_SEARCH) return true;
				else return false;
			}
			
		});

        // Clear search bar
        ImageButton clearSearchButton = (ImageButton) v.findViewById(R.id.filterClearButton);
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
                RMenuItemRow clickedItem = (RMenuItemRow) parent.getItemAtPosition(position);
                ComponentFactory.getInstance().switchFragments(clickedItem.getArgs());
            }
        });

        return v;
	}

    @Override
    public void onResume() {
        super.onResume();

        if(mLocationClientProvider != null) mLocationClientProvider.registerListener(this);

        // Don't update the screen if the places fragment isn't on top
        if(!AppUtil.isOnTop(PlacesMain.HANDLE)) {
            Log.v(TAG, "onResume(): Not on top, not updating nearby places");
        }
        // Reload nearby places
        else loadNearbyPlaces();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mLocationClientProvider != null) mLocationClientProvider.unregisterListener(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to services");

        // When location services are restored, retry loading nearby places.
        // Make sure this isn't called before the activity has been attached
        // or before onCreate() has ran.
        if(mNearbyData != null && isAdded()) {
            // Don't update the screen if the places fragment isn't on top
            if(!AppUtil.isOnTop(PlacesMain.HANDLE)) {
                Log.v(TAG, "onConnected(): Not on top, not updating nearby places");
            }
            else loadNearbyPlaces();
        }
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Disconnected from services");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mProgressCircle = null;
    }

    private void loadNearbyPlaces() {
        if(!isAdded()) return;

        final String nearbyPlacesString = getString(R.string.places_nearby);
        final String noneNearbyString = getString(R.string.places_none_nearby);
        final String failedLoadString = getString(R.string.failed_load_short);
        final String failedLocationString = getString(R.string.failed_location);
        final String connectingString = getString(R.string.location_connecting);

        mNearbyAdapter.clear();

        // Check for location services
        if(mLocationClientProvider == null || !mLocationClientProvider.getLocationClient().isConnected()) {
            Log.w(TAG, "Location services not connected");
            mNearbyAdapter.add(new RMenuHeaderRow(nearbyPlacesString));
            mNearbyAdapter.add(new RMenuItemRow(connectingString));
            return;
        }

        // Get last location
        final Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
        if (lastLoc == null) {
            Log.w(TAG, "Couldn't get location");
            mNearbyAdapter.add(new RMenuHeaderRow(nearbyPlacesString));
            mNearbyAdapter.add(new RMenuItemRow(failedLocationString));
            return;
        }

        showProgressCircle();

        Places.getPlacesNear(lastLoc.getLatitude(), lastLoc.getLongitude()).done(new DoneCallback<List<KeyValPair>>() {

            @Override
            public void onDone(List<KeyValPair> result) {
                mNearbyAdapter.clear();
                mNearbyAdapter.add(new RMenuHeaderRow(nearbyPlacesString));

                if (result.isEmpty())
                    mNearbyAdapter.add(new RMenuItemRow(noneNearbyString));
                else {
                    for (KeyValPair placeStub : result) {
                        Bundle args = new Bundle();
                        args.putString("component", PlacesDisplay.HANDLE);
                        args.putString("title", placeStub.getValue());
                        args.putString("placeKey", placeStub.getKey());
                        mNearbyAdapter.add(new RMenuItemRow(args));
                    }
                }

            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception result) {
                Log.e(TAG, result.getMessage());
                mNearbyAdapter.clear();
                mNearbyAdapter.add(new RMenuHeaderRow(nearbyPlacesString));
                mNearbyAdapter.add(new RMenuItemRow(failedLoadString));
            }

        }).always(new AlwaysCallback<List<KeyValPair>, Exception>() {

            @Override
            public void onAlways(Promise.State state, List<KeyValPair> resolved, Exception rejected) {
                hideProgressCircle();
            }

        });

    }

    private void showProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.VISIBLE);
    }

    private void hideProgressCircle() {
        if(mProgressCircle != null) mProgressCircle.setVisibility(View.GONE);
    }

}
