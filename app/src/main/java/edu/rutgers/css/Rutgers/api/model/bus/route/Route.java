package edu.rutgers.css.Rutgers.api.model.bus.route;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;

/**
 * Nextbus route. Contains list of stops on the route.
 */
public final class Route {
    private String title;
    @SerializedName("stops") private final List<String> stopTags;
    private String agencyTag; // Not part of Nextbus results

    public Route(final String title, final List<String> stopTags, final String agencyTag) {
        this.title = title;
        this.stopTags = stopTags;
        this.agencyTag = agencyTag;
    }

    /**
     * Readable title representing route
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * List of stops in this route
     * @return Strings representing stop tags
     * @see AgencyConfig#getStops()
     */
    public List<String> getStopTags() {
        return stopTags;
    }

    /**
     * Nextbus agency that contains this route. Most likely "rutgers".
     */
    public String getAgencyTag() {
        return agencyTag;
    }

    public void setAgencyTag(String agencyTag) {
        this.agencyTag = agencyTag;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
