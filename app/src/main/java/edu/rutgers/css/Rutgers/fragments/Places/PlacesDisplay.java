package edu.rutgers.css.Rutgers.fragments.Places;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.rutgers.css.Rutgers.adapters.RMenuAdapter;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers.items.Place;
import edu.rutgers.css.Rutgers.items.RMenuHeaderRow;
import edu.rutgers.css.Rutgers.items.RMenuItemRow;
import edu.rutgers.css.Rutgers.items.RMenuRow;
import edu.rutgers.css.Rutgers.utils.AppUtil;
import edu.rutgers.css.Rutgers2.R;

/**
 * Place information display component
 * 
 */
public class PlacesDisplay extends Fragment {

	private static final String TAG = "PlacesDisplay";
    public static final String HANDLE = "placesdisplay";

    private ItemizedOverlay<OverlayItem> mLocationOverlays;
    private Place mPlace;
    private List<RMenuRow> mData;
    private RMenuAdapter mAdapter;

    private static final Map<String, String> sAgencyMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Busch", "nb");
        put("College Avenue", "nb");
        put("Douglass", "nb");
        put("Cook", "nb");
        put("Livingston", "nb");
        put("Newark", "nwk");
        put("Health Sciences at Newark", "nwk");
    }});

    public PlacesDisplay() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mData = new ArrayList<RMenuRow>();
        mAdapter = new RMenuAdapter(getActivity(), R.layout.row_title, R.layout.row_section_header, mData);

        Bundle args = getArguments();

        // Get place JSON and display it
        String key = args.getString("placeKey");
        if(key == null) {
            AppUtil.showFailedLoadToast(getActivity());
            Log.e(TAG, "placeKey is null");
        } else {

            final String addressHeader = getString(R.string.address_header);
            final String buildingNoHeader = getString(R.string.building_no_header);
            final String campusHeader = getString(R.string.campus_header);
            final String descriptionHeader = getString(R.string.description_header);
            final String officesHeader = getString(R.string.offices_header);
            final String nearbyHeader = getString(R.string.nearby_bus_header);

            final AndroidDeferredManager dm = new AndroidDeferredManager();

            dm.when(Places.getPlace(key)).done(new DoneCallback<Place>() {
                @Override
                public void onDone(Place result) {
                    mPlace = result;

                    // Add address rows
                    if (mPlace.getLocation() != null) {
                        Bundle addressArgs = new Bundle();
                        addressArgs.putString("title", formatAddress(mPlace.getLocation()));
                        addressArgs.putBoolean("addressRow", true);
                        mAdapter.add(new RMenuHeaderRow(addressHeader));
                        mAdapter.add(new RMenuItemRow(addressArgs));
                    }

                    // Add description row
                    if (mPlace.getDescription() != null) {
                        mAdapter.add(new RMenuHeaderRow(descriptionHeader));
                        mAdapter.add(new RMenuItemRow(mPlace.getDescription()));
                    }

                    // List offices housed in this building
                    if (mPlace.getOffices() != null) {
                        mAdapter.add(new RMenuHeaderRow(officesHeader));
                        for (String office : mPlace.getOffices()) {
                            mAdapter.add(new RMenuItemRow(office));
                        }
                    }

                    // Add building number row
                    if (mPlace.getBuildingNumber() != null) {
                        mAdapter.add(new RMenuHeaderRow(buildingNoHeader));
                        mAdapter.add(new RMenuItemRow(mPlace.getBuildingNumber()));
                    }

                    // Add campus rows
                    if (mPlace.getCampusName() != null) {
                        mAdapter.add(new RMenuHeaderRow(campusHeader));
                        mAdapter.add(new RMenuItemRow(mPlace.getCampusName()));
                    }

                    /*
                    // Add nearby bus stops
                    if (mLocationJSON != null) {
                        try {
                            double buildLon = Double.parseDouble(mLocationJSON.getString("longitude"));
                            double buildLat = Double.parseDouble(mLocationJSON.getString("latitude"));

                            if (!JsonUtil.stringIsReallyEmpty(mPlaceJSON, "campus_name")) {
                                String campus_name = mPlaceJSON.optString("campus_name");
                                String agency = sAgencyMap.get(campus_name);

                                if (agency != null) {
                                    dm.when(Nextbus.getStopsByTitleNear(agency, buildLat, buildLon)).then(new DoneCallback<JSONObject>() {

                                        @Override
                                        public void onDone(JSONObject nearbyStopsByTitle) {
                                            // Hide nearby bus view if there aren't any to display
                                            if (nearbyStopsByTitle.length() == 0) {
                                                nearbyBusesHeaderRow.setVisibility(View.GONE);
                                                nearbyBusesContentRow.setVisibility(View.GONE);
                                                return;
                                            }

                                            for (Iterator<String> stopTitleIter = nearbyStopsByTitle.keys(); stopTitleIter.hasNext(); ) {
                                                TextView newStopTextView = new TextView(v.getContext());
                                                String stopTitle = stopTitleIter.next();
                                                newStopTextView.setText(stopTitle);
                                                nearbyBusesLinearLayout.addView(newStopTextView);
                                            }
                                        }

                                    });
                                } else {
                                    nearbyBusesHeaderRow.setVisibility(View.GONE);
                                    nearbyBusesContentRow.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            Log.w(TAG, "mLocationJSON: " + e.getMessage());
                        }
                    }*/

                    /*
                    // Set up map
                    if(mapView != null && mLocationJSON != null) {
                        if(!mLocationJSON.isNull("latitude") && !mLocationJSON.isNull("longitude")) {
                            // Enable launch map activity button
                            mapLaunchButton.setVisibility(View.VISIBLE);
                            mapLaunchButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    launchMap();
                                }
                            });

                            // Get latitude & longitude of location
                            double lon = Double.parseDouble(mLocationJSON.optString("longitude"));
                            double lat = Double.parseDouble(mLocationJSON.optString("latitude"));
                            final GeoPoint position = new GeoPoint(lat, lon);

                            // Create map icon for location
                            final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                            items.add(new OverlayItem(mPlaceJSON.optString("title"), "", position));
                            mLocationOverlays = new ItemizedIconOverlay<OverlayItem>(items, null, new DefaultResourceProxyImpl(getActivity()));

                            // Add the icon and center the map of the location
                            mapView.getOverlays().add(mLocationOverlays);
                            mapView.getController().setZoom(18);
                            mapView.getController().setCenter(position);
                        }
                        else {
                            // No location set
                            mapView.setVisibility(View.GONE);
                        }
                    }
                    else {
                        if(mapView == null) Log.e(TAG, "mapView is null");
                    }
                    */
                }
            }).fail(new FailCallback<Exception>() {
                @Override
                public void onFail(Exception e) {
                    AppUtil.showFailedLoadToast(getActivity());
                    Log.w(TAG, e.getMessage());
                }
            });
        }

    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_place_display, container, false);
		Bundle args = getArguments();

        // Set title
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.places_title);

        /*
        // Set up map
        final MapView mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // disable hardware acceleration
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(false);
        */

        final ListView listView = (ListView) v.findViewById(R.id.list);
        listView.setAdapter(mAdapter);

		return v;
	}

    /**
     * Start a map activity intent for this address/location
     */
    public void launchMap() {
        if(mPlace == null || mPlace.getLocation() == null) return;
        Place.Location location = mPlace.getLocation();

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Prefer address for user readability.
        if(location.getStreet() != null && location.getCity() != null && location.getStateAbbr() != null) {
            StringBuilder address = new StringBuilder();
            address.append(location.getStreet()).append(", ").append(location.getCity()).append(", ").append(location.getStateAbbr());
            intent.setData(Uri.parse("geo:0,0?q=" + address.toString()));
        }
        // Fallback to longitude & latitude
        else {
            intent.setData(Uri.parse("geo:0,0?q="+Double.toString(location.getLatitude())+","+Double.toString(location.getLongitude())));
        }

        // Try to launch a map activity
        try {
            startActivity(intent);
        }  catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
        }
    }

	/**
	 * Compile location information into readable string form
	 * @param location Place location info
	 * @return Multi-line string containing address
	 */
	private static String formatAddress(Place.Location location) {
		if(location == null) return null;

        StringBuilder result = new StringBuilder();

        if(location.getName() != null) result.append(location.getName()).append('\n');
        if(location.getStreet() != null) result.append(location.getStreet()).append('\n');
        if(location.getAdditional() != null) result.append(location.getAdditional()).append('\n');
        if(location.getCity() != null) result.append(location.getCity()).append(", ")
                .append(location.getStateAbbr()).append(' ').append(location.getPostalCode());

		return StringUtils.trim(result.toString());
	}

}
