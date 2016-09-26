package edu.rutgers.css.Rutgers.api.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;

/**
 * Nextbus active stop list.
 */
public final class ActiveStops {
    private final List<RouteStub> routes;
    private final List<StopStub> stops;
    @SerializedName("time") private final long timestamp;
    private String agencyTag; // Not part of Nextbus results

    public ActiveStops(final List<RouteStub> routes, final List<StopStub> stops, final long timestamp,
                       final String agencyTag) {
        this.routes = routes;
        this.stops = stops;
        this.timestamp = timestamp;
        this.agencyTag = agencyTag;
    }

    public List<RouteStub> getRoutes() {
        return routes;
    }

    public List<StopStub> getStops() {
        return stops;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAgencyTag() {
        return agencyTag;
    }

    /**
     * Update the agency tag on this ActiveStops container
     * and all route and stop information contained within.
     */
    public void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;

        for (RouteStub routeStub: routes) {
            routeStub.setAgencyTag(agencyTag);
        }

        for (StopStub stopStub: stops) {
            stopStub.setAgencyTag(agencyTag);
        }
    }
}
