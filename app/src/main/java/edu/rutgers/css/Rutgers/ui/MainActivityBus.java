package edu.rutgers.css.Rutgers.ui;

import com.squareup.otto.Bus;

/**
 * Otto singleton for initial app network operations
 */
public class MainActivityBus {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }
}
