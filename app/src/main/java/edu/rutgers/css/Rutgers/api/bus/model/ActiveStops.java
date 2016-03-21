package edu.rutgers.css.Rutgers.api.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.bus.model.route.RouteStub;
import edu.rutgers.css.Rutgers.api.bus.model.stop.StopStub;
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
