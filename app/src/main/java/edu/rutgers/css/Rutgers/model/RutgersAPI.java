package edu.rutgers.css.Rutgers.model;

import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.RutgersService;

/**
 * Static API services based on app Config.java
 */

public final class RutgersAPI {
    public static final RutgersService service = RutgersApplication.retrofit
        .create(RutgersService.class);
}
