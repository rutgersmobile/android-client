package edu.rutgers.css.Rutgers.api;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredObject;
import org.jdeferred.android.AndroidExecutionScope;
import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Gyms {
	
	private static final String TAG = "Gyms";
	
	public static final DateFormat GYM_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy", Locale.US);
	
	private static final long expire = Request.CACHE_ONE_DAY; // Cache gym info for a day

    /**
     * Get the Gyms API.
     * @return Gyms API JSON object
     */
	public static Promise<JSONArray, AjaxStatus, Double> getGyms() {
		final Deferred<JSONArray, AjaxStatus, Double> d = new AndroidDeferredObject<JSONArray, AjaxStatus, Double>(Request.jsonArray("http://sauron.rutgers.edu/~jamchamb/new_gyms.json", expire), AndroidExecutionScope.BACKGROUND);
		return d.promise();
	}
	
}
