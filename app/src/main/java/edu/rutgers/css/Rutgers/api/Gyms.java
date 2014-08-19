package edu.rutgers.css.Rutgers.api;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import edu.rutgers.css.Rutgers.utils.AppUtil;

public class Gyms {
	
	private static final String TAG = "Gyms";
	private static final String API_URL = AppUtil.API_BASE + "gyms.txt";
	
	public static final DateFormat GYM_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy", Locale.US);
	
	private static final long expire = Request.CACHE_ONE_DAY; // Cache gym info for a day

    /**
     * Get the Gyms API.
     * @return Gyms API JSON object
     */
	public static Promise<JSONObject, AjaxStatus, Double> getGyms() {
		final Deferred<JSONObject, AjaxStatus, Double> d = new AndroidDeferredObject<JSONObject, AjaxStatus, Double>(Request.json(API_URL, expire), AndroidExecutionScope.BACKGROUND);
		return d.promise();
	}

    /**
     * Get array of hours for sub-locations by date.
     * @param locationJson Gym JSON from API
     * @return JSON array of dates with hours by sub-location
     * @throws JSONException
     */
	public static JSONArray getGymHours(JSONObject locationJson) throws JSONException {
		ArrayList<String> dayKeys = new ArrayList<String>();
		JSONObject dailyData = new JSONObject();
		JSONArray result = new JSONArray();
		JSONObject areaHours = locationJson.getJSONObject("meetingareas");
		
		Iterator<String> areaKeys = areaHours.keys();
		while(areaKeys.hasNext()) {
			String curAreaKey = areaKeys.next();
			JSONObject curAreaData = areaHours.getJSONObject(curAreaKey);
			addLocationHours(dayKeys, dailyData, curAreaKey, curAreaData);
		}
			
		sortDayKeys(dayKeys);
		
		// Add objects for each key in order to the resulting array
		for(int i = 0; i < dayKeys.size(); i++) {
			JSONObject arrayEntry = new JSONObject();
			arrayEntry.put("date", dayKeys.get(i));
			arrayEntry.put("hours", dailyData.getJSONObject(dayKeys.get(i)));
			result.put(arrayEntry);
		}
			
		return result;
	}
	
	/**
	 * The point of this function is to convert the given JSON data into a set that is organized by date
	 * rather than sub-location name, because we will be displaying the hours by DAY, not by sub-location.
	 * 
	 * The resulting JSON object (mDailyTemp) looks something like this:
	 * 
	 * {
	 *		"5/17/2014":{
	 * 			"Multisports Bay 1 (Badminton)":"CLOSED",
	 *			"Multisports Bay 4 (Basketball)":"CLOSED",
	 * 			...
	 * 		},
	 * 		"6/5/2014":{
	 *			"Multisports Bay 1 (Badminton)":"7:00AM - 9:00PM",
	 *			"Multisports Bay 4 (Basketball)":"7:00AM - 9:00PM",
	 *			...
	 *	 	},
	 *		...
	 * }
	 * 
	 * Note that JSON object keys are NOT SORTED. The keys are stored in a separate list and sorted
	 * with {@link #sortDayKeys(ArrayList<String>)}. The sub-location names are NOT SORTED.
	 * 
	 * @param dayKeys List of strings to hold the sorted date keys
	 * @param dailyData JSON object to hold the resulting JSON
	 * @param curAreaKey Sub-location name
	 * @param curAreaData Daily hours for sub-location
	 */
	private static void addLocationHours(ArrayList<String> dayKeys, JSONObject dailyData, String curAreaKey, JSONObject curAreaData) {
		/* 
		 * If this is the first piece of JSON being processed, add the dates to the list of keys.
		 * This only needs to happen once, because the same dates are listed for every sub-location.
		 */
		boolean addKeys = dayKeys.size() == 0 ? true : false; 
		
		Iterator<String> dayIter = curAreaData.keys();
		while(dayIter.hasNext()) {
			String curDayKey = dayIter.next();
			
			if(addKeys) dayKeys.add(curDayKey);
			
			try {
				// Get hours for location on this day
				String hours = curAreaData.getString(curDayKey);
			
				// Try to find date in table
				JSONObject findDate = dailyData.optJSONObject(curDayKey);
				
				// Not found - make a new one
				if(findDate == null) {
					JSONObject newDay = new JSONObject();
					
					// Then add location:hours mapping for date
					newDay.put(curAreaKey, hours);
					
					// Add new date object to table
					dailyData.put(curDayKey, newDay);
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
	private static void sortDayKeys(ArrayList<String> dayKeys) {
		Collections.sort(dayKeys, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				try {
					Date lhd = GYM_DATE_FORMAT.parse(lhs);
					Date rhd = GYM_DATE_FORMAT.parse(rhs);
					return lhd.compareTo(rhd);
				} catch(ParseException e) {
					throw new IllegalArgumentException(e.getMessage());
				}
			}
			
		});
	}
	
}
