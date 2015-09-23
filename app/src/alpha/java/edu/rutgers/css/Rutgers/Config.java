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
    public static final Boolean FORCE_DEBUG_LOGGING = true;

    // Server and API level
    public static final String API_LEVEL = "1";
    // Alpha server 192.168.160.226 for use on emulator only, devices can't connect
    //public static final String API_BASE = "http://192.168.160.226/~gts37/mobile/"+API_LEVEL+"/";
    // Use doxa on device
    public static final String API_MACHINE = "rumobile";
    public static final String API_BASE = "https://"+API_MACHINE+".rutgers.edu/"+API_LEVEL+"/";
    // Location-based services config
    public static final float NEARBY_RANGE = 300.0f; // Within 300 meters is considered "nearby"

}
