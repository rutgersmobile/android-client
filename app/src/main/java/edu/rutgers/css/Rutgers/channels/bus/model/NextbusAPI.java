package edu.rutgers.css.Rutgers.channels.bus.model;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.ApiRequest;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Provides static methods for access to the Nextbus API.
 * Uses nextbusjs data to create requests against the official Nextbus API.
 */
public final class NextbusAPI {

    private static final String TAG = "NextbusAPI";

    private static AgencyConfig sNBConf;
    private static AgencyConfig sNWKConf;
    private static ActiveStops sNBActive;
    private static ActiveStops sNWKActive;
    
    private static final String BASE_URL = "http://webservices.nextbus.com/service/publicXMLFeed?command=";
    private static final long activeExpireTime = ApiRequest.CACHE_ONE_MINUTE * 10; // active bus data cached ten minutes
    private static final long configExpireTime = ApiRequest.CACHE_ONE_HOUR; // config data cached one hour

    public static final String AGENCY_NB = "nb";
    public static final String AGENCY_NWK = "nwk";

    private static boolean sSettingUp = false;

    /** This class only contains static utility methods. */
    private NextbusAPI() {}

    /**
     * Load agency configurations and lists of active routes/stops for each campus.
     */
    private static void setup () throws JsonSyntaxException, IOException {
        if (sSettingUp) return;
        else sSettingUp = true;

        ActiveStops.AgentlessActiveStops nbActive =
                ApiRequest.api("nbactivestops.txt", activeExpireTime, ActiveStops.AgentlessActiveStops.class);
        ActiveStops.AgentlessActiveStops nwkActive =
                ApiRequest.api("nwkactivestops.txt", activeExpireTime, ActiveStops.AgentlessActiveStops.class);
        sNBActive = new ActiveStops(AGENCY_NB, nbActive);
        sNWKActive = new ActiveStops(AGENCY_NWK, nwkActive);

        sNBConf = ApiRequest.api("rutgersrouteconfig.txt", configExpireTime, AgencyConfig.class,
                new AgencyConfigDeserializer(AGENCY_NB));
        sNWKConf = ApiRequest.api("rutgers-newarkrouteconfig.txt", configExpireTime, AgencyConfig.class,
                new AgencyConfigDeserializer(AGENCY_NWK));

        sSettingUp = false;
    }
    
    /**
     * Get arrival time predictions for every stop on a route.
     * @param agency Agency (campus) that the route belongs to.
     * @param routeKey Route to get predictions for.
     * @return Promise for list of arrival time predictions.
     */
    public static List<Prediction> routePredict(@NonNull final String agency, @NonNull final String routeKey) throws JsonSyntaxException, XmlPullParserException, IOException {
        setup();
        
        LOGV(TAG, "routePredict: " + agency + ", " + routeKey);

        // Get agency configuration
        AgencyConfig conf = AGENCY_NB.equals(agency) ? sNBConf : sNWKConf;

        // Start building Nextbus query with predictionsForMultiStops command
        // Returns predictions for a set of route/stop combinations. Direction is optional and can be null.
        StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=rutgers");

        // Find route in agency config, and get its stop tags
        final Route route = conf.getRoutes().get(routeKey);
        if (route == null) {
            throw new IllegalArgumentException("Invalid route tag \""+routeKey+"\"");
        }

        for (String stopTag: route.getStopTags()) {
            // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
            queryBuilder.append("&stops=").append(routeKey).append("%7Cnull%7C").append(stopTag);
        }

        // Run the query we built and sort the prediction results
        List<Prediction> predictions = ApiRequest.xml(queryBuilder.toString(), ApiRequest.CACHE_NEVER, new PredictionXmlParser(PredictionXmlParser.PredictionType.ROUTE));
        Collections.sort(predictions, new Comparator<Prediction>() {
            @Override
            public int compare(@NonNull Prediction p1, @NonNull Prediction p2) {
                return route.getStopTags().indexOf(p1.getTag()) - route.getStopTags().indexOf(p2.getTag());
            }
        });

        return predictions;
    }
    
    /**
     * Get arrival time predictions for every route going through a stop.
     * @param agency Agency (campus) that the stop belongs to.
     * @param stopTitleKey Full title of the stop to get predictions for.
     * @return Promise for list of arrival time predictions.
     */
    public static List<Prediction> stopPredict(@NonNull final String agency, @NonNull final String stopTitleKey) throws JsonSyntaxException, XmlPullParserException, IOException {
        setup();
        LOGV(TAG, "stopPredict: " + agency + ", " + stopTitleKey);

        // Get agency configuration
        AgencyConfig conf = AGENCY_NB.equals(agency) ? sNBConf : sNWKConf;

        StringBuilder queryBuilder = new StringBuilder(BASE_URL + "predictionsForMultiStops&a=rutgers");

        // Get group of stop IDs by stop title
        StopGroup stopsByTitle = conf.getStopsByTitle().get(stopTitleKey);
        if (stopsByTitle == null) {
            throw new IllegalArgumentException("Invalid stop tag \""+stopTitleKey+"\"");
        }

        // For every stop tag with the given stop title, get all its routes
        for (String stopTag: stopsByTitle.getStopTags()) {
            Stop stop = conf.getStops().get(stopTag);
            if (stop == null) {
                throw new IllegalArgumentException("Stop tag \""+stopTag+"\" in stopsByTitle but not stops");
            }

            // Then use the route tags to build the query
            for (String routeTag: stop.getRouteTags()) {
                // multiple 'stops' parameters, these are: routeTag|dirTag|stopId
                queryBuilder.append("&stops=").append(routeTag).append("%7Cnull%7C").append(stopTag);
            }
        }

        // Run the query we built and sort the prediction results
        List<Prediction> predictions = ApiRequest.xml(queryBuilder.toString(), ApiRequest.CACHE_NEVER, new PredictionXmlParser(PredictionXmlParser.PredictionType.STOP));
        Collections.sort(predictions, new Comparator<Prediction>() {
            @Override
            public int compare(@NonNull Prediction p1, @NonNull Prediction p2) {
                int res = p1.getTitle().compareTo(p2.getTitle());
                if (res == 0) {
                    return p1.getDirection().compareTo(p2.getDirection());
                }
                return res;
            }
        });
        return predictions;
    }
    
    /**
     * Get active routes for an agency.
     * @param agency Agency (campus) to get active routes for.
     * @return Promise for list of route stubs (tags and titles).
     */
    public static List<RouteStub> getActiveRoutes(@NonNull final String agency) throws JsonSyntaxException, IOException {
        setup();
        ActiveStops active = AGENCY_NB.equals(agency) ? sNBActive : sNWKActive;
        return active.getRoutes();
    }
    
    /**
     * Get all routes from an agency's configuration.
     * @param agency Agency (campus) to get all routes for.
     * @return Promise for list of route stubs (tags and titles).
     */
    public static List<RouteStub> getAllRoutes(@NonNull final String agency) throws JsonSyntaxException, IOException {
        setup();
        AgencyConfig conf = AGENCY_NB.equals(agency) ? sNBConf : sNWKConf;
        return conf.getSortedRoutes();
    }

    /**
     * Get active stops for an agency.
     * @param agency Agency (campus) to active stops for.
     * @return Promise for list of stop stubs (titles and geohashes).
     */
    public static List<StopStub> getActiveStops(@NonNull final String agency) throws JsonSyntaxException, IOException {
        setup();
        ActiveStops active = AGENCY_NB.equals(agency) ? sNBActive : sNWKActive;
        return active.getStops();
    }
    
    /**
     * Get all stops from an agency's configuration.
     * @param agency Agency (campus) to get all stops for.
     * @return Promise for list of stop stubs (titles and geohashes).
     */
    public static List<StopStub> getAllStops(@NonNull final String agency) throws JsonSyntaxException, IOException {
        setup();
        AgencyConfig conf = AGENCY_NB.equals(agency) ? sNBConf : sNWKConf;
        return conf.getSortedStops();
    }
    
    /**
     * Get all bus stops (grouped by title) near a specific location.
     * @param agency Agency (campus) to get stops for.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for list of stop groups (unique stops grouped by title).
     */
    public static List<StopGroup> getStopsByTitleNear(@NonNull final String agency, final double sourceLat, final double sourceLon) throws JsonSyntaxException, IOException {
        setup();
        AgencyConfig conf = AGENCY_NB.equals(agency) ? sNBConf : sNWKConf;
        HashMap<String, StopGroup> stopsByTitle = conf.getStopsByTitle();

        List<StopGroup> nearStops = new ArrayList<>();

        // Loop through stop groups
        for (StopGroup stopGroup: stopsByTitle.values()) {
            // TODO Decode the geohash into long/lat and just compare with that

            // Get stop data for each stop tag and check the distance.
            for (String stopTag: stopGroup.getStopTags()) {
                Stop stop = conf.getStops().get(stopTag);
                if (stop == null) {
                    LOGW(TAG, "Stop tag \""+stopTag+"\" found in stopsByTitle not in stops");
                    continue; // Check next tag
                }

                // Calculate distance to stop
                double stopLat = Double.parseDouble(stop.getLatitude());
                double stopLon = Double.parseDouble(stop.getLongitude());
                float[] results = new float[1];
                Location.distanceBetween(sourceLat, sourceLon, stopLat, stopLon, results);

                if (results[0] < Config.NEARBY_RANGE) {
                    nearStops.add(stopGroup);
                    break; // Skip to next group
                }
            }
        }

        return nearStops;
    }

    /**
     * Get all active bus stops (grouped by title) near a specific location.
     * @param agency Agency (campus) to get stops for.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for list of stop groups (unique stops grouped by title).
     */
    public static List<StopGroup> getActiveStopsByTitleNear(final String agency, final float sourceLat, final float sourceLon) throws JsonSyntaxException, IOException {
        setup();

        final List<StopGroup> nearbyStops = getStopsByTitleNear(agency, sourceLat, sourceLon);
        final ActiveStops active = AGENCY_NB.equals(agency) ? sNBActive : sNWKActive;
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

        return results;
    }

}
