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
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.android.DeferredAsyncTask;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();
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
        sDM.when(promisePlaces, AndroidExecutionScope.BACKGROUND).done(new DoneCallback<AjaxCallback<JSONObject>>() {

            @Override
            public void onDone(AjaxCallback<JSONObject> res) {
                // If the result came from cache, skip new setup
                if (sPlacesTable != null && res.getStatus().getSource() != AjaxStatus.NETWORK) {
                    Log.v(TAG, "Retaining cached place data");
                    confd.resolve(null);
                    return;
                }
                else {
                    Log.v(TAG, "Generating new place data from network");
                }

                try {
                    JSONObject allPlaces = res.getResult().getJSONObject("all");
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

    public static Promise<List<PlaceStub>, Exception, Double> getPlaceStubs() {
        final Deferred<List<PlaceStub>, Exception, Double> d = new DeferredObject<List<PlaceStub>, Exception, Double>();
        setup();

        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Object>() {
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
		
		sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Object>() {
			
			@Override
			public void onDone(Object o) {
                d.resolve(sPlacesTable.get(placeKey));
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
    public static Promise<Set<PlaceStub>, Exception, Double> getPlacesNear(final double sourceLat, final double sourceLon) {
        final Deferred<Set<PlaceStub>, Exception, Double> deferred = new DeferredObject<Set<PlaceStub>, Exception, Double>();
        setup();

        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Object>() {

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
    private static void calculateNearby(final Deferred<Set<PlaceStub>, Exception, Double> deferred, final double sourceLat, final double sourceLon) {
        sDM.when(new DeferredAsyncTask<Void, Object, Set<PlaceStub>>() {
            @Override
            protected Set<PlaceStub> doInBackgroundSafe(Void... voids) throws Exception {
                Set<PlaceStub> result = new TreeSet<PlaceStub>(new PlaceDistanceComparator());

                Iterator<String> placesIter = sPlacesTable.keySet().iterator();
                while (placesIter.hasNext()) {
                    String curPlaceKey = placesIter.next();
                    try {
                        JSONObject curPlace = sPlacesTable.get(curPlaceKey);
                        if (curPlace.has("location")) {
                            JSONObject curPlaceLocation = curPlace.getJSONObject("location");
                            double placeLongitude = Double.parseDouble(curPlaceLocation.getString("longitude"));
                            double placeLatitude = Double.parseDouble(curPlaceLocation.getString("latitude"));

                            float[] results = new float[1];
                            Location.distanceBetween(sourceLat, sourceLon, placeLatitude, placeLongitude, results);

                            // If the place is within range, add it to the list
                            if (results[0] <= AppUtil.NEARBY_RANGE) {
                                PlaceStub stub = new PlaceStub(curPlaceKey, curPlace.optString("title"));
                                stub.setDistance(results[0]);
                                result.add(stub);
                            }
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "calculateNearby(): " + e.getMessage());
                    }
                }

                return result;
            }
        }).done(new DoneCallback<Set<PlaceStub>>() {
            @Override
            public void onDone(Set<PlaceStub> result) {
                deferred.resolve(result);
            }
        });
    }

    private static class PlaceDistanceComparator implements Comparator<PlaceStub> {
        @Override
        public int compare(PlaceStub ps1, PlaceStub ps2) {
            Float dist1 = new Float(ps1.getDistance());
            Float dist2 = new Float(ps2.getDistance());
            return dist1.compareTo(dist2);
        }
    }

}
