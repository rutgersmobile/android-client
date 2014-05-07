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
 * Helper for getting data from places API
 *
 */
public class Places {
	
	private static Promise<Object, Object, Object> configured;
	private static final String TAG = "DiningAPI";
	
	private static JSONObject mNBPlacesConf;
	
	private static final String API_URL = "https://rumobile.rutgers.edu/1/places.txt";	
	private static long expire = 1000 * 60 * 60; // Cache data for an hour
	
	/**
	 * Grab the places API data.
	 * (Current API only has New Brunswick data; when multiple confs need to be read set this up like Nextbus.java)
	 */
	private static void setup() {
		// Get JSON object from places API
		final Deferred<Object, Object, Object> confd = new DeferredObject<Object, Object, Object>();
		configured = confd.promise();
		
		final Promise<JSONObject, AjaxStatus, Double> promiseNBDining = Request.json(API_URL, expire);
		
		DeferredManager dm = new DefaultDeferredManager();		
		dm.when(promiseNBDining).done(new DoneCallback<JSONObject>() {

			@Override
			public void onDone(JSONObject res) {
				mNBPlacesConf = (JSONObject) res;
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
	 * Get the JSONObject containing all of the Places API data
	 * @return JSONObject that contains all Places API data
	 */
	public static Promise<JSONObject, Exception, Double> getPlaces() {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			public void onDone(Object o) {
				JSONObject conf = mNBPlacesConf;
				d.resolve(conf);
			}
		});
		
		return d.promise();
	}

}
