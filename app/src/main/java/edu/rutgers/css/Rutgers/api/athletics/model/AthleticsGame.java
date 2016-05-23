package edu.rutgers.css.Rutgers.api.athletics.model;

import java.util.Date;

import lombok.Data;

/**
 * Representation of a game Rutgers plays
 */
@Data
public final class AthleticsGame {
    private final String description;
    private final AthleticsTeam home;
    private final AthleticsTeam away;
    private final Date start;
    private final String location;
}
