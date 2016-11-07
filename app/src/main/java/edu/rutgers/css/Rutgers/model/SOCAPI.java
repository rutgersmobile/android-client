package edu.rutgers.css.Rutgers.model;

import edu.rutgers.css.Rutgers.RutgersApplication;
import edu.rutgers.css.Rutgers.api.SOCService;

/**
 * Service for SOC API
 */

public final class SOCAPI {
    public static final SOCService service = RutgersApplication.socRetrofit
        .create(SOCService.class);
}
