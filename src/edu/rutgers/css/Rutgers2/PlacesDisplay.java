package edu.rutgers.css.Rutgers2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class PlacesDisplay extends Fragment {

	public PlacesDisplay() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_place_display, container, false);
		Bundle args = getArguments();
		
		if(args.get("title") != null) getActivity().setTitle(args.getString("title"));
		
		return v;
	}

}
