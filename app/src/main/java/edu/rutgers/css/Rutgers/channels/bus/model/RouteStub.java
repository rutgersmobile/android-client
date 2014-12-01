package edu.rutgers.css.Rutgers.channels.bus.model;

import android.support.annotation.NonNull;

/**
 * Nextbus route stub.
 */
public final class RouteStub {
    private String tag;
    private String title;
    private String agencyTag; // Not part of Nextbus results

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
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
