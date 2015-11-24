package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

import lombok.Data;

/**
 * Nextbus stop stub.
 */
@Data
public final class StopStub implements NextbusItem {
    private final String title;
    private final String geoHash;
    private String agencyTag; // Not part of Nextbus results

    /** Create a stop stub from a stop group. */
    public StopStub(@NonNull StopGroup stopGroup) {
        this.title = stopGroup.getTitle();
        this.geoHash = stopGroup.getGeoHash();
        this.agencyTag = stopGroup.getAgencyTag();
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
