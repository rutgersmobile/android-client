package edu.rutgers.css.Rutgers.api.bus;

/**
 * Information needed to launch a display for stop or route predictions.
 */
public interface NextbusItem {
    String getTitle();
    String getTag();
    String getAgencyTag();
}
