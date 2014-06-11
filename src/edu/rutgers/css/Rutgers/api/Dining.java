package edu.rutgers.css.Rutgers.api;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
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
	
	private static final String API_URL = "https://rumobile.rutgers.edu/1/rutgers-dining.txt";	
	private static long expire = Request.EXPIRE_ONE_HOUR; // Cache dining data for an hour
	
	/**
	 * Grab the dining API data.
	 * (Current API only has New Brunswick data; when multiple confs need to be read set this up like Nextbus.java)
	 */
	private static void setup() {
		// Get JSON array from dining API
		final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
		configured = confd.promise();
		
		final Promise<JSONArray, AjaxStatus, Double> promiseNBDining = Request.jsonArray(API_URL, expire);
		
		AndroidDeferredManager dm = new AndroidDeferredManager();		
		dm.when(promiseNBDining).done(new AndroidDoneCallback<JSONArray>() {

			@Override
			public void onDone(JSONArray res) {
				mNBDiningConf = (JSONArray) res;
				confd.resolve(null);
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
			}
			
		}).fail(new AndroidFailCallback<AjaxStatus>() {
		
			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, e.getMessage() + "; Response code: " + e.getCode());
				confd.reject(e);
			}

			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
			}
			
		});	
	}
	
	/**
	 * Get the JSONArray containing all of the dining hall JSON objects
	 * @return JSONArray of dining hall JSONObjects
	 */
	public static Promise<JSONArray, AjaxStatus, Double> getDiningHalls() {
		final Deferred<JSONArray, AjaxStatus, Double> d = new DeferredObject<JSONArray, AjaxStatus, Double>();
		setup();
		
		configured.then(new AndroidDoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
				JSONArray conf = mNBDiningConf;
				d.resolve(conf);
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
			}
			
		}).fail(new AndroidFailCallback<Object>() {

			@Override
			public void onFail(Object e) {
				d.reject((AjaxStatus)e);
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
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
		
		configured.then(new AndroidDoneCallback<Object>() {
			
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
						Log.e(TAG, e.getMessage());
						resret = true;
						d.reject(e);
					}
				}
				
				if(!resret) {
					Log.e(TAG, "Dining hall location \""+location+"\" not found API");
					d.reject(new IllegalArgumentException("Invalid dining location \"" + location +"\""));
				}
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
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
	
	/**
	 * Check whether a dining hall has any meals available.
	 * Dining halls without any active meals shouldn't be displayed in the food menu.
	 * @param diningHall Dining hall JSON data
	 * @return True if there are any meals active, false if not.
	 */
	public static boolean hasActiveMeals(JSONObject diningHall) {
		if(!diningHall.has("meals")) return false;
		else {
			try {
				JSONArray meals = diningHall.getJSONArray("meals");
				for(int i = 0; i < meals.length(); i++) {
					JSONObject curMeal = meals.getJSONObject(i);
					if(curMeal.getBoolean("meal_avail") == true) {
						return true;
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
			
			return false;
		}
	}

}
