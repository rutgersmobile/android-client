package edu.rutgers.css.Rutgers.api.places;

import android.support.annotation.NonNull;

import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.rutgers.css.Rutgers.api.ApiRequest;
import edu.rutgers.css.Rutgers.api.places.model.Place;

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

    public static final class KVHolder {
        public HashMap<String, Place> all;
        public Lunr lunr;

        private KVHolder() { }

        public static final class Lunr {
            public DocStore documentStore;

            private Lunr() { }

            public static final class DocStore {
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
}
