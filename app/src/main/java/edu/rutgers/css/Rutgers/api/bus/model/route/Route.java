package edu.rutgers.css.Rutgers.api.bus.model.route;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getStopTags() {
        return stopTags;
    }

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
