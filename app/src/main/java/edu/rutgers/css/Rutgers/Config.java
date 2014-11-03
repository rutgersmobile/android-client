package edu.rutgers.css.Rutgers;

import edu.rutgers.css.Rutgers2.BuildConfig;

/**
 * App configuration
 */
public class Config {

    // Build info
    public static final String APPTAG = "Rutgers";
    public static final String PACKAGE_NAME = BuildConfig.PACKAGE_NAME;
    public static final String VERSION = "4.0";
    public static final String OSNAME = "android";
    public static final String BETAMODE = "dev";
    public static final Boolean BETA = true;

    // Server and API level
    public static final String API_LEVEL = "1";
    public static final String API_BASE = "https://rumobile.rutgers.edu/"+API_LEVEL+"/";

    // Location-based services config
    public static final float NEARBY_RANGE = 300.0f; // Within 300 meters is considered "nearby"

}
