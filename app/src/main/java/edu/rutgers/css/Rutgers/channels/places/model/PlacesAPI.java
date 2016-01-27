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

    private static boolean sSettingUp;

    /** Map of Place keys to Place objects. Initialized in {@link #setup()}. */
    private static Map<String, Place> sPlaces;
    private static Map<String, Place> sTokens;

    private PlacesAPI() {}

    private class KVHolder {
        public HashMap<String, Place> all;
        public Lunr lunr;

        private KVHolder() { }

        private class Lunr {
            public DocStore documentStore;

            private Lunr() { }

            private class DocStore {
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
     * @return Promise for a Place object representing the entry in the database
     */
    public static synchronized Place getPlace(@NonNull final String placeKey) throws JsonSyntaxException, IOException {
        setup();
        return sPlaces.get(placeKey);
    }

    /**
     * Search for places near a given location.
     * @param sourceLat Latitude
     * @param sourceLon Longitude
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
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
     * <p>Search places by title.</p>
     * @param query Query string
     * @return Promise for a list of results as key-value pairs, with the place ID as key and name as value.
     */
    public static synchronized List<Place> searchPlaces(final String query) throws JsonSyntaxException, IOException {
        setup();

        List<Place> results = new ArrayList<>();

        Place p = sTokens.get(query.toLowerCase(Locale.US));
        if (p != null) {
            results.add(p);
        }

        // Split each place title up into individual words and see if the query is a prefix of any of them
        for (Place place : sPlaces.values()) {
            String parts[] = StringUtils.split(place.getTitle(), ' ');
            boolean found = false;
            for (String part : parts) {
                if (StringUtils.startsWithIgnoreCase(part, query)) {
                    // The check makes sure we don't duplicate the special token match 'p'
                    found = true;
                    if (place != p) results.add(place);
                    break;
                }
            }
            if (!found && StringUtils.containsIgnoreCase(place.getTitle(), query) && place != p) {
                results.add(place);
            }
        }

        return results;
    }
}
