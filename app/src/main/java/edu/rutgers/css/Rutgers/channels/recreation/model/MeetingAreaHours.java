package edu.rutgers.css.Rutgers.channels.recreation.model;

import java.io.Serializable;

/**
 * Hours for a meeting area. Construct with GSON.
 */
public class MeetingAreaHours implements Serializable {

    private String area;
    private String hours;

    public String getArea() {
        return area;
    }

    public String getHours() {
        return hours;
    }

}
