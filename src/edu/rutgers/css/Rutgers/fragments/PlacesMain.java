package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Places;
import edu.rutgers.css.Rutgers2.R;

/**
 * Main Places component: displays a text field with auto-complete information from places database.
 * User enters a building name and selects from list; selection sent to place display component.
 */
public class PlacesMain extends Fragment {

	private static final String TAG = "PlacesMain";
	private ArrayList<PlaceTuple> mList;
	private ArrayAdapter<PlaceTuple> mAdapter;
	private JSONObject mData;
	
	public PlacesMain() {
		// Required empty public constructor
	}
	
	private class PlaceTuple implements Comparable<PlaceTuple> {
		private String key;
		private JSONObject placeJson;
		
		public PlaceTuple(String key, JSONObject placeJson) {
			this.key = key;
			this.placeJson = placeJson;
		}
		
		public String getKey() {
			return this.key;
		}
		
		public JSONObject getPlaceJSON() {
			return this.placeJson;
		}
		
		@Override
		public String toString() {
			try {
				return placeJson.getString("title");
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				return key;
			}
		}

		@Override
		public int compareTo(PlaceTuple another) {
			// Order by 'title' field alphabetically (or by key if getting title string fails)
			try {
				String thisTitle = getPlaceJSON().getString("title");
				String thatTitle = another.getPlaceJSON().getString("title");
				return thisTitle.compareTo(thatTitle);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				return getKey().compareTo(another.getKey()); 
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mList = new ArrayList<PlaceTuple>();
		mAdapter = new ArrayAdapter<PlaceTuple>(getActivity(), android.R.layout.simple_dropdown_item_1line, mList);
		
		// Get places data
		Places.getPlaces().done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject json) {
				mData = json;
				
				// Grab "all" field and add title from each object inside
				try {
					@SuppressWarnings("unchecked")
					Iterator<String> curKey = json.keys();
					while(curKey.hasNext()) {
						String key = curKey.next();
						JSONObject curBuilding = json.getJSONObject(key);
						PlaceTuple newPT = new PlaceTuple(key, curBuilding);
						mAdapter.add(newPT);
					}
					Collections.sort(mList);
				} catch (JSONException e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
				
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_places, parent, false);
		Bundle args = getArguments();
		
		if(args.get("title") != null) getActivity().setTitle(args.getString("title"));

		AutoCompleteTextView autoComp = (AutoCompleteTextView) v.findViewById(R.id.buildingSearchField);
		autoComp.setAdapter(mAdapter);
		
		/* Item selected from auto-complete list */
		autoComp.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/* Close soft keyboard */
				InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				
				/* Launch Places display fragment */
				FragmentManager fm = getActivity().getSupportFragmentManager();
				Bundle args = new Bundle();
				PlaceTuple placeTuple = (PlaceTuple) parent.getAdapter().getItem(position);
				
				args.putString("component", "placesdisplay");
				args.putString("placeKey", placeTuple.getKey());
				args.putString("placeJSON", placeTuple.getPlaceJSON().toString());
				
				Fragment fragment = ComponentFactory.getInstance().createFragment(args);
				fm.beginTransaction()
					.replace(R.id.main_content_frame, fragment)
					.addToBackStack(null)
					.commit(); 
			}
			
		});
		
		/* Text placed in field from soft-keyboard/autocomplete (may happen in landscape) */
		autoComp.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_GO) {
					/* Close soft keyboard */
					InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
					return true;
				}
				
				return false;
			}
			
		});
		
		return v;
	}
	
}
