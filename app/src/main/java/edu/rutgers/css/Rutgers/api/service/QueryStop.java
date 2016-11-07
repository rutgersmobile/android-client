package edu.rutgers.css.Rutgers.api.service;

/**
 * Used to serialize stops for Nextbus API request
 */

public final class QueryStop {
    private final String routeKey;
    private final String stopTag;

    public QueryStop(final String routeKey, final String stopTag) {
        this.routeKey = routeKey;
        this.stopTag = stopTag;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public String getStopTag() {
        return stopTag;
    }

    @Override
    public String toString() {
        return routeKey + "|null|" + stopTag;
    }
}
