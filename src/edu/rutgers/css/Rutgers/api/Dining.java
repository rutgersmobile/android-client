package edu.rutgers.css.Rutgers.api;

import java.util.ArrayList;

import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;

import android.util.Log;

/**
 * Helper for getting data from dining API
 *
 */
public class Dining {
	
	// timestamps: remove last 3 digits for unix timestamp
	private static boolean isSetup = false;
	private static Promise<Object, Object, Object> configured;
	private static final String TAG = "DiningAPI";
	
	private static JSONArray mNBDiningConf;
	
	private static final String API_URL = "https://rumobile.rutgers.edu/1/rutgers-dining.txt";
	
	/**
	 * Grab the dining API data. Should update on day change.
	 */
	private static void setup() {
		if(!isSetup) {
			// Get JSON array from dining API
			final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
			configured = confd.promise();
			
			final Promise<JSONArray, AjaxStatus, Double> promiseNBDining = Request.jsonArray(API_URL);
			
			DeferredManager dm = new DefaultDeferredManager();
			
			dm.when(promiseNBDining).done(new DoneCallback<JSONArray>() {

				@Override
				public void onDone(JSONArray res) {
					mNBDiningConf = (JSONArray) res;
					isSetup = true;
					confd.resolve(null);
					/*try {
						for(int i = 0; i < res.size(); i++) {
							OneResult r = res.get(i);
						
							if(r.getPromise() == promiseNBDining) mNBDiningConf = (JSONArray) r.getResult();
						}
					
						confd.resolve(null);
					}
					catch(Exception e) {
						Log.e(TAG, Log.getStackTraceString(e));
						confd.reject(e);
					}*/
				}
				
			}).fail(new FailCallback<AjaxStatus>() {
			
				@Override
				public void onFail(AjaxStatus e) {
					Log.e(TAG, e.getError() + "; " + e.getMessage() + "; Response code: " + e.getCode());
				}
				
			});	
		}
	}
	
	/**
	 * Get the JSON Object for a specific dining hall
	 * @param location Dining hall to get JSON object of
	 * @return JSON Object containing data for a dining hall.
	 */
	public static Promise<JSONObject, Exception, Double> getDiningLocation(final String location) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				JSONArray conf = mNBDiningConf;
				boolean resret = false; //if resolve or reject was already called
				
				// Find dining location in dining data
				for(int i = 0; i < conf.length(); i++) {
					JSONObject curLoc;
					try {
						curLoc = (JSONObject) conf.get(i);
						if(curLoc.getString("location_name").equalsIgnoreCase(location)) {
							resret = true;
							d.resolve(curLoc);
						}
					} catch (JSONException e) {
						Log.e(TAG, "Could not read location: " + e.getMessage());
						resret = true;
						d.reject(e);
					}
				}
				
				if(!resret) {
					Log.e(TAG, "Dining hall location \""+location+"\" not found in Dining API output");
					d.reject(new Exception("location not found"));
				}
			}
		});
		
		return d.promise();
	}
	
	/**
	 * Get meal genres array for a meal at a specific dining hall. The "genre" is the category of food, its array is a set of strings describing
	 * each food item in that category.
	 * @param diningLocation JSON Object of the dining hall to get meal genres from
	 * @param meal Name of the meal to get genres from
	 * @return JSON Array containing categories of food, each category containing a list of food items.
	 */
	public static JSONArray getMealGenres(JSONObject diningLocation, String meal) {
		if(diningLocation == null) {
			Log.e(TAG, "null dining location data");
			return null;
		}
		
		try {
			JSONArray meals = diningLocation.getJSONArray("meals");
			
			// Find meal in dining hall data
			for(int i = 0; i < meals.length(); i++) {
				JSONObject curMeal = (JSONObject) meals.get(i);
				
				// Found meal - check if marked available & if so, return genres array
				if(curMeal.getString("meal_name").equalsIgnoreCase(meal)) {
					if(!curMeal.getBoolean("meal_avail")) {
						return null;
					}
					
					return curMeal.getJSONArray("genres");
				}
			}
			
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		
		return null;
	}

}
