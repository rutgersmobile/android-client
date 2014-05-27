package edu.rutgers.css.Rutgers.api;

import java.util.Hashtable;

import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

/**
 * Helper for getting data from places API
 *
 */
public class Places {
	
	private static Promise<Object, Object, Object> configured;
	private static final String TAG = "PlacesAPI";
	
	private static JSONObject mPlacesConf;
	private static Hashtable<String, JSONObject> mPlacesHashtable;
	
	private static final String API_URL = "https://rumobile.rutgers.edu/1/places.txt";	
	private static long expire = 1000 * 60 * 60; // Cache data for an hour
	
	/**
	 * Grab the places API data.
	 */
	private static void setup() {
		// Get JSON object from places API
		final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
		configured = confd.promise();
		
		final Promise<JSONObject, AjaxStatus, Double> promisePlaces = Request.json(API_URL, expire);
		
		DeferredManager dm = new DefaultDeferredManager();		
		dm.when(promisePlaces).done(new DoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject res) {
				
				/* Build hashtable if result is new */
				if(!res.equals(mPlacesConf)) {
					Log.i(TAG, "Building places hashtable");
				}
				
				mPlacesConf = (JSONObject) res;
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
	 * Get the JSONObject containing all of the place information
	 * @return JSONObject containing "all" field from Places API
	 */
	public static Promise<JSONObject, Exception, Double> getPlaces() {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			
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
			
		});
		
		return d.promise();
	}
	
	public static Promise<JSONObject, Exception, Double> getPlace(final String placeKey) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			
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
			
		});
		
		return d.promise();
	}

}
