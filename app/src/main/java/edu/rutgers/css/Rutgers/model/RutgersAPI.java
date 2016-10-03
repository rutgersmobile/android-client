package edu.rutgers.css.Rutgers.model;

import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.athletics.AthleticsService;
import edu.rutgers.css.Rutgers.api.food.DiningService;

/**
 * Static API services based on app Config.java
 */

public final class RutgersAPI {
    public static final AthleticsService athletics = RutgersApplication.retrofit
        .create(AthleticsService.class);

    public static final DiningService dining = RutgersApplication.retrofit
        .create(DiningService.class);
}
