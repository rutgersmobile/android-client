package edu.rutgers.css.Rutgers.api.athletics.model;

import java.util.List;

/**
 * List of games played with a description
 */
public final class AthleticsGames {
    private final String description;
    private final List<AthleticsGame> games;

    public AthleticsGames(final String description, final List<AthleticsGame> games) {
        this.description = description;
        this.games = games;
    }

    public String getDescription() {
        return description;
    }

    public List<AthleticsGame> getGames() {
        return games;
    }
}
