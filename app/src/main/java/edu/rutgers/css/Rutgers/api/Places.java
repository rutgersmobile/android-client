package edu.rutgers.css.Rutgers.api;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.rutgers.css.Rutgers.items.PlaceStub;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Helper for getting data from places API.
 */
public class Places {
	
	private static final String TAG = "PlacesAPI";

    private static final String API_URL = "http://sauron.rutgers.edu:8080/";
    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();

	/**
	 * Get JSON for a specific place.
	 * @param placeKey Place key
	 * @return JSON for place
	 */
	public static Promise<JSONObject, Exception, Double> getPlace(final String placeKey) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();

        sDM.when(Request.json(API_URL+"?id="+placeKey, Request.CACHE_ONE_DAY)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                d.resolve(result);
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                d.reject(new Exception(AppUtil.formatAjaxStatus(result)));
            }
        });

		return d.promise();
	}

    /**
     * Get places near a given location.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for a list of place keys & JSON objects.
     */
    public static Promise<List<PlaceStub>, Exception, Double> getPlacesNear(final double sourceLat, final double sourceLon) {
        final Deferred<List<PlaceStub>, Exception, Double> deferred = new DeferredObject<List<PlaceStub>, Exception, Double>();

        sDM.when(Request.jsonArray(API_URL+"?latitude="+sourceLat+"&longitude="+sourceLon, Request.CACHE_ONE_HOUR)).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                List<PlaceStub> stubs = new ArrayList<PlaceStub>();
                for (int i = 0; i < result.length(); i++) {
                    try {
                        JSONObject place = result.getJSONObject(i);
                        PlaceStub newStub = new PlaceStub(place.getString("id"), place.getString("title"));
                        stubs.add(newStub);
                    } catch (JSONException e) {
                        Log.w(TAG, "getPlacesNear(): " + e.getMessage());
                    }
                }

                deferred.resolve(stubs);
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(result)));
            }
        });

        return deferred.promise();
    }

    /**
     * Search places by title or building code.
     * @param query Query
     * @return JSON array of results.
     */
    public static Promise<JSONArray, Exception, Double> searchPlaces(final String query) {
        final Deferred<JSONArray, Exception, Double> deferred = new DeferredObject<JSONArray, Exception, Double>();

        sDM.when(Request.jsonArray(API_URL+"?search="+query, Request.CACHE_ONE_MINUTE)).done(new DoneCallback<JSONArray>() {
            @Override
            public void onDone(JSONArray result) {
                deferred.resolve(result);
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(result)));
            }
        });

        return deferred.promise();
    }

}
