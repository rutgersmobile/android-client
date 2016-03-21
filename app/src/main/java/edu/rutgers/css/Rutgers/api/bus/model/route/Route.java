package edu.rutgers.css.Rutgers.api.bus.model.route;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

/**
 * Nextbus route. Contains list of stops on the route.
 */
@Data
public final class Route {
    private String title;
    @SerializedName("stops") private final List<String> stopTags;
    private String agencyTag; // Not part of Nextbus results

    @Override
    public String toString() {
        return getTitle();
    }
}
