package edu.rutgers.css.Rutgers.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.rutgers.css.Rutgers2.R;

public class MainScreen extends Fragment {

	public MainScreen() {
		// Required empty public constructor
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_main_screen, container, false);
		
		getActivity().setTitle(R.string.app_name);
		
		return v;
	}
}
