package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus active stop list.
 */
public final class ActiveStops {
    private List<RouteStub> routes;
    private List<StopStub> stops;
    @SerializedName("time") private long timestamp;
    private String agencyTag; // Not part of Nextbus results

    public class AgentlessActiveStops {
        private List<RouteStub> routes;
        private List<StopStub> stops;
        @SerializedName("time") private long timestamp;

        public List<RouteStub> getRoutes() {
            return routes;
        }

        public List<StopStub> getStops() {
            return stops;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public ActiveStops(@NonNull String agencyTag, @NonNull AgentlessActiveStops activeStops) {
        setAgencyTag(agencyTag);

        this.routes = activeStops.getRoutes();
        this.stops = activeStops.getStops();
        this.timestamp = activeStops.getTimestamp();

        for (RouteStub routeStub: routes) routeStub.setAgencyTag(getAgencyTag());
        for (StopStub stopStub: stops) stopStub.setAgencyTag(getAgencyTag());
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

    private void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;
    }
}
