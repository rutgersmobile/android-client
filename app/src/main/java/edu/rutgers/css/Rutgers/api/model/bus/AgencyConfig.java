package edu.rutgers.css.Rutgers.api.model.bus;

import java.util.HashMap;
import java.util.List;

import edu.rutgers.css.Rutgers.api.model.bus.route.Route;
import edu.rutgers.css.Rutgers.api.model.bus.route.RouteStub;
import edu.rutgers.css.Rutgers.api.model.bus.stop.Stop;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.model.bus.stop.StopStub;

/**
 * Nextbus agency configuration.
 */
public final class AgencyConfig {
    private final HashMap<String, Route> routes;
    private final HashMap<String, Stop> stops;
    private final HashMap<String, StopGroup> stopsByTitle;
    private final List<StopStub> sortedStops;
    private final List<RouteStub> sortedRoutes;
    private final String agencyTag; // Not part of Nextbus results

    public AgencyConfig(final HashMap<String, Route> routes, final HashMap<String, Stop> stops,
                        final HashMap<String, StopGroup> stopsByTitle, final List<StopStub> sortedStops,
                        final List<RouteStub> sortedRoutes, final String agencyTag) {
        this.routes = routes;
        this.stops = stops;
        this.stopsByTitle = stopsByTitle;
        this.sortedStops = sortedStops;
        this.sortedRoutes = sortedRoutes;
        this.agencyTag = agencyTag;
    }

    /**
     * Get mapping of Route tags to route objects
     */
    public HashMap<String, Route> getRoutes() {
        return routes;
    }

    /**
     * Get mapping of Stop tags to stop objects
     */
    public HashMap<String, Stop> getStops() {
        return stops;
    }

    /**
     * Mapping of titles to a list of stops
     */
    public HashMap<String, StopGroup> getStopsByTitle() {
        return stopsByTitle;
    }

    /**
     * All stops in sorted order
     */
    public List<StopStub> getSortedStops() {
        return sortedStops;
    }

    /**
     * All routes in sorted order
     */
    public List<RouteStub> getSortedRoutes() {
        return sortedRoutes;
    }

    /**
     * Nextbus agency tag. Probably "rutgers"
     */
    public String getAgencyTag() {
        return agencyTag;
    }
}
