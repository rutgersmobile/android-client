package edu.rutgers.css.Rutgers.channels.places.model;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.api.ApiRequest;

/**
 * Provides access to the Places database.
 * @author James Chambers
 */
public final class PlacesAPI {
    
    private static final String TAG = "PlacesAPI";

    /** Map of Place keys to Place objects. Initialized in {@link #setup()}. */
    private static Map<String, Place> sPlaces;
    private static Map<String, Place> sTokens;

    private PlacesAPI() {}

    private static final class KVHolder {
        public HashMap<String, Place> all;
        public Lunr lunr;

        private KVHolder() { }

        private static final class Lunr {
            public DocStore documentStore;

            private Lunr() { }

            private static final class DocStore {
                public HashMap<String, List<String>> store;

                private DocStore() { }
            }
        }
    }

    /**
     * Grab the places flat-file from the API and convert it into a map.
     */
    private synchronized static void setup() throws JsonSyntaxException, IOException {
        if (sPlaces != null) return;

        sPlaces = new HashMap<>(1300);
        sTokens = new HashMap<>(1300);

        KVHolder holder = ApiRequest.api("places.txt", 1, TimeUnit.DAYS, KVHolder.class);
        sPlaces = holder.all;
        for (String key : holder.lunr.documentStore.store.keySet()) {
            Place place = sPlaces.get(key);
            if (place != null) {
                List<String> tokens = holder.lunr.documentStore.store.get(key);
                for (String token : tokens) {
                    sTokens.put(token, place);
                }
            }
        }
    }

    /**
     * Get a specific place from the Places API.
     * @param placeKey Key for place entry, returned from search results
     * @return The place object that has the given key, or null if it does not exist
     */
    public static synchronized Place getPlace(@NonNull final String placeKey) throws JsonSyntaxException, IOException {
        setup();
        return sPlaces.get(placeKey);
    }

    /**
     * Search for places near a given location.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return List of places that are near the given location.
     */
    public static synchronized List<Place> getPlacesNear(final double sourceLat, final double sourceLon) throws JsonSyntaxException, IOException {
        setup();

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

        return results;
    }

    /**
     * Search places by Lunr index tokens and title. Limit maxmimum number of results.
     * @param query Query string
     * @param maxResults Maximum number of results to return. A non-positive value disables the cap.
     * @return List of place objects that match the query string.
     */
    public static synchronized List<Place> searchPlaces(@NonNull final String query, final int maxResults)
            throws JsonSyntaxException, IOException {
        setup();

        List<Place> results = new ArrayList<>();

        // Check if this query matches a lunr token
        Place tokenMatch = sTokens.get(query.toLowerCase(Locale.US));
        if (tokenMatch != null) {
            results.add(tokenMatch);
        }

        // See if the query is a substring of any of the place titles
        for (Place place : sPlaces.values()) {
            if (place == tokenMatch) continue;

            if (StringUtils.containsIgnoreCase(place.getTitle(), query)) {
                results.add(place);

                // Use maxResults as the cap if it's positive
                if (maxResults > 0 && results.size() >= maxResults) break;
            }
        }

        return results;
    }

    /**
     * Search places by Lunr index tokens and title, with no cap on the number of results.
     * @param query Query string
     * @return List of place objects that match the query string.
     */
    public static synchronized List<Place> searchPlaces(@NonNull final String query)
            throws JsonSyntaxException, IOException, IllegalArgumentException  {
        return searchPlaces(query, 0);
    }

}
