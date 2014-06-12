package edu.rutgers.css.Rutgers.api;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONObject;

import com.androidquery.callback.AjaxStatus;

public class Gyms {
	
	private static final String TAG = "Gyms";
	private static final String API_URL = "https://rumobile.rutgers.edu/1/gyms.txt";
	
	private static final long expire = 1000 * 60 * 60 * 24; // Cache gym info for a day

	public static Promise<JSONObject, AjaxStatus, Double> getGyms() {
		final Deferred<JSONObject, AjaxStatus, Double> d = new AndroidDeferredObject<JSONObject, AjaxStatus, Double>(Request.json(API_URL, expire), AndroidExecutionScope.BACKGROUND);
		return d.promise();
	}
	
}
