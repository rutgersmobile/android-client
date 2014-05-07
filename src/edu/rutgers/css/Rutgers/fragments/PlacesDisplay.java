package edu.rutgers.css.Rutgers.fragments;

import org.jdeferred.DoneCallback;
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
	
	public PlacesDisplay() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_place_display, container, false);
		Bundle args = getArguments();
		
		final TextView addressTextView = (TextView) v.findViewById(R.id.addressTextView);
		final TextView buildingNoTextView = (TextView) v.findViewById(R.id.buildingNoTextView);
		final TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);
		
		if(args.get("place") != null) {
			getActivity().setTitle(args.getString("place"));
			
			Places.getPlace(args.getString("place")).done(new DoneCallback<JSONObject>() {

				@Override
				public void onDone(JSONObject json) {
					try {
						if(json.has("location")) addressTextView.setText(json.getJSONObject("location").toString());
						if(json.has("building_number")) buildingNoTextView.setText(json.getString("building_number"));
					} catch (JSONException e) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
				}
				
			});
		}
		
		return v;
	}

}
