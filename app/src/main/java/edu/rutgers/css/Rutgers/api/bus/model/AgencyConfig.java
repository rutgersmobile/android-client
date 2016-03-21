package edu.rutgers.css.Rutgers.api.bus.model;

import java.util.HashMap;
import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.model.route.Route;
import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopGroup;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.Stop;
import lombok.Data;

/**
 * Nextbus agency configuration.
 */
@Data
public final class AgencyConfig {
    private final HashMap<String, Route> routes;
    private final HashMap<String, Stop> stops;
    private final HashMap<String, StopGroup> stopsByTitle;
    private final List<StopStub> sortedStops;
    private final List<RouteStub> sortedRoutes;
    private final String agencyTag; // Not part of Nextbus results
}
