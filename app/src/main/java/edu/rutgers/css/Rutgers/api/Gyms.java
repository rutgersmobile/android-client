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

    /**
     * Get array of hours for sub-locations by date.
     * @param locationJson Gym JSON from API
     * @return JSON array of dates with hours by sub-location
     * @throws JSONException
     */
	public static JSONArray getGymHours(JSONObject locationJson) throws JSONException {
        return new JSONArray();
	}
	
}
