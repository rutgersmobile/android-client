package edu.rutgers.css.Rutgers.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
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
		
		if(args.get("placeJSON") != null) {
			JSONObject placeJSON;
			
			// Read JSON data for building
			try {
				placeJSON = new JSONObject(args.getString("placeJSON"));
			} catch (JSONException e) {
				Log.w(TAG, "onCreateView(): " + e.getMessage());
				return v;
			}
			
			// Set title
			getActivity().setTitle(placeJSON.optString("title", getResources().getString(R.string.places_title)));

			// Set up map
            mMapView = (MapView) v.findViewById(R.id.mapview);
            mMapView.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // disable hardware acceleration

			if(mMapView != null && placeJSON.optJSONObject("location") != null) {
				JSONObject locationJson = placeJSON.optJSONObject("location");
				if(!locationJson.optString("latitude").isEmpty() && !locationJson.optString("longitude").isEmpty()) {
                    // Get latitude & longitude of location
					double lon = Double.parseDouble(locationJson.optString("longitude"));
					double lat = Double.parseDouble(locationJson.optString("latitude"));
                    GeoPoint position = new GeoPoint(lat, lon);

                    // Create map icon for location
                    final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
                    items.add(new OverlayItem(placeJSON.optString("title"), "", position));
                    mLocationOverlays = new ItemizedIconOverlay<OverlayItem>(items, null, new DefaultResourceProxyImpl(getActivity()));

                    // Add the icon and center the map of the location
                    mMapView.getOverlays().add(mLocationOverlays);
                    mMapView.getController().setZoom(18);
                    mMapView.getController().setCenter(position);
				}
                else {
                    // No location set
                    mMapView.setVisibility(View.GONE);
                }
			}
			else {
				if(mMapView == null) Log.e(TAG, "mMapView is null");
			}
			
			// Display building info
			addressTextView.setText(formatAddress(placeJSON.optJSONObject("location")));

            if(!placeJSON.optString("building_number").equals("null")) {
                buildingNoTextView.setText(placeJSON.optString("building_number"));
            }
            else {
                v.findViewById(R.id.buildingNoHeaderRow).setVisibility(View.GONE);
                v.findViewById(R.id.buildingNoContentRow).setVisibility(View.GONE);
            }

            campusNameTextView.setText(placeJSON.optString("campus_name"));
			
			if(placeJSON.optString("description").equals("")) {
				descriptionHeaderRow.setVisibility(View.GONE);
				descriptionContentRow.setVisibility(View.GONE);
			}
			else descriptionTextView.setText(StringEscapeUtils.unescapeHtml4(placeJSON.optString("description")));
			
			if(placeJSON.optString("offices").equals("")) {
				officesHeaderRow.setVisibility(View.GONE);
				officesContentRow.setVisibility(View.GONE);
			}
			else officesTextView.setText(formatOffices(placeJSON.optJSONArray("offices")));

			try {				
				// Get & display nearby bus stops
				JSONObject location = placeJSON.getJSONObject("location");
				float buildLon = Float.parseFloat(location.getString("longitude"));
				float buildLat = Float.parseFloat(location.getString("latitude"));

                if(!placeJSON.optString("campus_name").isEmpty()) {
                    String campus_name = placeJSON.optString("campus_name");
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
		
		return v;
	}
	
	/**
	 * Put JSON address object into readable string form
	 * @param address JSON address object from place data
	 * @return Multi-line string containing address
	 */
	private static String formatAddress(JSONObject address) {
		if(address == null) return "";
		String result = "";
		
		if(!address.optString("name","").equals("")) result += address.optString("name");
		if(!address.optString("street","").equals("")) result += "\n" + address.optString("street");
		if(!address.optString("city","").equals("")) result += "\n" + address.optString("city") + ", " +
				address.optString("state_abbr") + " " + address.optString("postal_code");
		
		return result;
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
