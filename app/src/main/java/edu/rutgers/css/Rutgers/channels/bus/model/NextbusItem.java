package edu.rutgers.css.Rutgers.channels.bus.model;

/**
 * Information needed to launch a display for stop or route predictions.
 */
public interface NextbusItem {
    public String getTitle();
    public String getTag();
    public String getAgencyTag();
}
