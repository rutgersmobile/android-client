package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jdeferred.DoneCallback;
import org.json.JSONException;
import org.json.JSONObject;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers2.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Places channel fragment.
 * Displays a text field with auto-complete information from places database.
 * User enters a building name or abbreviation - fragment displays place information.
 */
public class PlacesMain extends Fragment {

	private static final String TAG = "PlacesMain";
	private static final String API_URL = "https://rumobile.rutgers.edu/1/places.txt";
	private ArrayList<String> mList;
	private ArrayAdapter<String> mAdapter;
	private JSONObject mData;
	
	public PlacesMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mList = new ArrayList<String>();
		mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, mList);
		
		// Get places data
		Places.getPlaces().done(new DoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject json) {
				mData = json;
				
				// Grab "all" field and add title from each object inside
				try {
					@SuppressWarnings("unchecked")
					Iterator<String> curKey = json.keys();
					while(curKey.hasNext()) {
						JSONObject curBuilding = json.getJSONObject(curKey.next());
						mAdapter.add(curBuilding.getString("title"));
					}
					Collections.sort(mList);
				} catch (JSONException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
				
			}
			
		});
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_places, parent, false);
		Bundle args = getArguments();
		
		if(args.get("title") != null) getActivity().setTitle(args.getString("title"));

		AutoCompleteTextView autoComp = (AutoCompleteTextView) v.findViewById(R.id.buildingSearchField);
		autoComp.setAdapter(mAdapter);
		autoComp.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				Bundle args = new Bundle();
				String placeName = (String) parent.getAdapter().getItem(position);
				
				args.putString("component", "placesdisplay");
				args.putString("place", placeName);
				
				Fragment fragment = ComponentFactory.getInstance().createFragment(args);
				fm.beginTransaction()
					.replace(R.id.main_content_frame, fragment)
					.addToBackStack(null)
					.commit(); 
			}
			
		});
		return v;
	}
	
}
