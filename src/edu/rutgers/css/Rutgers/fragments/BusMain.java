package edu.rutgers.css.Rutgers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.androidquery.AQuery;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.R.id;
import edu.rutgers.css.Rutgers.R.layout;

public class BusMain extends Fragment {
	private AQuery aq;
	private FragmentTabHost mTabHost;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		aq = new AQuery(this.getActivity());
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busmain, parent, false);
		
        mTabHost = (FragmentTabHost) v.findViewById(R.id.bus_tabhost);
        mTabHost.setup(getActivity(), getFragmentManager(), R.id.realtabcontent);
        
        mTabHost.addTab(mTabHost.newTabSpec("busroutes").setIndicator("Routes"), BusRoutes.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("busstops").setIndicator("Stops"), BusStops.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("busall").setIndicator("All"), BusAll.class, null);

		
		return v;
	}
}
