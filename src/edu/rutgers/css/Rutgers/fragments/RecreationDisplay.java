package edu.rutgers.css.Rutgers.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidquery.callback.AjaxStatus;

import edu.rutgers.css.Rutgers.api.Gyms;
import edu.rutgers.css.Rutgers2.R;

/**
 * Recreation display fragment displays gym information
 *
 */
public class RecreationDisplay extends Fragment {

	private static final String TAG = "RecreationDisplay";
	private static final DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy", Locale.US);

	private ViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private ArrayList<JSONObject> mDailyData;
	private ArrayList<String> mDayKeys;
	private JSONObject mDailyTemp;
	
	public RecreationDisplay() {
		// Required empty public constructor
	}	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDailyData = new ArrayList<JSONObject>();
		mDailyTemp = new JSONObject();
		mDayKeys = new ArrayList<String>();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		super.onCreateView(inflater, parent, savedInstanceState);
		final View v = inflater.inflate(R.layout.fragment_recreation_display, parent, false);

		// Make sure necessary arguments were given
		Bundle args = getArguments();
		if(args.getString("campus") == null || args.getString("location") == null) {
			Log.w(TAG, "Missing campus/location arg");
			// TODO Display data fail
			return v;
		}
		
		// Set up pager for hours displays
		mPager = (ViewPager) v.findViewById(R.id.hoursViewPager);
		mPagerAdapter = new HoursSlidePagerAdapter(getChildFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		
		// Get view IDs
		final TextView addressTextView = (TextView) v.findViewById(R.id.addressTextView);
		final TextView infoDeskNumberTextView = (TextView) v.findViewById(R.id.infoDeskNumberTextView);
		final TextView businessOfficeNumberTextView = (TextView) v.findViewById(R.id.businessOfficeNumberTextView);
		final TextView descriptionTextView = (TextView) v.findViewById(R.id.descriptionTextView);
		
		// Read gym location info and plug it in to the display
		final String location = args.getString("location");
		final String campus = args.getString("campus");	
		Gyms.getGyms().done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject gymsJson) {
				try {
					// Get location JSON
					JSONObject locationJson = gymsJson.getJSONObject(campus).getJSONObject(location);
					
					// Fill in location info
					addressTextView.setText(locationJson.optString("FacilityAddress"));
					infoDeskNumberTextView.setText(locationJson.optString("FacilityInformation"));
					businessOfficeNumberTextView.setText(locationJson.optString("FacilityBusiness"));
					descriptionTextView.setText(StringEscapeUtils.unescapeHtml4(locationJson.optString("FacilityBody")));
					
					// Get hours data for sub-locations & create fragments
					JSONObject areaHours = locationJson.getJSONObject("meetingareas");
					Iterator<String> areaKeys = areaHours.keys();
					while(areaKeys.hasNext()) {
						String curAreaKey = areaKeys.next();
						JSONObject curAreaData = areaHours.getJSONObject(curAreaKey);
						addLocationHours(curAreaKey, curAreaData);
					}
					
					sortDayKeys();
					for(String key: mDayKeys) {
						Log.v(TAG, key);
					}
					
				} catch (JSONException e) {
					Log.w(TAG, "onCreateView(): " + e.getMessage());
				}
				
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus status) {
				Log.w(TAG, status.getMessage());
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.UI;
			}
			
		});

		return v;
	}
	
	private class HoursSlidePagerAdapter extends FragmentStatePagerAdapter {
		
		public HoursSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return 0;
		}
		
	}
	
	/**
	 * The point of this function is to convert the given JSON data into a set that is organized by date
	 * rather than sub-location name, because we will be displaying the hours by DAY, not by location.
	 * 
	 * The resulting JSON object (mDailyTemp) looks something like this:
	 * 
	 * {
	 *		"5/17/2014":{
     * 			"Multisports Bay 1 (Badminton)":"CLOSED",
     *			"Multisports Bay 4 (Basketball)":"CLOSED",
     * 			...
   	 * 		},
   	 * 		"6\/5\/2014":{
     *			"Multisports Bay 1 (Badminton)":"7:00AM - 9:00PM",
     *			"Multisports Bay 4 (Basketball)":"7:00AM - 9:00PM",
     *			...
     *	 	},
     *		...
     * }
     * 
     * Note that the keys are NOT SORTED. The keys are stored in a separate list and sorted
     * with {@link #sortDayKeys()}.
	 * 
	 * @param curAreaKey Sub-location name
	 * @param curAreaData Daily hours for sub-location
	 */
	private void addLocationHours(String curAreaKey, JSONObject curAreaData) {
		/* 
		 * If this is the first piece of JSON being processed, add the dates to the list of keys.
		 * This only needs to happen once, because the same dates are listed for every sub-location.
		 */
		boolean addKeys = mDayKeys.size() == 0 ? true : false; 
		
		Iterator<String> dayIter = curAreaData.keys();
		while(dayIter.hasNext()) {
			String curDayKey = dayIter.next();
			
			if(addKeys) mDayKeys.add(curDayKey);
			
			try {
				// Get hours for location on this day
				String hours = curAreaData.getString(curDayKey);
			
				// Try to find date in table
				JSONObject findDate = mDailyTemp.optJSONObject(curDayKey);
				
				// Not found - make a new one
				if(findDate == null) {
					JSONObject newDay = new JSONObject();
					
					// Then add location:hours mapping for date
					newDay.put(curAreaKey, hours);
					
					// Add new date object to table
					mDailyTemp.put(curDayKey, newDay);
				}
				// Found - tack on new location:hours entry
				else {
					findDate.put(curAreaKey, hours);
				}
				
			} catch(JSONException e) {
				Log.w(TAG, "addLocationHours(): " + e.getMessage());
			}
			
		}
	}
	
	/**
	 * Sort the day keys by date order.
	 */
	private void sortDayKeys() {
		Collections.sort(mDayKeys, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				try {
					Date lhd = dateFormat.parse(lhs);
					Date rhd = dateFormat.parse(rhs);
					return lhd.compareTo(rhd);
				} catch(ParseException e) {
					throw new IllegalArgumentException(e.getMessage());
				}
			}
			
		});
	}
	
}
