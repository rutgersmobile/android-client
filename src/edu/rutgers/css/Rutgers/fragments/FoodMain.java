package edu.rutgers.css.Rutgers.fragments;

import edu.rutgers.css.Rutgers.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FoodMain extends Fragment {

	private ListView mList;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_foodmain, parent, false);
		mList = (ListView) v.findViewById(R.id.dining_locations_list);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("title"));
		
		return v;
	}
	
}
