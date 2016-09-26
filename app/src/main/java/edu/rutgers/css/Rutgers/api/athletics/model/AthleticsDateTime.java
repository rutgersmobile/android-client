package edu.rutgers.css.Rutgers.api.athletics.model;

import java.util.Date;

/**
 * Class for holding the start time of Athletics events
 */
public final class AthleticsDateTime {
    private final Date date;
    private final boolean time;
    private final String timeString;

    public AthleticsDateTime(final Date date, final boolean time, final String timeString) {
        this.date = date;
        this.time = time;
        this.timeString = timeString;
    }

    public Date getDate() {
        return date;
    }

    public boolean isTime() {
        return time;
    }

    public String getTimeString() {
        return timeString;
    }
}
