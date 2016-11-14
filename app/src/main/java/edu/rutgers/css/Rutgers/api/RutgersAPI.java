package edu.rutgers.css.Rutgers.api;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.api.model.athletics.AthleticsGames;
import edu.rutgers.css.Rutgers.api.model.bus.ActiveStops;
import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
import edu.rutgers.css.Rutgers.api.model.bus.parsers.AgencyConfigDeserializer;
import edu.rutgers.css.Rutgers.api.model.cinema.Movie;
import edu.rutgers.css.Rutgers.api.model.food.DiningMenu;
import edu.rutgers.css.Rutgers.api.model.places.KVHolder;
import edu.rutgers.css.Rutgers.api.model.places.Place;
import edu.rutgers.css.Rutgers.api.model.places.Places;
import edu.rutgers.css.Rutgers.api.model.soc.SOCIndex;
import edu.rutgers.css.Rutgers.api.model.soc.Semesters;
import edu.rutgers.css.Rutgers.api.model.soc.parsers.SOCIndexDeserializer;
import edu.rutgers.css.Rutgers.api.service.RutgersService;
import edu.rutgers.css.Rutgers.api.model.Motd;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Static API services based on app Config.java
 */
public final class RutgersAPI {

    private RutgersAPI() {}

    private static RutgersService service;

    private static final String NOT_SET_UP_MESSAGE = "Set up service with RutgersAPI::setService or RutgersAPI::simpleSetup";

    /**
     * Set the retrofit service that will be used to make requests
     * @param service A retrofit service for making calls
     */
    public static void setService(RutgersService service) {
        RutgersAPI.service = service;
    }

    /**
     * Get the service previously set. You probably don't want to use this. Instead just call the
     * other static, public methods in the class
     * @return Service for making Rutgers API calls
     */
    public static RutgersService getService() {
        if (service == null) {
            throw new IllegalStateException(NOT_SET_UP_MESSAGE);
        }

        return service;
    }

    /**
     * An easy way to set up the API for making calls. This will correctly configure the service
     * before using it.
     * @param client You should use the same client for all APIs. This will do actual HTTP work.
     * @param apiBase Base url for the api. Probably something like "https://rumobile.rutgers.edu/2"
     */
    public static void simpleSetup(OkHttpClient client, String apiBase) {
        final Retrofit retrofit = new Retrofit.Builder()
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(
                new GsonBuilder()
                    .registerTypeAdapter(AgencyConfig.class, new AgencyConfigDeserializer())
                    .registerTypeAdapter(SOCIndex.class, new SOCIndexDeserializer())
                    .create()
            ))
            .client(client)
            .baseUrl(apiBase)
            .build();

        RutgersAPI.service = retrofit.create(RutgersService.class);
    }

    /**
     * Get a list of games scheduled for a sport
     * @param sport ID of a sport. Like "m_soccer"
     * @return An object that wraps a list of games. It contains information like times and scores.
     */
    public static Observable<AthleticsGames> getAthleticsGames(String sport) {
        return getService().getGames(sport);
    }

    /**
     * Get current menus for all New Brunswick dining halls
     * @return A list where each element is a specific dining hall with its meals.
     */
    public static Observable<List<DiningMenu>> getDiningHalls() {
        return getService().getDiningHalls();
    }

    /**
     * Get all movies and showtimes for the Rutgers Cinema
     * @return A list that contains information about every currently showing film
     */
    public static Observable<List<Movie>> getMovies() {
        return getService().getMovies();
    }

    /**
     * Get the bus stops and routes that are currently running on the New Brunswick campus. You must
     * use the {@link NextbusAPI#predict(List)} method to get predicted times. This only provides
     * stub information. To get full information like all stops in a route, use {@link RutgersAPI#getNewBrunswickAgencyConfig()}.
     * @return Active New Brunswick stops and routes
     */
    public static Observable<ActiveStops> getNewBrunswickActiveStops() {
        return getService().getNewBrunswickActiveStops();
    }

    /**
     * Get the bus stops and routes that are currently running on the Newark campus. You must use
     * the {@link NextbusAPI#predict(List)} method to get predicted times. This only provides stub
     * information. To get full information like all stops in a route, use {@link RutgersAPI#getNewarkAgencyConfig()}.
     * @return Active Newark stops and routes
     */
    public static Observable<ActiveStops> getNewarkActiveStops() {
        return getService().getNewarkActiveStops();
    }

    /**
     * Get full configuration of New Brunswick stops and routes
     * @return Config object with mappings from stops to their routes and vice versa
     */
    public static Observable<AgencyConfig> getNewBrunswickAgencyConfig() {
        return getService().getNewBrunswickAgencyConfig();
    }

    /**
     * Get full configuration of Newark stops and routes
     * @return Config object with mappings from stops to their routes and vice versa
     */
    public static Observable<AgencyConfig> getNewarkAgencyConfig() {
        return getService().getNewarkAgencyConfig();
    }

    /**
     * Get basic information about Rutgers places. This is probably not you want, use
     * {@link RutgersAPI#getPlaces()}
     * @return Map of of strings to place objects and a Lunr store for autocompletion
     */
    public static Observable<KVHolder> getPlacesMap() {
        return getService().getPlacesMap();
    }

    /**
     * Get all Rutgers places
     * @return A Map of place names to places, and tokens for autocompletion
     */
    public static Observable<Places> getPlaces() {
        return getPlacesMap().map(kvHolder -> {
            final Map<String, Place> places = kvHolder.all;
            final Map<String, Place> tokens = new HashMap<>();
            for (final String key : kvHolder.lunr.documentStore.store.keySet()) {
                final Place place = places.get(key);
                if (place != null) {
                    final List<String> tokenList = kvHolder.lunr.documentStore.store.get(key);
                    for (final String token : tokenList) {
                        tokens.put(token, place);
                    }
                }
            }

            return new Places(places, tokens);
        });
    }

    /**
     * Lookup a specific place in the places store
     * @param placeKey Combination of a Rutgers place ID and the name. Look up from {@link RutgersAPI#getPlaces()}
     * @return A single Rutgers place
     */
    public static Observable<Place> getPlace(final String placeKey) {
        return getPlaces().map(places -> places.getPlaces().get(placeKey));
    }

    /**
     * Lookup all places near a latitude/longitude
     * @param sourceLat Latitude to search near
     * @param sourceLon Longitude to search near
     * @param range Radius to search in meters
     * @return A list of places located inside the circle provided by the arguments
     */
    public static Observable<List<Place>> getPlacesNear(final double sourceLat, final double sourceLon, final float range) {
        return getPlaces().map(places -> {
            final List<Place> results = new ArrayList<>();
            List<AbstractMap.SimpleEntry<Float, Place>> nearbyPlaces = new ArrayList<>();

            for (Place place : places.getPlaces().values()) {
                if (place.getLocation() == null) continue;
                final double placeLat = place.getLocation().getLatitude();
                final double placeLon = place.getLocation().getLongitude();
                final double dist = distanceBetween(placeLat, placeLon, sourceLat, sourceLon);
                if (dist <= range)
                    nearbyPlaces.add(new AbstractMap.SimpleEntry<>((float)dist, place));
            }

            Collections.sort(nearbyPlaces, (left, right) -> left.getKey().compareTo(right.getKey()));

            for (AbstractMap.SimpleEntry<Float, Place> entry : nearbyPlaces) {
                results.add(entry.getValue());
            }

            return results;
        });
    }

    /**
     * Calculate the distance between to points on the earths surface
     * @param lat0 Latitude of first point in degrees
     * @param lon0 Longitude of first point in degrees
     * @param lat1 Latitude of second point in degrees
     * @param lon1 Longitude of second point in degrees
     * @return Distance between points in meters
     */
    public static double distanceBetween(double lat0, double lon0, double lat1, double lon1) {
        final double dlat = degreesToRadians(lat1 - lat0);
        final double dlon = degreesToRadians(lon1 - lon0);
        final double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
            + Math.cos(degreesToRadians(lat0)) * Math.cos(degreesToRadians(lat1))
            * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371000 * c;
    }

    private static double degreesToRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /**
     * Search places using tokens from the places map. Also checks substrings of place names
     * @param query The token/substring to search for
     * @param maxResults The maximum number of results that should be returned. Use -1 for unbounded.
     * @return A list of places that were found that match
     */
    public static Observable<List<Place>> searchPlaces(final String query, final int maxResults) {
        return getPlaces().map(places -> {
            List<Place> results = new ArrayList<>();

            // Check if this query matches a lunr token
            Place tokenMatch = places.getTokens().get(query.toLowerCase(Locale.US));
            if (tokenMatch != null) {
                results.add(tokenMatch);
            }

            // See if the query is a substring of any of the place titles
            for (Place place : places.getPlaces().values()) {
                if (place == tokenMatch) continue;

                if (StringUtils.containsIgnoreCase(place.getTitle(), query)) {
                    results.add(place);

                    // Use maxResults as the cap if it's positive
                    if (maxResults > 0 && results.size() >= maxResults) break;
                }
            }

            return results;
        });
    }

    /**
     * Get current semesters that can be queried from the {@link SOCAPI}
     * @return List of semesters, ex. "092016"
     */
    public static Observable<Semesters> getSemesters() {
        return getService().getSemesters();
    }

    /**
     * Get the index for classes for looking up subjects and courses by name and abbreviations
     * @param semester A semester string from {@link RutgersAPI#getSemesters()}
     * @param campus A campus code like "NB", "CM", "NWK", etc.
     * @param level Graduate ("G") or undergraduate ("U")
     * @return {@link SOCIndex} object
     */
    public static Observable<SOCIndex> getSOCIndex(String semester, String campus, String level) {
        return getService().getSOCIndex(semester, campus, level);
    }

    /**
     * Get the message of the day.
     * @return An object that may contain information to display to the user. This is also used
     * on deprecated APIs that we are removing to lock the user out of old app versions.
     */
    public static Observable<Motd> getMotd() {
        return getService().getMotd();
    }

    /**
     * Get drawer listing
     * @return Json array where each element represents an element in the app drawer
     */
    public static Observable<JsonArray> getOrderedContent() {
        return getService().getOrderedContent();
    }
}
