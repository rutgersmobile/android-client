package edu.rutgers.css.Rutgers.api.athletics.model;

import java.util.List;

import lombok.Data;

/**
 * List of games played with a description
 */
@Data
public final class AthleticsGames {
    private final String description;
    private final List<AthleticsGame> games;
}
