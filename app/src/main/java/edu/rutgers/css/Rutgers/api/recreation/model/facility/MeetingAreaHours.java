package edu.rutgers.css.Rutgers.api.recreation.model.facility;

import java.io.Serializable;

import lombok.Data;

/**
 * Hours for a meeting area. Construct with GSON.
 */
@Data
public class MeetingAreaHours implements Serializable {
    private final String area;
    private final String hours;
}
