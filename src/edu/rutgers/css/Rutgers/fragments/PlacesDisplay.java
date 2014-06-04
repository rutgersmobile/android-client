package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers2.R;

/**
 * Place information display component
 * 
 */
public class PlacesDisplay extends Fragment {

	private static final String TAG = "PlacesDisplay";

	public PlacesDisplay() {
		// Required empty public constructor
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
		final LinearLayout nearbyBusesLinearLayout = (LinearLayout) v.findViewById(R.id.nearbyBusesLinearLayout);
		
		if(args.get("placeJSON") != null) {
			JSONObject placeJSON;
			
			// Read JSON data for building
			try {
				placeJSON = new JSONObject(args.getString("placeJSON"));
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage());
				return v;
			}
			
			// Set title
			try {
				getActivity().setTitle(placeJSON.getString("title"));
			} catch (JSONException e) {
				Log.w(TAG, e.getMessage());
				getActivity().setTitle(getResources().getString(R.string.places_title));
			}

			// Display building info
			try {
				
				addressTextView.setText(formatAddress(placeJSON.getJSONObject("location")));
				buildingNoTextView.setText(placeJSON.getString("building_number"));
				campusNameTextView.setText(placeJSON.getString("campus_name"));
				descriptionTextView.setText(placeJSON.getString("description"));
				officesTextView.setText(formatOffices(placeJSON.getJSONArray("offices")));
				
				// Get & display nearby bus stops
				JSONObject location = placeJSON.getJSONObject("location");
				float buildLon = Float.parseFloat(location.getString("longitude"));
				float buildLat = Float.parseFloat(location.getString("latitude"));
				
				Nextbus.getStopsNear(buildLat, buildLon).then(new DoneCallback<ArrayList<JSONObject>>() {

					@Override
					public void onDone(ArrayList<JSONObject> nearbyStops) {
						for(JSONObject stop : nearbyStops) {
							try {
								TextView newStopTextView = new TextView(v.getContext());
								String stopTitle = stop.getString("title");
								newStopTextView.setText(stopTitle);
								nearbyBusesLinearLayout.addView(newStopTextView);
							} catch(JSONException e) {
								Log.e(TAG, e.getMessage());
							}
						}
					}
					
				});
				
			} catch (JSONException e) {
				Log.w(TAG, e.getMessage());
			}
						
/*			TextView test = new TextView(this.getActivity());
			test.setText("TEST");
			TextView test2 = new TextView(this.getActivity());
			test2.setText("TEST2");
			nearbyBusesLinearLayout.addView(test);
			nearbyBusesLinearLayout.addView(test2);*/
			
		}					
		
		return v;
	}
	
	/**
	 * Put JSON address object into readable string form
	 * @param address JSON address object from place data
	 * @return Multi-line string containing address
	 */
	private static String formatAddress(JSONObject address) {
		String result = "";
		
		try {
			result += address.getString("name") + "\n";
			result += address.getString("street") + "\n";
			result += address.getString("city") + ", " + address.getString("state_abbr") + " " + address.getString("postal_code");
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Add each value from the Offices array to a single string with line breaks
	 * @param offices Offices JSON Array
	 * @return Multi-line string listing offices
	 */
	private static final String formatOffices(JSONArray offices) {
		String result = "";
		
		for(int i = 0; i < offices.length(); i++) {
			try {
				result += offices.getString(i) + "\n";
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
		}
		
		return result;
	}
	
}
