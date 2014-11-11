package edu.rutgers.css.Rutgers.channels.bus.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus stop. Contains stop location and serviced routes.
 */
public class Stop {
    private String title;
    @SerializedName("routes") private List<String> routeTags;
    @SerializedName("lat") private String latitude;
    @SerializedName("lon") private String longitude;
    private String stopId;

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRouteTags() {
        return routeTags;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getStopId() {
        return stopId;
    }
}
