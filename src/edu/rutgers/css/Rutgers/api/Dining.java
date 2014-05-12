package edu.rutgers.css.Rutgers.api;

import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

/**
 * Helper for getting data from dining API
 *
 */
public class Dining {
	
	private static Promise<Object, Object, Object> configured;
	private static final String TAG = "DiningAPI";
	
	private static JSONArray mNBDiningConf;
	
	private static final String API_URL = "http://vps.rsopher.com/nutrition.json";	//using my own vps for now
	private static long expire = 1000 * 60 * 60; // Cache dining data for an hour
	
	/**
	 * Grab the dining API data.
	 * (Current API only has New Brunswick data; when multiple confs need to be read set this up like Nextbus.java)
	 */
	private static void setup() {
		// Get JSON array from dining API
		final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
		configured = confd.promise();
		
		final Promise<JSONArray, AjaxStatus, Double> promiseNBDining = Request.jsonArray(API_URL, expire);
		
		DeferredManager dm = new DefaultDeferredManager();		
		dm.when(promiseNBDining).done(new DoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray res) {
				mNBDiningConf = (JSONArray) res;
				confd.resolve(null);
			}
			
		}).fail(new FailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, e.getMessage() + "; Response code: " + e.getCode());
			}
			
		});	
	}
	
	/**
	 * Get the JSONArray containing all of the dining hall JSON objects
	 * @return JSONArray of dining hall JSONObjects
	 */
	public static Promise<JSONArray, Exception, Double> getDiningHalls() {
		final Deferred<JSONArray, Exception, Double> d = new DeferredObject<JSONArray, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
				JSONArray conf = mNBDiningConf;
				d.resolve(conf);
			}
			
		});
		
		return d.promise();
	}
	
	/**
	 * Get the JSON Object for a specific dining hall
	 * @param location Dining hall to get JSON object of
	 * @return Promise containing the JSONObject data for a dining hall.
	 */
	public static Promise<JSONObject, Exception, Double> getDiningLocation(final String location) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			
			@Override
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
				
				// Found meal - check if available & if so, return genres array
				if(curMeal.getString("meal_name").equalsIgnoreCase(meal)) {
					if(curMeal.getJSONArray("genres").length()==0) {
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
