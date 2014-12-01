package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

/**
 * Nextbus stop stub.
 */
public final class StopStub {
    private String title;
    private String geoHash;
    private String agencyTag; // Not part of Nextbus results

    /** Create a stop stub from a stop group. */
    public StopStub(@NonNull StopGroup stopGroup) {
        this.title = stopGroup.getTitle();
        this.geoHash = stopGroup.getGeoHash();
        this.agencyTag = stopGroup.getAgencyTag();
    }

    public String getTitle() {
        return title;
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

    @Override
    public String toString() {
        return getTitle();
    }
}
