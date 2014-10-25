package edu.rutgers.css.Rutgers.api;

import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
import edu.rutgers.css.Rutgers.items.Place;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Provides access to the Places database.
 * @author James Chambers
 */
public class Places {
    
    private static final String TAG = "PlacesAPI";

    private static final String API_URL = "https://oss-services.rutgers.edu/pq";
    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();

    /**
     * Get a specific place from the Places API.
     * @param placeKey Key for place entry, returned from search results
     * @return Promise for a Place object representing the entry in the database
     */
    public static Promise<Place, Exception, Double> getPlace(final String placeKey) {
        final Deferred<Place, Exception, Double> d = new DeferredObject<Place, Exception, Double>();

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
                        try {
                            Gson gson = new Gson();
                            Place place = gson.fromJson(data.getJSONObject("place").toString(), Place.class);
                            d.resolve(place);
                        } catch (JsonSyntaxException e) {
                            d.reject(e);
                        }
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
     * Search for places near a given location.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
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
     * <p>Search places by title or building code.</p>
     *
     * <p>Server-side, this means the query is processed by the lunr index. Should more fields
     * be added to the index (such as the full place description), then it will also be possible
     * to search based on the contents of those fields.</p>
     * @param query Query string
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
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
