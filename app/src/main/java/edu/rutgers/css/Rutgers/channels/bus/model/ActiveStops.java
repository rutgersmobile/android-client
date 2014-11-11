package edu.rutgers.css.Rutgers.channels.bus.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus active stop list.
 */
public class ActiveStops {
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
