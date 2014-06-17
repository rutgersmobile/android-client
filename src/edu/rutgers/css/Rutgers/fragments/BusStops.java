package edu.rutgers.css.Rutgers.fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
	private static final int REFRESH_INTERVAL = 60; // nearby stop refresh interval in seconds
	
	private ListView mListView;
	private RMenuAdapter mAdapter;
	private ArrayList<RMenuPart> mData;
	private LocationClientProvider mLocationClientProvider;
	private int mNearbyStopCount; // Keep track of number of nearby stops displayed
	private Timer mUpdateTimer;
	private Handler mUpdateHandler;
	
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
		
		mNearbyStopCount = 0;
		mData = new ArrayList<RMenuPart>();
		mAdapter = new RMenuAdapter(getActivity(), R.layout.title_row, R.layout.basic_section_header, mData);
		
		// Setup the timer stuff for updating the nearby stops
		mUpdateTimer = new Timer();
		mUpdateHandler = new Handler();
		
		// Add "nearby stops" header
		mAdapter.add(new SlideMenuHeader(getActivity().getResources().getString(R.string.bus_nearby_active_stops_header)));
		
		loadAgency("nb", getResources().getString(R.string.bus_nb_active_stops_header));
		loadAgency("nwk", getResources().getString(R.string.bus_nwk_active_stops_header));
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_busstops, parent, false);
		
		mListView = (ListView) v.findViewById(R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

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
				
				ComponentFactory.getInstance().switchFragments(clickedArgs);
			}
			
		});
		
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Start the update thread when screen is active
		mUpdateTimer = new Timer();
		mUpdateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mUpdateHandler.post(new Runnable() {
					@Override
					public void run() {
						loadNearbyStops("nb");
					}
				});
			}
		}, 0, 1000 * REFRESH_INTERVAL);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		// Stop the update thread from running when screen isn't active
		if(mUpdateTimer == null) return;
		
		mUpdateTimer.cancel();
		mUpdateTimer = null;
	}
	
	/**
	 * Populate list with bus stops for agency, with a section header for that agency.
	 * Resulting JSON array looks like this:
	 * 	[{"geoHash":"dr5rb1qnk35gxur","title":"1 Washington Park"},{"geoHash":"dr5pzbvwhvpjst4","title":"225 Warren St"}]
	 * @param agencyTag Agency tag for API request
	 * @param agencyTitle Header title that goes above these stops
	 */
	private void loadAgency(final String agencyTag, final String agencyTitle) {
		Nextbus.getActiveStops(agencyTag).then(new AndroidDoneCallback<JSONArray>() {
			
			@Override
			public void onDone(JSONArray data) {
				//Log.d(TAG, "loadAgency(): " + data.toString());
				
				mAdapter.add(new SlideMenuHeader(agencyTitle));
				
				if(data.length() == 0) {
					mAdapter.add(new SlideMenuItem(getActivity().getResources().getString(R.string.bus_no_active_stops)));
					return;
				}
				
				// Create an item in the list for each stop from the array
				for(int i = 0; i < data.length(); i++) {
					try {
						JSONObject jsonObj = data.getJSONObject(i);
						Bundle menuBundle = new Bundle();
						menuBundle.putString("title", jsonObj.getString("title"));
						menuBundle.putString("agency", agencyTag);
						SlideMenuItem newMenuItem = new SlideMenuItem(menuBundle);
						mAdapter.add(newMenuItem);
					} catch (JSONException e) {
						Log.e(TAG, "loadAgency(): " + e.getMessage());
					}
				}
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});
	}
	
	/**
	 * Remove rows related to nearby stops
	 */
	private void clearNearbyRows() {
		for(int i = 0; i < mNearbyStopCount; i++) {
			mData.remove(1);
			mAdapter.notifyDataSetChanged();
		}
		mNearbyStopCount = 0;
	}
	
	private void addNearbyRow(int pos, RMenuPart row) {
		mData.add(pos, row);
		mNearbyStopCount++;
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * Populate list with active nearby stops for an agency
	 * @param agencyTag Agency tag for API request
	 */
	private void loadNearbyStops(final String agencyTag) {
		
		// Check for location services
		if(mLocationClientProvider != null && mLocationClientProvider.servicesConnected() && mLocationClientProvider.getLocationClient().isConnected()) {
			// Get last location
			Location lastLoc = mLocationClientProvider.getLocationClient().getLastLocation();
			if(lastLoc == null) {
				Log.e(TAG, "Could not get location");
				clearNearbyRows();
				addNearbyRow(1, new SlideMenuItem(getActivity().getResources().getString(R.string.failed_location)));
				return;
			}
			Log.d(TAG, "Current location: " + lastLoc.toString());
			
			// Look up nearby active bus stops
			Nextbus.getActiveStopsByTitleNear(agencyTag, 
					(float) lastLoc.getLatitude(), 
					(float) lastLoc.getLongitude()
					).then(new AndroidDoneCallback<JSONObject>() {

				@Override
				public void onDone(JSONObject activeNearbyStops) {				
					// Clear previous rows
					clearNearbyRows();
					
					// If there aren't any results, put a "no stops nearby" message
					if(activeNearbyStops.length() == 0) {
						addNearbyRow(1, new SlideMenuItem(getActivity().getResources().getString(R.string.bus_no_nearby_stops)));
					}
					// If there are new results, add them
					else {
						int j = 1;
						Iterator<String> stopTitleIter = activeNearbyStops.keys();
						while(stopTitleIter.hasNext()) {
							try {
								String curTitle = stopTitleIter.next();
								JSONObject curStop = activeNearbyStops.getJSONObject(curTitle);
															
								Bundle menuBundle = new Bundle();
								menuBundle.putString("title", curTitle);
								menuBundle.putString("agency", agencyTag);
								SlideMenuItem newMenuItem = new SlideMenuItem(menuBundle);
								
								addNearbyRow(j++, newMenuItem);
							} catch(JSONException e) {
								Log.e(TAG, "loadNearbyStops(): " + e.getMessage());
							}
						}
					}
				}
				
				@Override
				public AndroidExecutionScope getExecutionScope() {
					return AndroidExecutionScope.UI;
				}
				
			});
		}
		else {
			Log.e(TAG, "Could not get location provider, can't find nearby stops");
		}

	}
	
}
