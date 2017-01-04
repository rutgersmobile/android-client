package edu.rutgers.css.Rutgers.api;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.rutgers.css.Rutgers.api.model.bus.ActiveStops;
import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
import edu.rutgers.css.Rutgers.api.model.bus.Prediction;
import edu.rutgers.css.Rutgers.api.model.bus.Predictions;
import edu.rutgers.css.Rutgers.api.model.bus.SimpleBody;
import edu.rutgers.css.Rutgers.api.model.bus.SimpleMessage;
import edu.rutgers.css.Rutgers.api.model.bus.SimplePrediction;
import edu.rutgers.css.Rutgers.api.model.bus.SimplePredictions;
import edu.rutgers.css.Rutgers.api.model.bus.route.Route;
import edu.rutgers.css.Rutgers.api.model.bus.route.RouteStub;
import edu.rutgers.css.Rutgers.api.model.bus.stop.Stop;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopStub;
import edu.rutgers.css.Rutgers.api.service.NextbusService;
import edu.rutgers.css.Rutgers.api.service.QueryStop;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import rx.Observable;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/**
 * Provides static methods for access to the Nextbus API.
 * Uses nextbusjs data to create requests against the official Nextbus API.
 */
public final class NextbusAPI {

    private static final String TAG = "NextbusAPI";

    public static final String AGENCY_NB = "nb";
    public static final String AGENCY_NWK = "nwk";
    public static final String AGENCY_RUTGERS = "rutgers";
    private static final String NOT_SET_UP_MESSAGE = "Set up service with NextbusAPI::setService or NextbusAPI::simpleSetup";

    /** This class only contains static utility methods. */
    private NextbusAPI() {}

    private static NextbusService service;

    /**
     * Set the retrofit service that will be used to make requests
     * @param service A retrofit service for making calls
     */
    public static void setService(NextbusService service) {
        NextbusAPI.service = service;
    }

    /**
     * Get the service previously set. You probably don't want to use this. Instead just call the
     * other static, public methods in the class
     * @return Service for making Rutgers API calls
     */
    public static NextbusService getService() {
        if (service == null) {
            throw new IllegalStateException(NOT_SET_UP_MESSAGE);
        }

        return service;
    }

    /**
     * An easy way to set up the API for making calls. This will correctly configure the service
     * before using it.
     * @param client You should use the same client for all APIs. This will do actual HTTP work.
     * @param apiBase Base url for the api. Probably something like "http://webservices.nextbus.com/"
     */
    public static void simpleSetup(OkHttpClient client, String apiBase) {
        if (service == null) {
            final Retrofit nbRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .client(client)
                .baseUrl(apiBase)
                .build();

            NextbusAPI.setService(nbRetrofit.create(NextbusService.class));
        }
    }

    public static Observable<SimpleBody> predict(List<QueryStop> stops) {
        return getService().predict(stops);
    }

    /**
     * Returns an agency config for the given agency
     * @param agency An agency string like "nb" or "nwk"
     * @return All route and stop information for the given agency
     */
    public static Observable<AgencyConfig> configForAgency(final String agency) {
        return AGENCY_NB.equals(agency) || AGENCY_RUTGERS.equals(agency)
            ? RutgersAPI.getNewBrunswickAgencyConfig()
            : RutgersAPI.getNewarkAgencyConfig();
    }

    /**
     * Get stub information for currently active stops
     * @param agency An agency string like "nb" or "nwk"
     * @return Stub information for active stops
     */
    public static Observable<ActiveStops> activeStopsForAgency(final String agency) {
        return (AGENCY_NB.equals(agency) || AGENCY_RUTGERS.equals(agency)
            ? RutgersAPI.getNewBrunswickActiveStops()
            : RutgersAPI.getNewarkActiveStops()).map(activeStops -> {
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
            return predict(stops).map(simpleBody -> {
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

            return predict(stops);
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
    public static Observable<List<StopGroup>> getStopsByTitleNear(final String agency, final double sourceLat, final double sourceLon, final double range) {
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
                    double distance = APIUtils.distanceBetween(sourceLat, sourceLon, stopLat, stopLon);

                    if (distance < range) {
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
    public static Observable<List<StopGroup>> getActiveStopsByTitleNear(final String agency, final float sourceLat, final float sourceLon, final double range) {
        return getStopsByTitleNear(agency, sourceLat, sourceLon, range).flatMap(nearbyStops ->
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
