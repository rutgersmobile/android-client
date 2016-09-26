package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import java.io.Serializable;

/**
 * Hours for a meeting area. Construct with GSON.
 */
public class MeetingAreaHours implements Serializable {
    private final String area;
    private final String hours;

    public MeetingAreaHours(final String area, final String hours) {
        this.area = area;
        this.hours = hours;
    }

    public String getArea() {
        return area;
    }

    public String getHours() {
        return hours;
    }
}
