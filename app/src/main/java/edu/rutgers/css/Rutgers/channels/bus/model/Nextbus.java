package edu.rutgers.css.Rutgers.channels.bus.model;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private static Promise<Void, Exception, Void> configured;
    private static AgencyConfig mNBConf;
    private static AgencyConfig mNWKConf;
    private static ActiveStops mNBActive;
    private static ActiveStops mNWKActive;
    
    private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=";
    private static final long activeExpireTime = Request.CACHE_ONE_MINUTE * 10; // active bus data cached ten minutes
    private static final long configExpireTime = Request.CACHE_ONE_HOUR; // config data cached one hour

    public static final String AGENCY_NB = "nb";
    public static final String AGENCY_NWK = "nwk";
    
    /**
     * Load JSON data on active buses & the entire bus config.
     */
    private static void setup () {
        // This promise is used to notify the other objects that the object has been configured.
        final Deferred<Void, Exception, Void> confd = new DeferredObject<>();
        configured = confd.promise();

        final Promise promiseNBActive = Request.api("nbactivestops.txt", activeExpireTime);
        final Promise promiseNWKActive = Request.api("nwkactivestops.txt", activeExpireTime);
        final Promise promiseNBConf = Request.api("rutgersrouteconfig.txt", configExpireTime);
        final Promise promiseNWKConf = Request.api("rutgers-newarkrouteconfig.txt", configExpireTime);

        sDM.when(AndroidExecutionScope.BACKGROUND, promiseNBActive, promiseNBConf, promiseNWKActive, promiseNWKConf).done(new DoneCallback<MultipleResults>() {
            @Override
            public void onDone(MultipleResults results) {
                Gson gson = new Gson();

                try {
                    for (OneResult result: results) {
                        if (result.getPromise() == promiseNBActive) {
                            mNBActive = gson.fromJson(result.getResult().toString(), ActiveStops.class);
                        } else if (result.getPromise() == promiseNWKActive) {
                            mNWKActive = gson.fromJson(result.getResult().toString(), ActiveStops.class);
                        } else if (result.getPromise() == promiseNBConf) {
                            mNBConf = new AgencyConfig((JSONObject) result.getResult());
                        } else if (result.getPromise() == promiseNWKConf) {
                            mNWKConf = new AgencyConfig((JSONObject) result.getResult());
                        }
                    }
                } catch (JsonSyntaxException | JSONException e) {
                    Log.e(TAG, e.getMessage());
                    confd.reject(e);
                }

                confd.resolve(null);
            }
        }).fail(new FailCallback<OneReject>() {
            @Override
            public void onFail(OneReject reject) {
                AjaxStatus status = (AjaxStatus) reject.getReject();
                Log.e(TAG, AppUtils.formatAjaxStatus(status));
                confd.reject(new Exception(AppUtils.formatAjaxStatus(status)));
            }
        });
    }
    
    /**
     * Queries the Nextbus API for predictions for every stop in the given route.
     * @param agency Agency (campus) to get bus data for
     * @param routeKey Route to get prediction data for
     * @return
     */
    public static Promise<ArrayList<Prediction>, Exception, Void> routePredict(@NonNull final String agency, @NonNull final String routeKey) {
        final Deferred<ArrayList<Prediction>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                Log.v(TAG, "routePredict: " + agency + ", " + routeKey);

                // Get agency configuration
                AgencyConfig conf = AGENCY_NB.equals(agency) ? mNBConf : mNWKConf;

                // Start building Nextbus query with predictionsForMultiStops command
                // Returns predictions for a set of route/stop combinations. Direction is optional and can be null.
                StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=" + (AGENCY_NB.equals(agency)? "rutgers" : "rutgers-newark"));

                // Find route in agency config, and get its stop tags
                Route route = conf.getRoutes().get(routeKey);
                if(route == null) {
                    d.reject(new IllegalArgumentException("Invalid route tag \""+routeKey+"\""));
                    return;
                }

                for(String stopTag: route.getStopTags()) {
                    // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
                    queryBuilder.append("&stops=").append(routeKey).append("|null|").append(stopTag);
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
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Queries the Nextbus API for prediction data for every route going through given stop.
     * @param agency Agency (campus) to get bus data for
     * @param stopTitleKey Stop to get prediction data for
     * @return
     */
    public static Promise<ArrayList<Prediction>, Exception, Void> stopPredict(@NonNull final String agency, @NonNull final String stopTitleKey) {
        final Deferred<ArrayList<Prediction>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            public void onDone(Void v) {
                Log.v(TAG, "stopPredict: " + agency + ", " + stopTitleKey);

                // Get agency configuration
                AgencyConfig conf = AGENCY_NB.equals(agency) ? mNBConf : mNWKConf;

                StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=" + (AGENCY_NB.equals(agency)? "rutgers" : "rutgers-newark"));

                // Get group of stop IDs by stop title
                StopGroup stopsByTitle = conf.getStopsByTitle().get(stopTitleKey);
                if(stopsByTitle == null) {
                    d.reject(new IllegalArgumentException("Invalid stop tag \""+stopTitleKey+"\""));
                    return;
                }

                // For every stop tag with the given stop title, get all its routes
                for(String stopTag: stopsByTitle.getStopTags()) {
                    Stop stop = conf.getStops().get(stopTag);
                    if(stop == null) {
                        d.reject(new Exception("Stop tag \""+stopTag+"\" in stopsByTitle but not stops"));
                        return;
                    }

                    // Then use the route tags to build the query
                    for(String routeTag: stop.getRouteTags()) {
                        // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
                        queryBuilder.append("&stops=").append(routeTag).append("|null|").append(stopTag);
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
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Get active routes for given agency (campus).
     * @param agency Agency (campus) to get routes for
     * @return
     */
    public static Promise<List<RouteStub>, Exception, Void> getActiveRoutes(@NonNull final String agency) {
        final Deferred<List<RouteStub>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            @Override
            public void onDone(Void v) {
                ActiveStops active = AGENCY_NB.equals(agency) ? mNBActive : mNWKActive;
                d.resolve(active.getRoutes());
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Get all configured routes for given agency (campus).
     * @param agency Agency (campus) to get routes for
     * @return
     */
    public static Promise<List<RouteStub>, Exception, Void> getAllRoutes(@NonNull final String agency) {
        final Deferred<List<RouteStub>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            @Override
            public void onDone(Void v) {
                AgencyConfig conf = AGENCY_NB.equals(agency) ? mNBConf : mNWKConf;
                d.resolve(conf.getSortedRoutes());
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }

    /**
     * Get active stops for a given agency (campus).
     * @param agency Agency (campus) to get stops for
     * @return
     */
    public static Promise<List<StopStub>, Exception, Void> getActiveStops(@NonNull final String agency) {
        final Deferred<List<StopStub>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            @Override
            public void onDone(Void v) {
                ActiveStops active = AGENCY_NB.equals(agency) ? mNBActive : mNWKActive;
                d.resolve(active.getStops());
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Get all configured stops for a given agency (campus).
     * @param agency Agency (campus) to get stops for
     * @return
     */
    public static Promise<List<StopStub>, Exception, Void> getAllStops(@NonNull final String agency) {
        final Deferred<List<StopStub>, Exception, Void> d = new DeferredObject<>();
        setup();
        
        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            @Override
            public void onDone(Void v) {
                AgencyConfig conf = AGENCY_NB.equals(agency) ? mNBConf : mNWKConf;
                d.resolve(conf.getSortedStops());
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }
    
    /**
     * Get all bus stops (by title) near a specific location.
     * @param agency Agency (campus) to get stops for
     * @param sourceLat Latitude of location
     * @param sourceLon Longitude of location
     * @return
     */
    public static Promise<List<StopGroup>, Exception, Void> getStopsByTitleNear(@NonNull final String agency, final double sourceLat, final double sourceLon) {
        final Deferred<List<StopGroup>, Exception, Void> d = new DeferredObject<>();
        setup();

        sDM.when(configured, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<Void>() {
            
            @Override
            public void onDone(Void v) {
                AgencyConfig conf = AGENCY_NB.equals(agency) ? mNBConf : mNWKConf;
                HashMap<String, StopGroup> stopsByTitle = conf.getStopsByTitle();

                List<StopGroup> nearStops = new ArrayList<>();

                // Loop through stop groups
                for(StopGroup stopGroup: stopsByTitle.values()) {
                    // TODO Decode the geohash into long/lat and just compare with that

                    // Get stop data for each stop tag and check the distance.
                    for(String stopTag: stopGroup.getStopTags()) {
                        Stop stop = conf.getStops().get(stopTag);
                        if(stop == null) {
                            Log.w(TAG, "Stop tag \""+stopTag+"\" found in stopsByTitle not in stops");
                            continue; // Check next tag
                        }

                        // Calculate distance to stop
                        double stopLat = Double.parseDouble(stop.getLatitude());
                        double stopLon = Double.parseDouble(stop.getLongitude());
                        float[] results = new float[1];
                        Location.distanceBetween(sourceLat, sourceLon, stopLat, stopLon, results);

                        if(results[0] < Config.NEARBY_RANGE) {
                            nearStops.add(stopGroup);
                            break; // Skip to next group
                        }
                    }

                    d.resolve(nearStops);
                }
            }

        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }

    /**
     * Get all active bus stops (by title) near a specific location.
     * @param agency Agency (campus) to get bus stops for
     * @param sourceLat Latitude of location
     * @param sourceLon Longitude of location
     * @return
     */
    public static Promise<List<StopGroup>, Exception, Void> getActiveStopsByTitleNear(final String agency, final float sourceLat, final float sourceLon) {
        final Deferred<List<StopGroup>, Exception, Void> d = new DeferredObject<>();
        final ActiveStops active = AGENCY_NB.equals(agency) ? mNBActive : mNWKActive;

        final Promise<List<StopGroup>, Exception, Void> allNearStops = getStopsByTitleNear(agency, sourceLat, sourceLon);
        sDM.when(allNearStops, AndroidExecutionScope.BACKGROUND).then(new DoneCallback<List<StopGroup>>() {
            @Override
            public void onDone(List<StopGroup> nearbyStops) {
                List<StopGroup> results = new ArrayList<>();

                for (StopGroup stopGroup : nearbyStops) {
                    String stopTitle = stopGroup.getTitle();

                    // Try to find this stop in list of active stops
                    for (StopStub activeStopStub : active.getStops()) {
                        if (StringUtils.equals(stopTitle, activeStopStub.getTitle())) {
                            results.add(stopGroup); // Found: it's active
                        }
                    }
                }

                d.resolve(results);
            }
        }).fail(new FailCallback<Exception>() {
            @Override
            public void onFail(Exception e) {
                d.reject(e);
            }
        });
        
        return d.promise();
    }

}
