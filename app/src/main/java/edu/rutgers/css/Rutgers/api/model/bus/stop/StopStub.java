package edu.rutgers.css.Rutgers.api.model.bus.stop;

import android.support.annotation.NonNull;

import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;

/**
 * Nextbus stop stub.
 */
public final class StopStub implements NextbusItem {
    private String title;
    private final String geoHash;
    private String agencyTag; // Not part of Nextbus results

    /** Create a stop stub from a stop group. */
    public StopStub(@NonNull StopGroup stopGroup) {
        this.title = stopGroup.getTitle();
        this.geoHash = stopGroup.getGeoHash();
        this.agencyTag = stopGroup.getAgencyTag();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
