package edu.rutgers.css.Rutgers.channels.places.model;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.AlwaysCallback;
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Provides access to the Places database.
 * @author James Chambers
 */
public final class PlacesAPI {
    
    private static final String TAG = "PlacesAPI";

    private static final DeferredManager sDM = new DefaultDeferredManager();
    private static Promise<Void, Exception, Void> configured;
    private static boolean sIsSettingUp;

    /** Map of Place keys to Place objects. Initialized in {@link #setup()}. */
    private static Map<String, Place> sPlaces;
    private static Map<String, Place> sTokens;

    private PlacesAPI() {}

    /**
     * Grab the places flat-file from the API and convert it into a map.
     */
    private synchronized static void setup() {
        if(sIsSettingUp || sPlaces != null) return;
        else sIsSettingUp = true;

        final Deferred<Void, Exception, Void> confd = new DeferredObject<>();

        sDM.when(Request.api("places.txt", Request.CACHE_ONE_DAY)).done(new DoneCallback<JSONObject>() {
            @Override
            public void onDone(JSONObject result) {
                // There are 1,286 places in the database as of Jan '15
                sPlaces = new HashMap<>(1300);
                sTokens = new HashMap<>(1300);
                Gson gson = new Gson();

                try {
                    // Get the place documents and build the places map.
                    JSONObject allPlaces = result.getJSONObject("all");
                    for(Iterator<String> placeKeyIter = allPlaces.keys(); placeKeyIter.hasNext();) {
                        String placeKey = placeKeyIter.next();
                        Place place = gson.fromJson(allPlaces.getJSONObject(placeKey).toString(), Place.class);
                        sPlaces.put(placeKey, place);
                    }

                    // Get the lunr.js document tokens and build the special token map.
                    JSONObject docStore = result.getJSONObject("lunr").getJSONObject("documentStore").getJSONObject("store");
                    for (Iterator<String> docKeyIter = docStore.keys(); docKeyIter.hasNext();) {
                        String docKey = docKeyIter.next();
                        Place place = sPlaces.get(docKey);
                        if (place != null) {
                            JSONArray tokens = docStore.getJSONArray(docKey);
                            for (int i = 0; i < tokens.length(); i++) {
                                sTokens.put(tokens.getString(i), place);
                            }
                        } else {
                            Log.e(TAG, "No document found for key from lunr.documentStore: \""+docKey+"\"");
                        }
                    }


                    confd.resolve(null);
                } catch (JSONException | JsonSyntaxException e) {
                    Log.e(TAG, "setup(): " + e.getMessage());
                    confd.reject(e);
                    sPlaces = null;
                }
            }
        }).fail(new FailCallback<AjaxStatus>() {
            @Override
            public void onFail(AjaxStatus result) {
                confd.reject(new Exception(AppUtils.formatAjaxStatus(result)));
            }
        }).always(new AlwaysCallback<JSONObject, AjaxStatus>() {
            @Override
            public void onAlways(Promise.State state, JSONObject resolved, AjaxStatus rejected) {
                sIsSettingUp = false;
            }
        });

        configured = confd.promise();
    }

    /**
     * Get a specific place from the Places API.
     * @param placeKey Key for place entry, returned from search results
     * @return Promise for a Place object representing the entry in the database
     */
    public static Promise<Place, Exception, Void> getPlace(@NonNull final String placeKey) {
        final Deferred<Place, Exception, Void> deferred = new DeferredObject<>();

        setup();
        sDM.when(configured).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void nothing) {
                deferred.resolve(sPlaces.get(placeKey));
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                deferred.reject(result);
            }
        });

        return deferred.promise();
    }

    /**
     * Search for places near a given location.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
     */
    public static Promise<List<Place>, Exception, Void> getPlacesNear(final double sourceLat, final double sourceLon) {
        final Deferred<List<Place>, Exception, Void> deferred = new DeferredObject<>();

        setup();
        sDM.when(configured).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void nothing) {
                List<Place> results = new ArrayList<>();
                List<AbstractMap.SimpleEntry<Float, Place>> nearbyPlaces = new ArrayList<>();

                for (Place place : sPlaces.values()) {
                    if (place.getLocation() == null) continue;
                    final double placeLat = place.getLocation().getLatitude();
                    final double placeLon = place.getLocation().getLongitude();
                    float dist[] = new float[1];
                    Location.distanceBetween(placeLat, placeLon, sourceLat, sourceLon, dist);
                    if (dist[0] <= Config.NEARBY_RANGE)
                        nearbyPlaces.add(new AbstractMap.SimpleEntry<>(dist[0], place));
                }

                Collections.sort(nearbyPlaces, new Comparator<AbstractMap.SimpleEntry<Float, Place>>() {
                    @Override
                    public int compare(AbstractMap.SimpleEntry<Float, Place> left, AbstractMap.SimpleEntry<Float, Place> right) {
                        return left.getKey().compareTo(right.getKey());
                    }
                });

                for (AbstractMap.SimpleEntry<Float, Place> entry : nearbyPlaces) {
                    results.add(entry.getValue());
                }

                deferred.resolve(results);
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                deferred.reject(result);
            }
        });

        return deferred.promise();
    }

    /**
     * <p>Search places by title.</p>
     * @param query Query string
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
     */
    public static Promise<List<Place>, Exception, Void> searchPlaces(final String query) {
        final Deferred<List<Place>, Exception, Void> deferred = new DeferredObject<>();

        setup();
        sDM.when(configured).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void none) {
                List<Place> results = new ArrayList<>();

                Place p = sTokens.get(query.toLowerCase(Locale.US));
                if (p != null) {
                    results.add(p);
                }

                // Split each place title up into individual words and see if the query is a prefix of any of them
                for (Place place : sPlaces.values()) {
                    String parts[] = StringUtils.split(place.getTitle(), ' ');
                    for (String part : parts) {
                        if (StringUtils.startsWithIgnoreCase(part, query)) {
                            // The check makes sure we don't duplicate the special token match 'p'
                            if (place != p) results.add(place);
                            break;
                        }
                    }
                }

                deferred.resolve(results);
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception result) {
                deferred.reject(result);
            }
        });

        return deferred.promise();
    }

}
