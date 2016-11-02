package edu.rutgers.css.Rutgers.model;

import android.location.Location;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.NextBusService;
import edu.rutgers.css.Rutgers.api.QueryStop;
import edu.rutgers.css.Rutgers.api.bus.model.ActiveStops;
import edu.rutgers.css.Rutgers.api.bus.model.AgencyConfig;
import edu.rutgers.css.Rutgers.api.bus.model.Prediction;
import edu.rutgers.css.Rutgers.api.bus.model.Predictions;
import edu.rutgers.css.Rutgers.api.bus.model.SimpleBody;
import edu.rutgers.css.Rutgers.api.bus.model.SimpleMessage;
import edu.rutgers.css.Rutgers.api.bus.model.SimplePrediction;
import edu.rutgers.css.Rutgers.api.bus.model.SimplePredictions;
import edu.rutgers.css.Rutgers.api.bus.model.route.Route;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.Stop;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
import rx.Observable;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * Provides static methods for access to the Nextbus API.
 * Uses nextbusjs data to create requests against the official Nextbus API.
 */
public final class NextbusAPI {

    public static final NextBusService service = RutgersApplication.nbRetrofit
        .create(NextBusService.class);

    private static final String TAG = "NextbusAPI";

    public static final String AGENCY_NB = "nb";
    public static final String AGENCY_NWK = "nwk";
    public static final String AGENCY_RUTGERS = "rutgers";

    /** This class only contains static utility methods. */
    private NextbusAPI() {}

    private static Observable<AgencyConfig> configForAgency(final String agency) {
        return AGENCY_NB.equals(agency) || AGENCY_RUTGERS.equals(agency)
            ? RutgersAPI.service.getNewBrunswickAgencyConfig()
            : RutgersAPI.service.getNewarkAgencyConfig();
    }

    private static Observable<ActiveStops> activeStopsForAgency(final String agency) {
        return (AGENCY_NB.equals(agency) || AGENCY_RUTGERS.equals(agency)
            ? RutgersAPI.service.getNewBrunswickActiveStops()
            : RutgersAPI.service.getNewarkActiveStops()).map(activeStops -> {
            activeStops.setAgencyTag(agency);
            return activeStops;
        });
    }

    /**
     * Get arrival time predictions for every stop on a route.
     * @param agency Agency (campus) that the route belongs to.
     * @param routeKey Route to get predictions for.
     * @return Promise for list of arrival time predictions.
     */
    public static Observable<Predictions> routePredict(final String agency, final String routeKey) {
        // get agency config
        return configForAgency(agency).flatMap(agencyConfig -> {
            // Find route in agency config, and make sure it exists
            final Route route = agencyConfig.getRoutes().get(routeKey);
            if (route == null) {
                return Observable.error(
                    new IllegalArgumentException("Invalid route tag \"" + routeKey + "\"")
                );
            }

            return Observable.just(route);
        }).flatMap(route -> {
            // Get route's stop tags and construct prediction
            final List<QueryStop> stops = new ArrayList<>();
            for (final String stopTag : route.getStopTags()) {
                stops.add(new QueryStop(routeKey, stopTag));
            }

            // Transform Simple XML translation into Predicitons object used by adapter
            return NextbusAPI.service.predict(stops).map(simpleBody -> {
                final Predictions predictions = parseSimplePredictions(simpleBody, NBItemType.ROUTE);
                Collections.sort(predictions.getPredictions(), (p1, p2) ->
                    route.getStopTags().indexOf(p1.getTag()) - route.getStopTags().indexOf(p2.getTag())
                );
                return predictions;
            });
        });
    }

    /**
     * Get arrival time predictions for every route going through a stop.
     * @param agency Agency (campus) that the stop belongs to.
     * @param stopTitleKey Full title of the stop to get predictions for.
     * @return Promise for list of arrival time predictions.
     */
    public static Observable<Predictions> stopPredict(final String agency, final String stopTitleKey) {
        return configForAgency(agency).flatMap(agencyConfig -> {
            final StopGroup stopsByTitle = agencyConfig.getStopsByTitle().get(stopTitleKey);
            if (stopsByTitle == null) {
                return Observable.error(
                    new IllegalArgumentException("Invalid stop tag \""+stopTitleKey+"\"")
                );
            }

            final List<QueryStop> stops = new ArrayList<>();
            for (final String stopTag : stopsByTitle.getStopTags()) {
                final Stop stop = agencyConfig.getStops().get(stopTag);
                if (stop == null) {
                    return Observable.error(
                        new IllegalArgumentException("Stop tag\"" + stopTag + "\" in stopsByTitle but not stops")
                    );
                }

                for (final String routeTag : stop.getRouteTags()) {
                    stops.add(new QueryStop(routeTag, stopTag));
                }
            }

            return NextbusAPI.service.predict(stops);
        }).map(simpleBody -> {
            final Predictions predictions = parseSimplePredictions(simpleBody, NBItemType.STOP);
            Collections.sort(predictions.getPredictions(), (p1, p2) -> {
                int res = p1.getTitle().compareTo(p2.getTitle());
                if (res == 0) {
                    return p1.getDirection().compareTo(p2.getDirection());
                }
                return res;
            });

            return predictions;
        });
    }

    private enum NBItemType {
        ROUTE, STOP
    }

    private static Predictions parseSimplePredictions(SimpleBody simpleBody, NBItemType itemType) {
        final Predictions predictions = new Predictions(new HashSet<>(), new ArrayList<>());
        for (final SimplePredictions simplePredictions : simpleBody.getPredictions()) {
            final Prediction prediction = itemType == NBItemType.ROUTE
                ? new Prediction(simplePredictions.getStopTitle(), simplePredictions.getStopTag())
                : new Prediction(simplePredictions.getRouteTitle(), simplePredictions.getRouteTag());

            if (simplePredictions.getDirection() != null) {
                for (final SimplePrediction simplePrediction : simplePredictions.getDirection().getPredictions()) {
                    prediction.addMinutes(simplePrediction.getMinutes());
                    prediction.setDirection(simplePredictions.getDirection().getTitle());
                }
            } else {
                prediction.setDirection(simplePredictions.getDirTitleBecauseNoPredictions());
            }

            // remove this if statement to include disabled routes/stops
            if (!prediction.getMinutes().isEmpty()) {
                predictions.getPredictions().add(prediction);
            }

            final SimpleMessage message = simplePredictions.getMessage();
            if (message != null) {
                predictions.getMessages().add(message.getText());
            }
        }

        return predictions;
    }

    /**
     * Get active routes for an agency.
     * @param agency Agency (campus) to get active routes for.
     * @return Promise for list of route stubs (tags and titles).
     */
    public static Observable<List<RouteStub>> getActiveRoutes(final String agency) {
        return activeStopsForAgency(agency).map(ActiveStops::getRoutes);
    }

    /**
     * Get all routes from an agency's configuration.
     * @param agency Agency (campus) to get all routes for.
     * @return Promise for list of route stubs (tags and titles).
     */
    public static Observable<List<RouteStub>> getAllRoutes(final String agency) {
        return configForAgency(agency).map(AgencyConfig::getSortedRoutes);
    }

    /**
     * Get active stops for an agency.
     * @param agency Agency (campus) to active stops for.
     * @return Promise for list of stop stubs (titles and geohashes).
     */
    public static Observable<List<StopStub>> getActiveStops(final String agency) {
        return activeStopsForAgency(agency).map(ActiveStops::getStops);
    }

    /**
     * Get all stops from an agency's configuration.
     * @param agency Agency (campus) to get all stops for.
     * @return Promise for list of stop stubs (titles and geohashes).
     */
    public static Observable<List<StopStub>> getAllStops(final String agency) {
        return configForAgency(agency).map(AgencyConfig::getSortedStops);
    }

    /**
     * Get all bus stops (grouped by title) near a specific location.
     * @param agency Agency (campus) to get stops for.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for list of stop groups (unique stops grouped by title).
     */
    public static Observable<List<StopGroup>> getStopsByTitleNear(final String agency, final double sourceLat, final double sourceLon) {
        return configForAgency(agency).map(agencyConfig -> {
            HashMap<String, StopGroup> stopsByTitle = agencyConfig.getStopsByTitle();
            final List<StopGroup> nearStops = new ArrayList<>();
            for (final StopGroup stopGroup : stopsByTitle.values()) {
                for (final String stopTag : stopGroup.getStopTags()) {
                    final Stop stop = agencyConfig.getStops().get(stopTag);
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
        });
    }

    /**
     * Get all active bus stops (grouped by title) near a specific location.
     * @param agency Agency (campus) to get stops for.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for list of stop groups (unique stops grouped by title).
     */
    public static Observable<List<StopGroup>> getActiveStopsByTitleNear(final String agency, final float sourceLat, final float sourceLon) {
        return getStopsByTitleNear(agency, sourceLat, sourceLon).flatMap(nearbyStops ->
            activeStopsForAgency(agency).map(activeStops -> {
                final List<StopGroup> results = new ArrayList<>();

                for (StopGroup stopGroup : nearbyStops) {
                    String stopTitle = stopGroup.getTitle();

                    // Try to find this stop in list of active stops
                    for (StopStub activeStopStub : activeStops.getStops()) {
                        if (StringUtils.equals(stopTitle, activeStopStub.getTitle())) {
                            results.add(stopGroup); // Found: it's active
                        }
                    }
                }

                return results;
            })
        );
    }
}
