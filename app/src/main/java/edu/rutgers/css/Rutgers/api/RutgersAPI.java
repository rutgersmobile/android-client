package edu.rutgers.css.Rutgers.api;

import android.location.Location;

import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.model.athletics.AthleticsGames;
import edu.rutgers.css.Rutgers.api.model.bus.ActiveStops;
import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
import edu.rutgers.css.Rutgers.api.model.bus.parsers.AgencyConfigDeserializer;
import edu.rutgers.css.Rutgers.api.model.cinema.Movie;
import edu.rutgers.css.Rutgers.api.model.food.DiningMenu;
import edu.rutgers.css.Rutgers.api.model.places.KVHolder;
import edu.rutgers.css.Rutgers.api.model.places.Place;
import edu.rutgers.css.Rutgers.api.model.soc.SOCIndex;
import edu.rutgers.css.Rutgers.api.model.soc.Semesters;
import edu.rutgers.css.Rutgers.api.model.soc.parsers.SOCIndexDeserializer;
import edu.rutgers.css.Rutgers.api.service.RutgersService;
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

    public static void setService(RutgersService service) {
        RutgersAPI.service = service;
    }

    public static RutgersService getService() {
        if (service == null) {
            throw new IllegalStateException(NOT_SET_UP_MESSAGE);
        }

        return service;
    }

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

    public static Observable<AthleticsGames> getAthleticsGames(String sport) {
        return getService().getGames(sport);
    }

    public static Observable<List<DiningMenu>> getDiningHalls() {
        return getService().getDiningHalls();
    }

    public static Observable<List<Movie>> getMovies() {
        return getService().getMovies();
    }

    public static Observable<ActiveStops> getNewBrunswickActiveStops() {
        return getService().getNewBrunswickActiveStops();
    }

    public static Observable<ActiveStops> getNewarkActiveStops() {
        return getService().getNewarkActiveStops();
    }

    public static Observable<AgencyConfig> getNewBrunswickAgencyConfig() {
        return getService().getNewBrunswickAgencyConfig();
    }

    public static Observable<AgencyConfig> getNewarkAgencyConfig() {
        return getService().getNewarkAgencyConfig();
    }

    public static class Places {
        private final Map<String, Place> places;
        private final Map<String, Place> tokens;

        public Places(final Map<String, Place> places, final Map<String, Place> tokens) {
            this.places = places;
            this.tokens = tokens;
        }

        public Map<String, Place> getPlaces() {
            return places;
        }

        public Map<String, Place> getTokens() {
            return tokens;
        }
    }

    public static Observable<KVHolder> getPlacesMap() {
        return getService().getPlacesMap();
    }

    public static Observable<Places> getPlaces() {
        return getService().getPlacesMap().map(kvHolder -> {
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

    public static Observable<Place> getPlace(final String placeKey) {
        return getPlaces().map(places -> places.getPlaces().get(placeKey));
    }

    public static Observable<List<Place>> getPlacesNear(final double sourceLat, final double sourceLon) {
        return getPlaces().map(places -> {
            final List<Place> results = new ArrayList<>();
            List<AbstractMap.SimpleEntry<Float, Place>> nearbyPlaces = new ArrayList<>();

            for (Place place : places.getPlaces().values()) {
                if (place.getLocation() == null) continue;
                final double placeLat = place.getLocation().getLatitude();
                final double placeLon = place.getLocation().getLongitude();
                float dist[] = new float[1];
                Location.distanceBetween(placeLat, placeLon, sourceLat, sourceLon, dist);
                if (dist[0] <= Config.NEARBY_RANGE)
                    nearbyPlaces.add(new AbstractMap.SimpleEntry<>(dist[0], place));
            }

            Collections.sort(nearbyPlaces, (left, right) -> left.getKey().compareTo(right.getKey()));

            for (AbstractMap.SimpleEntry<Float, Place> entry : nearbyPlaces) {
                results.add(entry.getValue());
            }

            return results;
        });
    }

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

    public static Observable<Semesters> getSemesters() {
        return getService().getSemesters();
    }

    public static Observable<SOCIndex> getSOCIndex(String semester, String campus, String level) {
        return getService().getSOCIndex(semester, campus, level);
    }
}
