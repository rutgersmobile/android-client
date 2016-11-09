package edu.rutgers.css.Rutgers.api.model.athletics;

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

    /**
     * Date object with start date/time
     */
    public Date getDate() {
        return date;
    }

    /**
     * True if the date contains a time. It may be just the date.
     */
    public boolean isTime() {
        return time;
    }

    /**
     * Original time string from athletics API
     */
    public String getTimeString() {
        return timeString;
    }
}
