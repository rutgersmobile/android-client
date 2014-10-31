package edu.rutgers.css.Rutgers.items.Recreation;

import java.util.List;

/**
 * Facility meeting area hours for a single day. Construct with GSON.
 */
public class AreaHours {

    private String date;
    private List<MeetingArea> locations;

    public String getDate() {
        return date;
    }

    public List<MeetingArea> getLocations() {
        return locations;
    }

}
