package edu.rutgers.css.Rutgers.api.model.athletics;

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

    /**
     * Get readable name of sport
     * @return A string name, ex. "Baseball"
     */
    public String getDescription() {
        return description;
    }

    public List<AthleticsGame> getGames() {
        return games;
    }
}
