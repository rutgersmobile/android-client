package edu.rutgers.css.Rutgers.api;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.items.KeyValPair;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Helper for getting data from places API.
 */
public class Places {
	
	private static final String TAG = "PlacesAPI";

    private static final String API_URL = "http://sauron.rutgers.edu/pq";
    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();

	/**
	 * Get JSON for a specific place.
	 * @param placeKey Place key
	 * @return JSON for place
	 */
	public static Promise<JSONObject, Exception, Double> getPlace(final String placeKey) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();

        String parameter;
        try {
            parameter = URLEncoder.encode(placeKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            d.reject(e);
            return d.promise();
        }

        sDM.when(Request.json(API_URL+"?id="+parameter, Request.CACHE_ONE_DAY * 7)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                try {
                    JSONObject data = result.getJSONObject("data");

                    if (result.getString("status").equals("success")) {
                        d.resolve(data.getJSONObject("place"));
                    } else {
                        d.reject(new Exception(data.getString("message")));
                    }
                } catch (JSONException e) {
                    d.reject(e);
                }
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
    public static Promise<List<KeyValPair>, Exception, Double> getPlacesNear(final double sourceLat, final double sourceLon) {
        final Deferred<List<KeyValPair>, Exception, Double> deferred = new DeferredObject<List<KeyValPair>, Exception, Double>();

        sDM.when(Request.json(API_URL+"?latitude="+sourceLat+"&longitude="+sourceLon, Request.CACHE_ONE_MINUTE)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                try {
                    JSONObject data = result.getJSONObject("data");

                    if(result.getString("status").equals("success")) {
                        JSONArray places = data.getJSONArray("places");

                        List<KeyValPair> stubs = new ArrayList<KeyValPair>();
                        for (int i = 0; i < places.length(); i++) {
                            try {
                                JSONObject place = places.getJSONObject(i);
                                stubs.add(new KeyValPair(place.getString("id"), place.getString("title")));
                            } catch (JSONException e) {
                                Log.w(TAG, "getPlacesNear(): " + e.getMessage());
                            }
                        }

                        deferred.resolve(stubs);
                    } else {
                        deferred.reject(new Exception(data.getString("message")));
                    }
                } catch(JSONException e) {
                    deferred.reject(e);
                }
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
    public static Promise<List<KeyValPair>, Exception, Double> searchPlaces(final String query) {
        final Deferred<List<KeyValPair>, Exception, Double> deferred = new DeferredObject<List<KeyValPair>, Exception, Double>();

        sDM.when(Request.json(API_URL+"?search="+query, Request.CACHE_ONE_MINUTE)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                try {
                    JSONObject data = result.getJSONObject("data");

                    if (result.getString("status").equals("success")) {
                        JSONArray places = data.getJSONArray("places");
                        List<KeyValPair> stubs = new ArrayList<KeyValPair>();

                        for (int i = 0; i < places.length(); i++) {
                            JSONObject place = places.getJSONObject(i);
                            stubs.add(new KeyValPair(place.getString("id"), place.getString("title")));
                        }

                        deferred.resolve(stubs);
                    } else {
                        deferred.reject(new Exception(data.getString("message")));
                    }
                } catch (JSONException e) {
                    deferred.reject(e);
                }
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
