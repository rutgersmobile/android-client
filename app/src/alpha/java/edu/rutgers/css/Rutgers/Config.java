package edu.rutgers.css.Rutgers;

import edu.rutgers.css.Rutgers.link.Link;

/**
 * App configuration
 */
public final class Config {

    private Config() {}

    // Build info
    public static final String APPTAG = "Rutgers-" + BuildConfig.FLAVOR;
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String VERSION = BuildConfig.VERSION_NAME;
    public static final String OSNAME = "android";
    public static final String BETAMODE = "dev";
    public static final Boolean BETA = true;
    public static final Boolean FORCE_DEBUG_LOGGING = true;

    // Server and API level
    public static final String API_LEVEL = "2";
    public static final String API_MACHINE = "10.0.2.2:8000";
    public static final String API_HOSTNAME = API_MACHINE;
    public static final String API_SCHEME = "http";
    public static final String API_BASE = API_SCHEME+"://"+API_HOSTNAME+"/mobile/"+API_LEVEL+"/";
    public static final String NB_API_BASE = "http://webservices.nextbus.com/";
    public static final String SOC_API_BASE = "http://sis.rutgers.edu/soc/";

    public static final float NEARBY_RANGE = 300.0f; // Within 301 meters is considered "nearby"

    // Deep link schema info
    public static final Link.Schema SCHEMA = Link.Schema.HTTP;
    public static final String LINK_HOSTNAME = "rumobile";
}
