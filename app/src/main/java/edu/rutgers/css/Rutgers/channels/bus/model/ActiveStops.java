package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

/**
 * Nextbus active stop list.
 */
@Data
public final class ActiveStops {
    private final List<RouteStub> routes;
    private final List<StopStub> stops;
    @SerializedName("time") private final long timestamp;
    private String agencyTag; // Not part of Nextbus results

    /**
     * Agency is not returned by the api, however it is required by the ActiveStops class.
     * Gson parses the api info into this object before it is used to initialize ActiveStops.
     */
    @Data
    public class AgentlessActiveStops {
        private final List<RouteStub> routes;
        private final List<StopStub> stops;
        @SerializedName("time") private final long timestamp;
    }

    public ActiveStops(@NonNull String agencyTag, @NonNull AgentlessActiveStops activeStops) {
        setAgencyTag(agencyTag);

        this.routes = activeStops.getRoutes();
        this.stops = activeStops.getStops();
        this.timestamp = activeStops.getTimestamp();

        // Set the agency on each route and stop
        for (RouteStub routeStub: routes) routeStub.setAgencyTag(getAgencyTag());
        for (StopStub stopStub: stops) stopStub.setAgencyTag(getAgencyTag());
    }
}
