package edu.rutgers.css.Rutgers.channels.bus.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus stop tags grouped by stop title.
 */
public class StopGroup {
    private String title;
    @SerializedName("tags") private List<String> stopTags;
    private String geoHash;

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
}
