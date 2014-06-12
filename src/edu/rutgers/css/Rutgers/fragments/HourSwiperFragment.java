package edu.rutgers.css.Rutgers.fragments;

import edu.rutgers.css.Rutgers2.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HourSwiperFragment extends Fragment {

	
	
	public HourSwiperFragment() {
		// Required empty public constructor
	}
	
	public static HourSwiperFragment newInstance() {
		HourSwiperFragment newFrag =  new HourSwiperFragment();
		Bundle args = new Bundle();
		newFrag.setArguments(args);
		return newFrag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.hour_swiper, container, false);
		View v = (View) inflater.inflate(R.layout.hour_swiper, container, false);
		
		return v;
	}
	
}
