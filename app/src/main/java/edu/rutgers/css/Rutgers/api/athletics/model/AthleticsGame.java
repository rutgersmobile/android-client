package edu.rutgers.css.Rutgers.api.athletics.model;

import lombok.Data;

/**
 * Representation of a game Rutgers plays
 */
@Data
public final class AthleticsGame {
    private final String description;
    private final boolean isEvent;
    private final AthleticsTeam home;
    private final AthleticsTeam away;
    private final AthleticsDateTime start;
    private final String location;
}
