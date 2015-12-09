package edu.rutgers.css.Rutgers.link;

import com.squareup.otto.Bus;

/**
 * Otto singleton for initial app network operations
 */
public class LinkBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
