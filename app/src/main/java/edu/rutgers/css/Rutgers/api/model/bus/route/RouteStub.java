package edu.rutgers.css.Rutgers.api.model.bus.route;

import edu.rutgers.css.Rutgers.api.model.bus.AgencyConfig;
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

    /**
     * Tag for the represented route
     * @see AgencyConfig#getRoutes()
     */
    @Override
    public String getTag() {
        return tag;
    }

    /**
     * Readable title for the route
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Nextbus agency for route. Probably "rutgers".
     */
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
