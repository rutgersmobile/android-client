package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus stop tags grouped by stop title.
 */
public final class StopGroup {
    private String title;
    @SerializedName("tags") private List<String> stopTags;
    private String geoHash;
    private String agencyTag; // Not part of Nextbus results

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getStopTags() {
        return stopTags;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public String getAgencyTag() {
        return agencyTag;
    }

    void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;
    }
}
