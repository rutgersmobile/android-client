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
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers2.R;

/**
 * Place information display component
 * 
 */
public class PlacesDisplay extends Fragment {

	private static final String TAG = "PlacesDisplay";

    private static Map<String, String> mAgencyMap;
    private MapView mMapView;
    private ItemizedOverlay<OverlayItem> mLocationOverlays;
    private JSONObject mPlaceJSON;
    private JSONObject mLocationJSON;

    public PlacesDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mAgencyMap = new HashMap<String, String>();
        mAgencyMap.put("Busch", "nb");
        mAgencyMap.put("College Avenue", "nb");
        mAgencyMap.put("Douglass", "nb");
        mAgencyMap.put("Cook", "nb");
        mAgencyMap.put("Livingston", "nb");
        mAgencyMap.put("Newark", "nwk");
        mAgencyMap.put("Health Sciences at Newark", "nwk");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_place_display, container, false);
		Bundle args = getArguments();
		
		final TextView addressTextView = (TextView) v.findViewById(R.id.addressTextView);
		final TextView buildingNoTextView = (TextView) v.findViewById(R.id.buildingNoTextView);
		final TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);
		final TextView campusNameTextView = (TextView) v.findViewById(R.id.campusTextView);
		final TextView officesTextView = (TextView) v.findViewById(R.id.officesTextView);
		
		final TableRow descriptionHeaderRow = (TableRow) v.findViewById(R.id.descriptionHeaderRow);
		final TableRow descriptionContentRow = (TableRow) v.findViewById(R.id.descriptionContentRow);
		final TableRow officesHeaderRow = (TableRow) v.findViewById(R.id.officesHeaderRow);
		final TableRow officesContentRow = (TableRow) v.findViewById(R.id.officesContentRow);
		final TableRow nearbyBusesHeaderRow = (TableRow) v.findViewById(R.id.nearbyBusesHeaderRow);
		final TableRow nearbyBusesContentRow = (TableRow) v.findViewById(R.id.nearbyBusesContentRow);
		final LinearLayout nearbyBusesLinearLayout = (LinearLayout) v.findViewById(R.id.nearbyBusesLinearLayout);

        final ImageButton mapLaunchButton = (ImageButton) v.findViewById(R.id.launchMapButton);
		
		if(args.getString("placeJSON") != null) {
			// Read JSON data for building
			try {
				mPlaceJSON = new JSONObject(args.getString("placeJSON"));
			} catch (JSONException e) {
				Log.w(TAG, "onCreateView(): " + e.getMessage());
				return v;
			}
			
			// Set title
			getActivity().setTitle(mPlaceJSON.optString("title", getResources().getString(R.string.places_title)));

			// Set up map
            mMapView = (MapView) v.findViewById(R.id.mapview);
            mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // disable hardware acceleration
            mMapView.setBuiltInZoomControls(false);
            mMapView.setMultiTouchControls(false);

            mLocationJSON = mPlaceJSON.optJSONObject("location");

			if(mMapView != null && mLocationJSON != null) {

				if(!mLocationJSON.optString("latitude").isEmpty() && !mLocationJSON.optString("longitude").isEmpty()) {
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
                    mMapView.getOverlays().add(mLocationOverlays);

                    // This delays the center & zoom until after layout is set up
                    v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mMapView.getController().setZoom(18);
                            mMapView.getController().setCenter(position);
                        }
                    });
				}
                else {
                    // No location set
                    mMapView.setVisibility(View.GONE);
                }
			}
			else {
				if(mMapView == null) Log.e(TAG, "mMapView is null");
			}
			
			// Display address
			addressTextView.setText(formatAddress(mLocationJSON));

            // Display building number
            if(!mPlaceJSON.optString("building_number").equals("null")) {
                buildingNoTextView.setText(mPlaceJSON.optString("building_number"));
            }
            else {
                v.findViewById(R.id.buildingNoHeaderRow).setVisibility(View.GONE);
                v.findViewById(R.id.buildingNoContentRow).setVisibility(View.GONE);
            }

            // Display campus
            campusNameTextView.setText(mPlaceJSON.optString("campus_name"));

            // Display building description
			if(mPlaceJSON.optString("description").isEmpty()) {
				descriptionHeaderRow.setVisibility(View.GONE);
				descriptionContentRow.setVisibility(View.GONE);
			}
			else descriptionTextView.setText(StringEscapeUtils.unescapeHtml4(mPlaceJSON.optString("description")));

            // Display offices housed in this building
			if(mPlaceJSON.optString("offices").isEmpty()) {
				officesHeaderRow.setVisibility(View.GONE);
				officesContentRow.setVisibility(View.GONE);
			}
			else officesTextView.setText(formatOffices(mPlaceJSON.optJSONArray("offices")));

            // Get & display nearby bus stops
			if(mLocationJSON != null) {
                try {
                    double buildLon = Double.parseDouble(mLocationJSON.getString("longitude"));
                    double buildLat = Double.parseDouble(mLocationJSON.getString("latitude"));

                    if(!mPlaceJSON.optString("campus_name").isEmpty()) {
                        String campus_name = mPlaceJSON.optString("campus_name");
                        String agency = mAgencyMap.get(campus_name);

                        if(agency != null) {
                            Nextbus.getStopsByTitleNear(agency, buildLat, buildLon).then(new AndroidDoneCallback<JSONObject>() {

                                @Override
                                public void onDone(JSONObject nearbyStopsByTitle) {
                                    // Hide nearby bus view if there aren't any to display
                                    if (nearbyStopsByTitle.length() == 0) {
                                        nearbyBusesHeaderRow.setVisibility(View.GONE);
                                        nearbyBusesContentRow.setVisibility(View.GONE);
                                        return;
                                    }

                                    Iterator<String> stopTitleIter = nearbyStopsByTitle.keys();
                                    while (stopTitleIter.hasNext()) {
                                        TextView newStopTextView = new TextView(v.getContext());
                                        String stopTitle = stopTitleIter.next();
                                        newStopTextView.setText(stopTitle);
                                        nearbyBusesLinearLayout.addView(newStopTextView);
                                    }
                                }

                                @Override
                                public AndroidExecutionScope getExecutionScope() {
                                    return AndroidExecutionScope.UI;
                                }

                            });
                        }
                        else {
                            nearbyBusesHeaderRow.setVisibility(View.GONE);
                            nearbyBusesContentRow.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "onCreateView(): " + e.getMessage());
                }
            }

		}					
		
		return v;
	}

    /**
     * Start a map activity intent for this address/location
     */
    public void launchMap() {
        if(mLocationJSON == null) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Prefer address for user readability
        if(!mLocationJSON.optString("street").isEmpty() && !mLocationJSON.optString("street").isEmpty() && !mLocationJSON.optString("state").isEmpty()) {
            String addr = mLocationJSON.optString("street") + ", " + mLocationJSON.optString("city") + ", " + mLocationJSON.optString("state");
            intent.setData(Uri.parse("geo:0,0?q=" + addr));
        }
        // Fallback to longitude & latitude
        else {
            intent.setData(Uri.parse("geo:0,0?q=" + mLocationJSON.optString("latitude") + ", " + mLocationJSON.optString("longitude")));
        }

        // Try to launch a map activity
        try {
            startActivity(intent);
        }  catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.failed_no_activity, Toast.LENGTH_SHORT).show();
        }
    }

	/**
	 * Put JSON address object into readable string form
	 * @param address JSON address object from place data
	 * @return Multi-line string containing address
	 */
	private static String formatAddress(JSONObject address) {
		if(address == null) return "";
		StringBuilder result = new StringBuilder();
		
		if(!address.optString("name").isEmpty()) result.append(address.optString("name") + "\n");
		if(!address.optString("street").isEmpty()) result.append(address.optString("street") + "\n");
		if(!address.optString("city").isEmpty()) result.append(address.optString("city") + ", " +
				address.optString("state_abbr") + " " + address.optString("postal_code"));

		return StringUtils.trim(result.toString());
	}
	
	/**
	 * Add each value from the Offices array to a single string with line breaks
	 * @param offices Offices JSON Array
	 * @return Multi-line string listing offices
	 */
	private static final String formatOffices(JSONArray offices) {
		if(offices == null) return "";
		String result = "";
		
		for(int i = 0; i < offices.length(); i++) {
			try {
				result += offices.getString(i);
				if(i != offices.length()-1) result += "\n";
			} catch (JSONException e) {
				Log.w(TAG, "formatOffices(): " + e.getMessage());
			}
		}
		
		return result;
	}
	
}
