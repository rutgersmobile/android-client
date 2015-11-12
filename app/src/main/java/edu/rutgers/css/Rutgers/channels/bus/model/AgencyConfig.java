package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;

/**
 * Nextbus agency configuration.
 */
public final class AgencyConfig {
    private HashMap<String, Route> routes;
    private HashMap<String, Stop> stops;
    private HashMap<String, StopGroup> stopsByTitle;
    private List<StopStub> sortedStops;
    private List<RouteStub> sortedRoutes;
    private String agencyTag; // Not part of Nextbus results

    public AgencyConfig(@NonNull String agencyTag, @NonNull HashMap<String, Route> routes,
                        @NonNull HashMap<String, Stop> stops, @NonNull HashMap<String, StopGroup> stopsByTitle,
                        @NonNull List<StopStub> sortedStops, @NonNull List<RouteStub> sortedRoutes) {
        setAgencyTag(agencyTag);
        this.routes = routes;
        this.stops = stops;
        this.stopsByTitle = stopsByTitle;
        this.sortedStops = sortedStops;
        this.sortedRoutes = sortedRoutes;
    }

    public HashMap<String, Route> getRoutes() {
        return routes;
    }

    public HashMap<String, Stop> getStops() {
        return stops;
    }

    public HashMap<String, StopGroup> getStopsByTitle() {
        return stopsByTitle;
    }

    public List<StopStub> getSortedStops() {
        return sortedStops;
    }

    public List<RouteStub> getSortedRoutes() {
        return sortedRoutes;
    }

    public String getAgencyTag() {
        return agencyTag;
    }

    private void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;
    }
}
