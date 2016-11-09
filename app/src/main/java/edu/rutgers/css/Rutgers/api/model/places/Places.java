package edu.rutgers.css.Rutgers.api.model.places;

import java.util.Map;

/**
 * Created by mattro on 11/8/16.
 */
public class Places {
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

