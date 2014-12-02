package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Nextbus stop. Contains stop location and serviced routes.
 */
public final class Stop implements NextbusItem {
    private String title;
    @SerializedName("routes") private List<String> routeTags;
    @SerializedName("lat") private String latitude;
    @SerializedName("lon") private String longitude;
    private String stopId;
    private String agencyTag; // Not part of Nextbus results

    protected void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getTag() {
        return getTitle();
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

    @Override
    public String getAgencyTag() {
        return agencyTag;
    }

    void setAgencyTag(@NonNull String agencyTag) {
        this.agencyTag = agencyTag;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
