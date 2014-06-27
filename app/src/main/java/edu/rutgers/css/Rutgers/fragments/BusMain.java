package edu.rutgers.css.Rutgers.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import edu.rutgers.css.Rutgers2.R;

public class BusMain extends Fragment {
	private AQuery aq;
	private FragmentTabHost mTabHost;

	public BusMain() {
		// Required empty public constructor
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		aq = new AQuery(this.getActivity());
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busmain, parent, false);
		
		Bundle args = getArguments();
		getActivity().setTitle(args.getString("title"));
		
        mTabHost = (FragmentTabHost) v.findViewById(R.id.bus_tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        
        mTabHost.addTab(mTabHost.newTabSpec("busroutes").setIndicator(getResources().getString(R.string.bus_routes_tab)), BusRoutes.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("busstops").setIndicator(getResources().getString(R.string.bus_stops_tab)), BusStops.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("busall").setIndicator(getResources().getString(R.string.bus_all_tab)), BusAll.class, null);

		return v;
	}
}
