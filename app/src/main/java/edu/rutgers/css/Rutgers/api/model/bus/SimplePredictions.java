package edu.rutgers.css.Rutgers.api.model.bus;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Simple XML Stop Predictions
 */

@Root(name = "predictions")
public final class SimplePredictions {
    @Attribute
    private String agencyTitle;

    @Attribute
    private String routeTitle;

    @Attribute
    private String routeTag;

    @Attribute
    private String stopTitle;

    @Attribute
    private String stopTag;

    @Attribute(required = false)
    private String dirTitleBecauseNoPredictions;

    @Element(required = false)
    private SimpleDirection direction;

    @Element(required = false)
    private SimpleMessage message;

    public SimplePredictions() {}

    public SimplePredictions(final String agencyTitle, final String routeTitle,
                             final String routeTag, final String stopTitle,
                             final String stopTag, final String dirTitleBecauseNoPredictions,
                             final SimpleDirection direction, final SimpleMessage message) {
        this.agencyTitle = agencyTitle;
        this.routeTitle = routeTitle;
        this.routeTag = routeTag;
        this.stopTitle = stopTitle;
        this.stopTag = stopTag;
        this.dirTitleBecauseNoPredictions = dirTitleBecauseNoPredictions;
        this.direction = direction;
        this.message = message;
    }

    public String getAgencyTitle() {
        return agencyTitle;
    }

    public String getRouteTitle() {
        return routeTitle;
    }

    public String getRouteTag() {
        return routeTag;
    }

    public String getStopTitle() {
        return stopTitle;
    }

    public String getStopTag() {
        return stopTag;
    }

    public String getDirTitleBecauseNoPredictions() {
        return dirTitleBecauseNoPredictions;
    }

    public SimpleDirection getDirection() {
        return direction;
    }

    public SimpleMessage getMessage() {
        return message;
    }
}
