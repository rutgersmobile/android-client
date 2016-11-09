package edu.rutgers.css.Rutgers.api.model.bus.stop;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;

/**
 * Nextbus stop. Contains stop location and serviced routes.
 */
public final class Stop implements NextbusItem {
    private String title;
    @SerializedName("routes") private final List<String> routeTags;
    @SerializedName("lat") private final String latitude;
    @SerializedName("lon") private final String longitude;
    private final String stopId;
    private String agencyTag; // Not part of Nextbus results

    public Stop(final String title, final List<String> routeTags, final String latitude,
                final String longitude, final String stopId, final String agencyTag) {
        this.title = title;
        this.routeTags = routeTags;
        this.latitude = latitude;
        this.longitude = longitude;
        this.stopId = stopId;
        this.agencyTag = agencyTag;
    }

    /**
     * Readable title for stop
     */
    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * List of all routes this stop appears on
     * @see AgencyConfig#getRoutes()
     */
    public List<String> getRouteTags() {
        return routeTags;
    }

    /**
     * String representation of decimal latitude of stop location
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * String representation of decimal longitude of stop location
     */
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
