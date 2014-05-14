package edu.rutgers.css.Rutgers.fragments;

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
import android.widget.TextView;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers2.R;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class PlacesDisplay extends Fragment {

	private static final String TAG = "PlacesDisplay";
	
	/**
	 * Put JSON address object into readable string form
	 * @param address JSON address object from place data
	 * @return Address as a multi-line human-readable string
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
		
		if(args.get("placeKey") != null) {
			getActivity().setTitle(args.getString("placeName"));
			
			Places.getPlace(args.getString("placeKey")).done(new DoneCallback<JSONObject>() {

				@Override
				public void onDone(JSONObject json) {
					
					try {
						
						if(json.has("location")) addressTextView.setText(formatAddress(json.getJSONObject("location")));
						if(json.has("building_number")) buildingNoTextView.setText(json.getString("building_number"));
						if(json.has("campus_name")) campusNameTextView.setText(json.getString("campus_name"));
						if(json.has("description")) descriptionTextView.setText(json.getString("description"));
						if(json.has("offices")) officesTextView.setText(formatOffices(json.getJSONArray("offices")));
						
					} catch (JSONException e) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
					
				}
				
			});
		}
		
		return v;
	}

}
