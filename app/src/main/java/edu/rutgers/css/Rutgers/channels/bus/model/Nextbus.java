package edu.rutgers.css.Rutgers.channels.bus.model;

import android.location.Location;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.jdeferred.multiple.OneResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.Request;
import edu.rutgers.css.Rutgers.utils.AppUtils;

/**
 * Singleton class that provides access to the Nextbus API. Uses the JSON that nextbusjs generates to create requests
 * against the official Nextbus API.
 */
public class Nextbus {

    private static final String TAG = "Nextbus";

    private static final AndroidDeferredManager sDM = new AndroidDeferredManager();
    private static Promise<Void, AjaxStatus, Void> configured;
    private static JSONObject mNBConf;
    private static JSONObject mNWKConf;
    private static JSONObject mNBActive;
    private static JSONObject mNWKActive;
    
    private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=";
    private static final long activeExpireTime = 1000 * 60 * 10; // active bus data cached ten minutes
    private static final long configExpireTime = 1000 * 60 * 60; // config data cached one hour

    public static final String AGENCY_NB = "nb";
    public static final String AGENCY_NWK = "nwk";
    
    /**
     * Load JSON data on active buses & the entire bus config.
     */
    private static void setup () {
        // This promise is used to notify the other objects that the object has been configured.
        final Deferred<Void, AjaxStatus, Void> confd = new DeferredObject<>();
        configured = confd.promise();

        final Promise promiseNBActive = Request.api("nbactivestops.txt", activeExpireTime);
        final Promise promiseNWKActive = Request.api("nwkactivestops.txt", activeExpireTime);
        final Promise promiseNBConf = Request.api("rutgersrouteconfig.txt", configExpireTime);
        final Promise promiseNWKConf = Request.api("rutgers-newarkrouteconfig.txt", configExpireTime);

        sDM.when(AndroidExecutionScope.BACKGROUND, promiseNBActive, promiseNBConf, promiseNWKActive, promiseNWKConf).done(new DoneCallback<MultipleResults>() {
            
            @Override
            public void onDone(MultipleResults res) {

                for (int i = 0; i < res.size(); i++) {
                    OneResult r = res.get(i);

                    if (r.getPromise() == promiseNBActive) mNBActive = (JSONObject) r.getResult();
                    else if (r.getPromise() == promiseNWKActive) mNWKActive = (JSONObject) r.getResult();
                    else if (r.getPromise() == promiseNBConf) mNBConf = (JSONObject) r.getResult();
                    else if (r.getPromise() == promiseNWKConf) mNWKConf = (JSONObject) r.getResult();
                }

                confd.resolve(null);

            }

        }).fail(new FailCallback<OneReject>() {
            
            @Override
            public void onFail(OneReject reject) {
                AjaxStatus r = (AjaxStatus) reject.getReject();
                Log.e(TAG, AppUtils.formatAjaxStatus(r));
                confd.reject(r);
            }

        });
    }
    
    /**
     * Queries the Nextbus API for predictions for every stop in the given route.
     * @param agency Agency (campus) to get bus data for
     * @param route Route to get prediction data for
     * @return Promise for prediction data for each stop on the route
     */
    public static Promise<ArrayList<Prediction>, Exception, Void> routePredict(final String agency, final String route) {
        final Deferred<ArrayList<Prediction>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                Log.v(TAG, "routePredict: " + agency + ", " + route);

                // Get agency configuration
                JSONObject conf = agency.equals(AGENCY_NB) ? mNBConf : mNWKConf;

                // Start building Nextbus query with predictionsForMultiStops command
                // Returns predictions for a set of route/stop combinations. Direction is optional and can be null.
                StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=" + (agency.equals(AGENCY_NB)? "rutgers" : "rutgers-newark"));
                
                try {
                    // Find route in agency config, and get its stop tags
                    JSONObject routeData = conf.getJSONObject("routes").getJSONObject(route);
                    JSONArray stops = routeData.getJSONArray("stops");

                    // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
                    for (int i = 0; i < stops.length(); i++) {
                        queryBuilder.append("&stops=").append(route).append("|null|").append(stops.getString(i));
                    }

                                    
                    Request.xml(queryBuilder.toString(), Request.CACHE_NEVER).done(new DoneCallback<XmlDom>() {
                        
                        @Override
                        public void onDone(XmlDom xml) {
                            ArrayList<Prediction> results = new ArrayList<>();
                            
                            // Read each group of predictions for a single stop.
                            for (XmlDom stop: xml.tags("predictions")) {
                                Prediction newPrediction = new Prediction(stop.attr("stopTitle"), stop.attr("stopTag"));

                                // Predictions are children of the 'direction' element
                                if (stop.tag("direction") == null) continue;

                                // Read each prediction element. Contains estimated times for each vehicle to reach stop.
                                for (XmlDom time: stop.tags("prediction")) {
                                    newPrediction.addMinutes(Integer.parseInt(time.attr("minutes")));
                                }

                                results.add(newPrediction);
                            }

                            d.resolve(results);
                        }

                    }).fail(new FailCallback<AjaxStatus>() {
                        
                        @Override
                        public void onFail(AjaxStatus status) {
                            d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
                        }

                    });
    
                } catch (JSONException e) {
                    Log.e(TAG, "routePredict(): " + e.getMessage());
                    d.reject(e);
                }

            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }
    
    /**
     * Queries the Nextbus API for prediction data for every route going through given stop.
     * @param agency Agency (campus) to get bus data for
     * @param stop Stop to get prediction data for
     * @return Promise for prediction data for each route arriving at the stop
     */
    public static Promise<ArrayList<Prediction>, Exception, Void> stopPredict(final String agency, final String stop) {
        final Deferred<ArrayList<Prediction>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                Log.v(TAG, "stopPredict: " + agency + ", " + stop);

                // Get agency configuration
                JSONObject conf = agency.equals(AGENCY_NB) ? mNBConf : mNWKConf;

                StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=" + (agency.equals(AGENCY_NB)? "rutgers" : "rutgers-newark"));
                
                try {
                    // Get group of stop IDs by stop title
                    JSONObject routeData = conf.getJSONObject("stopsByTitle").getJSONObject(stop);
                    JSONArray stopTags = routeData.getJSONArray("tags");

                    // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
                    for (int i = 0; i < stopTags.length(); i++) {
                        JSONArray routeTags = conf.getJSONObject("stops").getJSONObject(stopTags.getString(i)).getJSONArray("routes");
                        for (int j = 0; j < routeTags.length(); j++) {
                            queryBuilder.append("&stops=").append(routeTags.getString(j)).append("|null|").append(stopTags.getString(i));
                        }
                    }
                    
                    Request.xml(queryBuilder.toString(), Request.CACHE_NEVER).done(new DoneCallback<XmlDom>() {
                        
                        @Override
                        public void onDone(XmlDom xml) {
                            ArrayList<Prediction> results = new ArrayList<>();

                            // Read each group of predictions for a single stop.
                            for (XmlDom stop: xml.tags("predictions")) {
                                Prediction oneresult = new Prediction(stop.attr("routeTitle"), stop.attr("routeTag"));

                                // Predictions are children of the 'direction' element
                                XmlDom dirtag = stop.tag("direction");
                                if (dirtag != null) oneresult.setDirection(dirtag.attr("title"));
                                else continue;

                                // Read each prediction element. Contains estimated times for each vehicle to reach stop.
                                for (XmlDom time: stop.tags("prediction")) {
                                    oneresult.addMinutes(Integer.parseInt(time.attr("minutes")));
                                }
                                results.add(oneresult);
                            }
                            
                            d.resolve(results);
                        }

                    }).fail(new FailCallback<AjaxStatus>() {
                        
                        @Override
                        public void onFail(AjaxStatus status) {
                            d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
                        }

                    });
    
                } catch (JSONException e) {
                    Log.e(TAG, "stopPredict(): " + e.getMessage());
                    d.reject(e);
                }

            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }
    
    /**
     * Get active routes for given agency (campus).
     * @param agency Agency (campus) to get routes for
     * @return Promise for a JSON array of active routes.
     */
    public static Promise<JSONArray, Exception, Void> getActiveRoutes(final String agency) {
        final Deferred<JSONArray, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                try {
                    JSONObject active = agency.equals(AGENCY_NB) ? mNBActive : mNWKActive;
                    if(active != null) d.resolve(active.getJSONArray("routes"));
                    else {
                        Log.e(TAG, "active buses null for agency " + agency);
                        d.reject(new Exception("Active bus data was null"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getActiveRoutes(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }
    
    /**
     * Get all configured routes for given agency (campus).
     * @param agency Agency (campus) to get routes for
     * @return Promise for a JSON array of all configured routes.
     */
    public static Promise<JSONArray, Exception, Void> getAllRoutes(final String agency) {
        final Deferred<JSONArray, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                try {
                    JSONObject conf = agency.equals(AGENCY_NB) ? mNBConf : mNWKConf;
                    if(conf != null) d.resolve(conf.getJSONArray("sortedRoutes"));
                    else {
                        Log.e(TAG, "conf null for agency " + agency);
                        d.reject(new Exception("Bus config data was null"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getAllRoutes(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }

    /**
     * Get active stops for a given agency (campus).
     * @param agency Agency (campus) to get stops for
     * @return A promise for a JSON array of active stops.
     */
    public static Promise<JSONArray, Exception, Void> getActiveStops(final String agency) {
        final Deferred<JSONArray, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {

            @Override
            public void onDone(Void v) {
                try {
                    JSONObject active = agency.equals(AGENCY_NB) ? mNBActive : mNWKActive;
                    if (active != null) d.resolve(active.getJSONArray("stops"));
                    else {
                        Log.e(TAG, "active buses null for agency " + agency);
                        d.reject(new Exception("Active bus data is null"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getActiveStops(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }
    
    /**
     * Get all configured stops for a given agency (campus).
     * @param agency Agency (campus) to get stops for
     * @return A promise for a JSON array of all stops found in the agency config.
     */
    public static Promise<JSONArray, Exception, Void> getAllStops(final String agency) {
        final Deferred<JSONArray, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            @Override
            public void onDone(Void v) {
                try {
                    JSONObject conf = agency.equals(AGENCY_NB) ? mNBConf : mNWKConf;
                    if(conf != null) d.resolve(conf.getJSONArray("sortedStops"));
                    else {
                        Log.e(TAG, "conf null for agency " + agency);
                        d.reject(new Exception("Bus config data was null"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getAllStops(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }
    
    /**
     * Get all bus stops (by title) near a specific location.
     * @param agency Agency (campus) to get stops for
     * @param sourceLat Latitude of location
     * @param sourceLon Longitude of location
     * @return stopByTitle JSON objects
     */
    public static Promise<JSONObject, Exception, Void> getStopsByTitleNear(final String agency, final double sourceLat, final double sourceLon) {
        final Deferred<JSONObject, Exception, Void> d = new DeferredObject<>();
        setup();

        if(agency == null) {
            Log.e(TAG, "getStopsByTitleNear(): agency tag not set");
            d.reject(new Exception("Agency tag not set"));
            return d.promise();
        }

        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            @Override
            public void onDone(Void v) {
                JSONObject nearStops = new JSONObject();
                
                try {
                    JSONObject conf = agency.equals(AGENCY_NB) ? mNBConf : mNWKConf;
                    JSONObject stopsByTitle = conf.getJSONObject("stopsByTitle");
                    
                    // Loop through stop titles
                    for(Iterator<String> confIter = stopsByTitle.keys(); confIter.hasNext();) {
                        String curTitle = confIter.next();
                        JSONObject curStopByTitle = stopsByTitle.getJSONObject(curTitle);
                        JSONArray curStopTags = curStopByTitle.getJSONArray("tags");
                        
                        // Loop through tags for the stop -- if any are within range, list this stop
                        for(int i = 0; i < curStopTags.length(); i++) {
                            JSONObject curStop = conf.getJSONObject("stops").getJSONObject(curStopTags.getString(i));
                            
                            // Get distance between building and stop
                            double endLatitude = Double.parseDouble(curStop.getString("lat"));
                            double endLongitude = Double.parseDouble(curStop.getString("lon"));
                            float[] results = new float[1];
                            Location.distanceBetween(sourceLat, sourceLon, endLatitude, endLongitude, results);
                            
                            // If the stop is within range, add it to the list
                            if(results[0] < Config.NEARBY_RANGE) {
                                nearStops.put(curTitle, curStopByTitle);
                                break; // Skip to next stop title
                            }
                            
                        }
                        
                    }
                    
                    d.resolve(nearStops);
                } catch(JSONException e) {
                    Log.e(TAG, "getStopsByTitleNear(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<AjaxStatus>() {

            @Override
            public void onFail(AjaxStatus status) {
                d.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }

        });
        
        return d.promise();
    }

    /**
     * Get all active bus stops (by title) near a specific location.
     * @param agency Agency (campus) to get bus stops for
     * @param sourceLat Latitude of location
     * @param sourceLon Longitude of location
     * @return stopByTitle JSON objects
     */
    public static Promise<JSONObject, Exception, Void> getActiveStopsByTitleNear(final String agency, final float sourceLat, final float sourceLon) {
        final Deferred<JSONObject, Exception, Void> d = new DeferredObject<>();
        Promise<JSONObject, Exception, Void> allNearStops = getStopsByTitleNear(agency, sourceLat, sourceLon);
        
        sDM.when(allNearStops, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<JSONObject>() {

            public void onDone(JSONObject stopsByTitle) {
                JSONObject active = agency.equals(AGENCY_NB) ? mNBActive : mNWKActive;
                
                JSONObject result = new JSONObject();
                
                try {
                    JSONArray activeStops = active.getJSONArray("stops");
                    
                    // Loop through ALL nearby stops returned by earlier call
                    for(Iterator<String> stopTitleIter = stopsByTitle.keys(); stopTitleIter.hasNext();) {
                        String curTitle = stopTitleIter.next();
                        
                        // Check to see if this stop is active
                        for(int i = 0; i < activeStops.length(); i++) {
                            JSONObject anActiveStop = activeStops.getJSONObject(i);
                            if(anActiveStop.getString("title").equals(curTitle)) {
                                result.put(curTitle, stopsByTitle.get(curTitle));    
                            }
                        }
                    }
                    
                    d.resolve(result);
                } catch(JSONException e) {
                    Log.e(TAG, "getActiveStopsByTitleNear(): " + e.getMessage());
                    d.reject(e);
                }
            }

        }).fail(new FailCallback<Exception>() {

            @Override
            public void onFail(Exception result) {
                d.reject(result);
            }

        });
        
        return d.promise();
    }

}
