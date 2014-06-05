package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Iterator;

import org.jdeferred.DoneCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.api.Nextbus;
import edu.rutgers.css.Rutgers.auxiliary.LocationClientProvider;
import edu.rutgers.css.Rutgers.auxiliary.RMenuAdapter;
import edu.rutgers.css.Rutgers.auxiliary.RMenuPart;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuHeader;
import edu.rutgers.css.Rutgers.auxiliary.SlideMenuItem;
import edu.rutgers.css.Rutgers2.R;

public class BusStops extends Fragment {

	private static final String TAG = "BusStops";
	private ListView mList;
	private RMenuAdapter mAdapter;
	private ArrayList<RMenuPart> mData;
	private LocationClientProvider mLocationClientProvider;
	
	public BusStops() {
		// Required empty public constructor
	}
	
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mLocationClientProvider = (LocationClientProvider) activity;
		} catch(ClassCastException e) {
			mLocationClientProvider = null;
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mData = new ArrayList<RMenuPart>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.title_row, R.layout.main_drawer_header, mData);
		
		loadNearbyStops("nb");
		loadAgency("nb", getResources().getString(R.string.bus_nb_active_stops_header));
		loadAgency("nwk", getResources().getString(R.string.bus_nwk_active_stops_header));
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busstops, parent, false);
		
		mList = (ListView) v.findViewById(R.id.list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new OnItemClickListener() {

			/**
			 * Clicking on one of the stops will launch the bus display in stop mode, which lists routes going through that stop.
			 */
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SlideMenuItem clickedItem = (SlideMenuItem) parent.getAdapter().getItem(position);
				Bundle clickedArgs = clickedItem.getArgs();
						
				Bundle args = new Bundle();
				args.putString("component", "busdisplay");
				args.putString("mode", "stop");
				args.putString("title", clickedArgs.getString("title"));
				args.putString("agency", clickedArgs.getString("agency"));
				
				FragmentManager fm = getActivity().getSupportFragmentManager();
				Fragment fragment = ComponentFactory.getInstance().createFragment(args);
				
				if(fragment != null) {
					fm.beginTransaction()
						.replace(R.id.main_content_frame, fragment)
						.addToBackStack(null)
						.commit();
				}
			}
			
		});
		
		return v;
	}
	
	/**
	 * Populate list with bus stops for agency, with a section header for that agency.
	 * Resulting JSON array looks like this:
	 * 	[{"geoHash":"dr5rb1qnk35gxur","title":"1 Washington Park"},{"geoHash":"dr5pzbvwhvpjst4","title":"225 Warren St"}]
	 * @param agencyTag Agency tag for API request
	 * @param agencyTitle Header title that goes above these stops
	 */
	private void loadAgency(final String agencyTag, final String agencyTitle) {
		Nextbus.getStops(agencyTag).then(new DoneCallback<JSONArray>() {
			
			@Override
			public void onDone(JSONArray data) {
				//Log.d(TAG, "loadAgency(): " + data.toString());
				
				// Create an item in the list for each stop from the array
				mAdapter.add(new SlideMenuHeader(agencyTitle));
				for(int i = 0; i < data.length(); i++) {
					try {
						JSONObject jsonObj = data.getJSONObject(i);
						Bundle menuBundle = new Bundle();
						menuBundle.putString("title", jsonObj.getString("title"));
						//menuBundle.putString("json", jsonObj.toString());
						menuBundle.putString("agency", agencyTag);
						SlideMenuItem newMenuItem = new SlideMenuItem(menuBundle);
						mAdapter.add(newMenuItem);
					} catch (JSONException e) {
						Log.e(TAG, "loadAgency(): " + e.getMessage());
					}
				}
			}
			
		});
	}
	
	/**
	 * Populate list with active nearby stops for an agency
	 * @param agencyTag Agency tag for API request
	 */
	private void loadNearbyStops(final String agencyTag) {
		
		// Check for location services
		if(mLocationClientProvider != null && mLocationClientProvider.servicesConnected()) {
			// Get last location
			Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
			Log.d(TAG, "Current location: " + lastLoc.toString());
			
			// Look up nearby active bus stops
			Nextbus.getActiveStopsByTitleNear(agencyTag, (float) lastLoc.getLatitude(), (float) lastLoc.getLongitude()).then(new DoneCallback<JSONObject>() {

				@Override
				public void onDone(JSONObject activeNearbyStops) {
					// If there aren't any results, don't do anything
					if(activeNearbyStops.length() == 0) return;
					
					// Add section header
					mAdapter.add(new SlideMenuHeader(getActivity().getResources().getString(R.string.bus_nearby_active_stops_header)));
					
					Iterator<String> stopTitleIter = activeNearbyStops.keys();
					while(stopTitleIter.hasNext()) {
						try {
							String curTitle = stopTitleIter.next();
							JSONObject curStop = activeNearbyStops.getJSONObject(curTitle);
							
							//curStop.put("title", curTitle); // title field required in the JSON for click events
							
							Bundle menuBundle = new Bundle();
							menuBundle.putString("title", curTitle);
							//menuBundle.putString("json", curStop.toString());
							menuBundle.putString("agency", agencyTag);
							SlideMenuItem newMenuItem = new SlideMenuItem(menuBundle);
							mAdapter.add(newMenuItem);
						} catch(JSONException e) {
							Log.e(TAG, "loadNearbyStops(): " + e.getMessage());
						}
					}
				}
				
			});
		}
		else {
			Log.e(TAG, "Could not get location provider, can't find nearby stops");
		}

	}
	
}
