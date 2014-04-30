package edu.rutgers.css.Rutgers.fragments;

import edu.rutgers.css.Rutgers2.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Places channel fragment.
 * Displays a text field with auto-complete information from places database.
 * User enters a building name or abbreviation - fragment displays place information.
 */
public class PlacesMain extends Fragment {

	public PlacesMain() {
		// Required empty constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_places, parent, false);
		
		return v;
	}
	
}
