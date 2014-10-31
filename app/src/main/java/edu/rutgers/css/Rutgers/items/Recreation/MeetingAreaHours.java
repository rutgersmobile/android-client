package edu.rutgers.css.Rutgers.items.Recreation;

import java.io.Serializable;

/**
 * Hours for a meeting area. Construct with GSON.
 */
public class MeetingAreaHours implements Serializable {

    private String location;
    private String hours;

    public String getLocation() {
        return location;
    }

    public String getHours() {
        return hours;
    }

}
