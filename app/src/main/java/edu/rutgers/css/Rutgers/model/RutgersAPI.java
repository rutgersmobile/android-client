package edu.rutgers.css.Rutgers.model;

import android.location.Location;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.RutgersService;
import edu.rutgers.css.Rutgers.api.places.model.Place;
import rx.Observable;

/**
 * Static API services based on app Config.java
 */

public final class RutgersAPI {
    public static final RutgersService service = RutgersApplication.retrofit
        .create(RutgersService.class);

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

    public static Observable<Places> getPlaces() {
        return service.getPlacesMap().map(kvHolder -> {
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
}
