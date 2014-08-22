package edu.rutgers.css.Rutgers.fragments.Bus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import java.util.List;

import edu.rutgers.css.Rutgers.items.FilterFocusBroadcaster;
import edu.rutgers.css.Rutgers.items.FilterFocusListener;
import edu.rutgers.css.Rutgers2.R;

public class BusMain extends Fragment implements FilterFocusListener {

    private static final String TAG = "BusMain";
    public static final String HANDLE = "bus";

	private FragmentTabHost mTabHost;

	public BusMain() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_bus_main, parent, false);
		Bundle args = getArguments();

        // Set title from JSON
        if(args.getString("title") != null) getActivity().setTitle(args.getString("title"));
        else getActivity().setTitle(R.string.bus_title);
		
        mTabHost = (FragmentTabHost) v.findViewById(R.id.bus_tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        
        mTabHost.addTab(mTabHost.newTabSpec(BusRoutes.HANDLE).setIndicator(getResources().getString(R.string.bus_routes_tab)), BusRoutes.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(BusStops.HANDLE).setIndicator(getResources().getString(R.string.bus_stops_tab)), BusStops.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(BusAll.HANDLE).setIndicator(getResources().getString(R.string.bus_all_tab)), BusAll.class, null);

        List<Fragment> frags = getChildFragmentManager().getFragments();
        if(frags != null) {
            for(Fragment frag: frags) {
                Log.d(TAG, "Fragment " + frag.toString());
            }
        }
        else Log.d(TAG, "couldn't get fragments");

        FilterFocusBroadcaster routesFragment = (BusRoutes) getChildFragmentManager().findFragmentByTag(BusRoutes.HANDLE);
        FilterFocusBroadcaster stopsFragment = (BusStops) getChildFragmentManager().findFragmentByTag(BusStops.HANDLE);
        if(routesFragment != null) routesFragment.setListener(this);
        if(stopsFragment != null) stopsFragment.setListener(this);

		return v;
	}

    @Override
    public void focusEvent() {
        Log.i(TAG, "Got focus event");
    }

}
