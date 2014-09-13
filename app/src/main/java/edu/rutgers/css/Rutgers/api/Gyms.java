package edu.rutgers.css.Rutgers.api;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		return Request.jsonArray("http://sauron.rutgers.edu/~jamchamb/new_gyms.txt", expire);
	}

    /**
     * Get facility information.
     * @param campusTitle Campus title
     * @param facilityTitle Facility title
     * @return Promise for a facility JSON object, which resolves as null if no facility of the given title can be found.
     */
    public static Promise<JSONObject, AjaxStatus, Double> getFacility(final String campusTitle, final String facilityTitle) {
        final Deferred<JSONObject, AjaxStatus, Double> d = new DeferredObject<JSONObject, AjaxStatus, Double>();

        AndroidDeferredManager dm = new AndroidDeferredManager();
        dm.when(getGyms(), AndroidExecutionScope.BACKGROUND).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                try {
                    // Loop to find campus
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject campus = result.getJSONObject(i);
                        if (campus.getString("title").equalsIgnoreCase(campusTitle)) {

                            // Found campus; loop to find facility
                            JSONArray facilities = campus.getJSONArray("facilities");
                            for (int j = 0; j < facilities.length(); j++) {
                                JSONObject facility = facilities.getJSONObject(j);
                                if (facility.getString("title").equalsIgnoreCase(facilityTitle)) {
                                    d.resolve(facility);
                                    return;
                                }
                            }

                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG, "getFacility(): " + e.getMessage());
                }
                d.resolve(null);
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus status) {
                d.reject(status);
            }
        });

        return d.promise();
    }

}
