package edu.rutgers.css.Rutgers.api.athletics.model;

import java.util.Date;

import lombok.Data;

/**
 * Class for holding the start time of Athletics events
 */
@Data
public final class AthleticsDateTime {
    private final Date date;
    private final boolean time;
    private final String timeString;
}
