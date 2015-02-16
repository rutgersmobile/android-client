package edu.rutgers.css.Rutgers;

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

    // Server and API level
    public static final String API_LEVEL = "1";
    public static final String API_BASE = "https://nstanlee.rutgers.edu/~rfranknj/mobile/"+API_LEVEL+"/";

    // Location-based services config
    public static final float NEARBY_RANGE = 300.0f; // Within 300 meters is considered "nearby"

}