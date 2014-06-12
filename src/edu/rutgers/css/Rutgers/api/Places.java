package edu.rutgers.css.Rutgers.api;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidDoneCallback;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.AndroidFailCallback;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

/**
 * Helper for getting data from places API.
 * 
 */
public class Places {
	
	private static final String TAG = "PlacesAPI";
	private static final String API_URL = "https://rumobile.rutgers.edu/1/places.txt";	
	private static final long expire = Request.EXPIRE_ONE_HOUR; // Cache data for an hour
	
	private static Promise<Object, Object, Object> configured;
	private static JSONObject mPlacesConf;
	
	/**
	 * Grab the places API data.
	 */
	private static void setup() {
		// Get JSON object from places API
		final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
		configured = confd.promise();
		
		final Promise<JSONObject, AjaxStatus, Double> promisePlaces = Request.json(API_URL, expire);
		
		AndroidDeferredManager dm = new AndroidDeferredManager();		
		dm.when(promisePlaces).done(new AndroidDoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject res) {
				
				mPlacesConf = (JSONObject) res;
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
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
			}
			
		});	
	}
	
	/**
	 * Get the JSON containing all of the place information
	 * @return JSONObject containing "all" field from Places API
	 */
	public static Promise<JSONObject, Exception, Double> getPlaces() {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new AndroidDoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
				JSONObject conf = mPlacesConf;
				
				try {
					JSONObject allPlaces = conf.getJSONObject("all");
					d.resolve(allPlaces);
				}
				catch(JSONException e) {
					Log.e(TAG, Log.getStackTraceString(e));
					d.fail(null);
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
	 * Get JSON for a specific place.
	 * @param placeKey Place key (NOT title)
	 * @return JSON for place
	 */
	public static Promise<JSONObject, Exception, Double> getPlace(final String placeKey) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new AndroidDoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
				JSONObject conf = mPlacesConf;
				
				try {
					
					JSONObject allPlaces = conf.getJSONObject("all");	
					JSONObject place = allPlaces.getJSONObject(placeKey);
					if(place != null) d.resolve(place);
					else {
						Log.i(TAG, "Failed to get location " + placeKey);
						d.fail(null);
					}
					
				} catch (JSONException e) {
					
					Log.e(TAG, Log.getStackTraceString(e));
				
				}
				
			}
			
			@Override
			public AndroidExecutionScope getExecutionScope() {
				return AndroidExecutionScope.BACKGROUND;
			}
			
		});
		
		return d.promise();
	}

}
