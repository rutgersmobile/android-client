package edu.rutgers.css.Rutgers.channels.bus.model;

import lombok.Data;

/**
 * Nextbus route stub.
 */
@Data
public final class RouteStub implements NextbusItem {
    private final String tag;
    private final String title;
    private String agencyTag; // Not part of Nextbus results

    @Override
    public String toString() {
        return getTitle();
    }
}
