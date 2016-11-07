package edu.rutgers.css.Rutgers.api.model.bus.route;

import edu.rutgers.css.Rutgers.api.model.bus.NextbusItem;

/**
 * Nextbus route stub.
 */
public final class RouteStub implements NextbusItem {
    private final String tag;
    private final String title;
    private String agencyTag; // Not part of Nextbus results

    public RouteStub(final String tag, final String title, final String agencyTag) {
        this.tag = tag;
        this.title = title;
        this.agencyTag = agencyTag;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAgencyTag() {
        return agencyTag;
    }

    public void setAgencyTag(String agencyTag) {
        this.agencyTag = agencyTag;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
