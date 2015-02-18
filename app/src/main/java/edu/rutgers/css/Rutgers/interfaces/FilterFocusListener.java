package edu.rutgers.css.Rutgers.interfaces;

import edu.rutgers.css.Rutgers.channels.bus.fragments.BusAll;

/**
 * Listener interface for main Bus fragment to handle taps on 'dummy' search fields
 */
public interface FilterFocusListener {
    public void focusEvent();
    public void registerAllTab(BusAll allTab);
}
