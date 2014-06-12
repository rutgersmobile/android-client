package edu.rutgers.css.Rutgers.fragments;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import edu.rutgers.css.Rutgers2.R;

public class HourSwiperFragment extends Fragment {

	private static final String TAG = "HourSwiperFragment";
	
	public HourSwiperFragment() {
		// Required empty public constructor
	}
	
	public static HourSwiperFragment newInstance(String date, JSONObject hours) {
		HourSwiperFragment newFrag =  new HourSwiperFragment();
		Bundle args = new Bundle();
		args.putString("date", date);
		args.putString("hours", hours.toString());
		newFrag.setArguments(args);
		return newFrag;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.hour_swiper, container, false);
		Bundle args = getArguments();
		
		LinearLayout hourSwiperLinearLayout = (LinearLayout) rootView.findViewById(R.id.hourSwiperLinearLayout);
		TextView dateTextView = (TextView) rootView.findViewById(R.id.dateTextView);
		dateTextView.setText(args.getString("date"));
		
		try {
			JSONObject hours = new JSONObject(args.getString("hours"));
			Iterator<String> keys = hours.keys();
			while(keys.hasNext()) {
				String curKey = keys.next();
				TextView newTV = new TextView(getActivity());
				newTV.setText(curKey + "\t" + hours.getString(curKey));
				hourSwiperLinearLayout.addView(newTV);
			}
		} catch(JSONException e) {
			Log.w(TAG, "onCreateView(): " + e.getMessage());
		}
		
		return rootView;
	}
	
}
