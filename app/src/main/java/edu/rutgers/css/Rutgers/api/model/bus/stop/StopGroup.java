package edu.rutgers.css.Rutgers.api.model.bus.stop;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;

/**
 * Nextbus stop tags grouped by stop title.
 */
public final class StopGroup implements NextbusItem {
    private String title;
    @SerializedName("tags") private final List<String> stopTags;
    private final String geoHash;
    private String agencyTag; // Not part of Nextbus results

    public StopGroup(final String title, final List<String> stopTags, final String geoHash, final String agencyTag) {
        this.title = title;
        this.stopTags = stopTags;
        this.geoHash = geoHash;
        this.agencyTag = agencyTag;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getStopTags() {
        return stopTags;
    }

    public String getGeoHash() {
        return geoHash;
    }

    @Override
    public String getAgencyTag() {
        return agencyTag;
    }

    public void setAgencyTag(String agencyTag) {
        this.agencyTag = agencyTag;
    }

    @Override
    public String getTag() {
        return getTitle();
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
