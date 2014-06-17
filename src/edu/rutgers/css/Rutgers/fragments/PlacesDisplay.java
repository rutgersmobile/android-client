package edu.rutgers.css.Rutgers.fragments;

import java.util.Iterator;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
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
import android.widget.TableRow;
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
			} catch (JSONException e1) {
				Log.e(TAG, e1.getMessage());
				return v;
			}
			
			// Set title
			getActivity().setTitle(placeJSON.optString("title", getResources().getString(R.string.places_title)));

			// Display building info
				
			addressTextView.setText(formatAddress(placeJSON.optJSONObject("location")));
			buildingNoTextView.setText(placeJSON.optString("building_number"));
			campusNameTextView.setText(placeJSON.optString("campus_name"));
			
			if(placeJSON.optString("description").equals("")) {
				descriptionHeaderRow.setVisibility(View.GONE);
				descriptionContentRow.setVisibility(View.GONE);
			}
			else descriptionTextView.setText(placeJSON.optString("description"));
			
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
				
				Nextbus.getStopsByTitleNear("nb", buildLat, buildLon).then(new AndroidDoneCallback<JSONObject>() {

					@Override
					public void onDone(JSONObject nearbyStopsByTitle) {
						// Hide nearby bus view if there aren't any to display
						if(nearbyStopsByTitle.length() == 0) {
							nearbyBusesHeaderRow.setVisibility(View.GONE);
							nearbyBusesContentRow.setVisibility(View.GONE);
							return;
						}
						
						Iterator<String> stopTitleIter = nearbyStopsByTitle.keys();
						while(stopTitleIter.hasNext()) {
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
				
			} catch (JSONException e) {
				Log.w(TAG, e.getMessage());
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
				Log.e(TAG, e.getMessage());
			}
		}
		
		return result;
	}
	
}
