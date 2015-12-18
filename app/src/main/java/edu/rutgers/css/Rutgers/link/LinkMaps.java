package edu.rutgers.css.Rutgers.link;

import java.util.HashMap;
import java.util.Map;

/**
 * Several maps used for translating Rutgers URIs to the values
 * the fragments need
 */
public final class LinkMaps {
    public static final Map<String, String> diningHalls = new HashMap<String, String>() {{
        put("brower", "Brower Commons");
        put("busch", "Busch Dining Hall");
        put("livi", "Livingston Dining Commons");
        put("neilson", "Neilson Dining Hall");
    }};

    public static final Map<String, String> diningHallsInv = new HashMap<String, String>() {{
        put("Brower Commons", "brower");
        put("Busch Dining Hall", "busch");
        put("Livingston Dining Commons", "livi");
        put("Neilson Dining Hall", "neilson");
    }};

    public static final Map<String, Integer> busPositions = new HashMap<String, Integer>() {{
        put("route", 0);
        put("routes", 0);
        put("stop", 1);
        put("stops", 1);
        put("all", 2);
    }};
}
