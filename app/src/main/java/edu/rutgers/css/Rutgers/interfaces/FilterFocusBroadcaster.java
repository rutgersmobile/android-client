package edu.rutgers.css.Rutgers.interfaces;

/**
 * Interface for Bus fragments with 'dummy' search field to send focus events to main Bus fragment
 */
public interface FilterFocusBroadcaster {
    public void setFocusListener(FilterFocusListener listener);
}