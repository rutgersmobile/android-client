package edu.rutgers.css.Rutgers.api;

import android.location.Location;
import android.util.Log;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.DeferredAsyncTask;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.rutgers.css.Rutgers.items.PlaceStub;
import edu.rutgers.css.Rutgers.utils.AppUtil;

/**
 * Helper for getting data from places API.
 */
public class Places {
	
	private static final String TAG = "PlacesAPI";

	private static Promise<Object, AjaxStatus, Object> configured;
	private static JSONObject sPlaceConf;
    private static final AndroidDeferredManager sDeferredManager = new AndroidDeferredManager();
    private static boolean sSetupLocked;
    private static Map<String, JSONObject> sPlacesTable;

    /**
	 * Grab the places API data.
	 */
	private static synchronized void setup() {

        // Prevent multiple calls coming in and spawning many different requests for the places JSON,
        // which is about 2-3 MB and often results in heap growth.
        if(sSetupLocked) return;
        else sSetupLocked = true;

		// Get places JSON from server
        final Deferred<Object, AjaxStatus, Object> confd = new DeferredObject<Object, AjaxStatus, Object>();
		configured = confd.promise();

        final Promise<AjaxCallback<JSONObject>, AjaxStatus, Double> promisePlaces = Request.apiWithStatus("places.txt", Request.CACHE_ONE_DAY * 7);
        promisePlaces.done(new DoneCallback<AjaxCallback<JSONObject>>() {

            @Override
            public void onDone(AjaxCallback<JSONObject> res) {
                // If the result came from cache, skip new setup
                if (sPlaceConf != null && res.getStatus().getSource() != AjaxStatus.NETWORK) {
                    Log.v(TAG, "Retaining cached place data");
                    confd.resolve(null);
                    return;
                }
                else {
                    Log.v(TAG, "Generating new place data from network");
                }

                try {
                    JSONObject allPlaces = res.getResult().getJSONObject("all");
                    sPlaceConf = allPlaces;
                    sPlacesTable = Collections.synchronizedMap(new LinkedHashMap<String, JSONObject>());

                    Iterator<String> placeIter = allPlaces.keys();
                    while(placeIter.hasNext()) {
                        String curKey = placeIter.next();
                        JSONObject curPlace = allPlaces.getJSONObject(curKey);
                        sPlacesTable.put(curKey, curPlace);
                    }

                    confd.resolve(null);
                } catch (JSONException e) {
                    Log.e(TAG, "setup(): " + e.getMessage());
                    confd.reject(res.getStatus());
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

			@Override
			public void onFail(AjaxStatus e) {
				Log.e(TAG, AppUtil.formatAjaxStatus(e));
				confd.reject(e);
			}

		}).always(new AlwaysCallback<AjaxCallback<JSONObject>, AjaxStatus>() {

            @Override
            public void onAlways(Promise.State state, AjaxCallback<JSONObject> resolved, AjaxStatus rejected) {
                sSetupLocked = false;
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
		
		configured.then(new DoneCallback<Object>() {

			@Override
			public void onDone(Object o) {
				d.resolve(sPlaceConf);
			}

		}).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }

        });
		
		return d.promise();
	}

    public static Promise<List<PlaceStub>, Exception, Double> getPlaceStubs() {
        final Deferred<List<PlaceStub>, Exception, Double> d = new DeferredObject<List<PlaceStub>, Exception, Double>();
        setup();

        configured.then(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                List<PlaceStub> results = new ArrayList<PlaceStub>();

                Set<Map.Entry<String, JSONObject>> placeSet = sPlacesTable.entrySet();
                for(Map.Entry<String, JSONObject> placeEntry: placeSet) {
                    results.add(new PlaceStub(placeEntry.getKey(), placeEntry.getValue().optString("title")));
                }

                d.resolve(results);
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
	 * Get JSON for a specific place.
	 * @param placeKey Place key (NOT title)
	 * @return JSON for place
	 */
	public static Promise<JSONObject, Exception, Double> getPlace(final String placeKey) {
		final Deferred<JSONObject, Exception, Double> d = new DeferredObject<JSONObject, Exception, Double>();
		setup();
		
		configured.then(new DoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
				try {
					JSONObject place = sPlaceConf.getJSONObject(placeKey);
					d.resolve(place);
				} catch (JSONException e) {
					Log.w(TAG, "getPlace(): " + e.getMessage());
					d.reject(e);
				}
			}

		}).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtil.formatAjaxStatus(status)));
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
    public static Promise<Set<PlaceTuple>, Exception, Double> getPlacesNear(final double sourceLat, final double sourceLon) {
        final Deferred<Set<PlaceTuple>, Exception, Double> deferred = new DeferredObject<Set<PlaceTuple>, Exception, Double>();
        setup();

        configured.then(new DoneCallback<Object>() {

            @Override
            public void onDone(Object o) {
                calculateNearby(deferred, sourceLat, sourceLon);
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                deferred.reject(new Exception(AppUtil.formatAjaxStatus(status)));
            }

        });

        return deferred.promise();
    }

    /**
     * Calculate nearby locations in a background thread
     * @param deferred
     * @param sourceLat
     * @param sourceLon
     */
    private static void calculateNearby(final Deferred<Set<PlaceTuple>, Exception, Double> deferred, final double sourceLat, final double sourceLon) {
        sDeferredManager.when(new DeferredAsyncTask<Void, Object, Set<PlaceTuple>>() {
            @Override
            protected Set<PlaceTuple> doInBackgroundSafe(Void... voids) throws Exception {
                Set<PlaceTuple> result = new TreeSet<PlaceTuple>(new PTDistanceComparator());

                Iterator<String> placesIter = sPlaceConf.keys();
                while (placesIter.hasNext()) {
                    String curPlaceKey = placesIter.next();
                    try {
                        JSONObject curPlace = sPlaceConf.getJSONObject(curPlaceKey);
                        if (curPlace.has("location")) {
                            JSONObject curPlaceLocation = curPlace.getJSONObject("location");
                            double placeLongitude = Double.parseDouble(curPlaceLocation.getString("longitude"));
                            double placeLatitude = Double.parseDouble(curPlaceLocation.getString("latitude"));

                            float[] results = new float[1];
                            Location.distanceBetween(sourceLat, sourceLon, placeLatitude, placeLongitude, results);

                            // If the place is within range, add it to the list
                            if (results[0] < AppUtil.NEARBY_RANGE) {
                                //Log.v(TAG, "Found nearby place " + curPlaceKey);
                                curPlace.put("distance", "" + results[0]);
                                result.add(new PlaceTuple(curPlaceKey, curPlace));
                            }
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "calculateNearby(): " + e.getMessage());
                    }
                }

                return result;
            }
        }).done(new DoneCallback<Set<PlaceTuple>>() {
            @Override
            public void onDone(Set<PlaceTuple> result) {
                deferred.resolve(result);
            }
        });
    }

    public static class PlaceTuple implements Comparable<PlaceTuple> {
        private String key;
        private JSONObject placeJson;

        public PlaceTuple(String key, JSONObject placeJson) {
            this.key = key;
            this.placeJson = placeJson;
        }

        public String getKey() {
            return this.key;
        }

        public JSONObject getPlaceJSON() {
            return this.placeJson;
        }

        @Override
        public String toString() {
            try {
                return placeJson.getString("title");
            } catch (JSONException e) {
                Log.w(TAG, "toString(): " + e.getMessage());
                return key;
            }
        }

        @Override
        public int compareTo(PlaceTuple another) {
            // Order by 'title' field alphabetically (or by key if getting title string fails)
            try {
                String thisTitle = getPlaceJSON().getString("title");
                String thatTitle = another.getPlaceJSON().getString("title");
                return thisTitle.compareTo(thatTitle);
            } catch (JSONException e) {
                Log.w(TAG, "compareTo(): " + e.getMessage());
                return getKey().compareTo(another.getKey());
            }
        }
    }

    private static class PTDistanceComparator implements Comparator<PlaceTuple> {
        @Override
        public int compare(PlaceTuple pt1, PlaceTuple pt2) {
            String distString1 = pt1.getPlaceJSON().optString("distance");
            String distString2 = pt2.getPlaceJSON().optString("distance");

            Float dist1 = Float.parseFloat(distString1);
            Float dist2 = Float.parseFloat(distString2);

            return dist1.compareTo(dist2);
        }
    }

}
