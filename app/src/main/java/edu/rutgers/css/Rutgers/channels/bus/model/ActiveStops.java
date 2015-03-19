package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Nextbus active stop list.
 */
public final class ActiveStops {
    private List<RouteStub> routes;
    private List<StopStub> stops;
    @SerializedName("time") private long timestamp;
    private String agencyTag; // Not part of Nextbus results

    public ActiveStops(@NonNull String agencyTag, @NonNull JSONObject activeStopsJson) throws JSONException, JsonSyntaxException {
        setAgencyTag(agencyTag);

        Gson gson = new Gson();
        ActiveStops temp = gson.fromJson(activeStopsJson.toString(), ActiveStops.class);
        this.routes = temp.getRoutes();
        this.stops = temp.getStops();
        this.timestamp = temp.getTimestamp();

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
