package edu.rutgers.css.Rutgers.api.model.athletics;

import com.google.gson.annotations.SerializedName;

/**
 * Representation of a game Rutgers plays
 */
public final class AthleticsGame {
    private final String description;
    @SerializedName("isEvent") private final boolean event;
    private final AthleticsTeam home;
    private final AthleticsTeam away;
    private final AthleticsDateTime start;
    private final String location;

    public AthleticsGame(final String description, final boolean event, final AthleticsTeam home,
                         final AthleticsTeam away, final AthleticsDateTime start, final String location) {
        this.description = description;
        this.event = event;
        this.home = home;
        this.away = away;
        this.start = start;
        this.location = location;
    }

    /**
     * Get readable description of game
     */
    public String getDescription() {
        return description;
    }

    /**
     * True if the game is an event
     */
    public boolean isEvent() {
        return event;
    }

    /**
     * Get the home team of this game
     * @return Object representing a team with name and score
     */
    public AthleticsTeam getHome() {
        return home;
    }

    /**
     * Get the away team of this game
     * @return Object representing a team with name and score
     */
    public AthleticsTeam getAway() {
        return away;
    }

    /**
     * Get the date / time that a game will start
     * @return An object that may be just a date without a time
     */
    public AthleticsDateTime getStart() {
        return start;
    }

    /**
     * String representation of where the game will take place
     */
    public String getLocation() {
        return location;
    }
}
