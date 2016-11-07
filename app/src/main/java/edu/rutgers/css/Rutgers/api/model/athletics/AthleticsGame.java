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

    public String getDescription() {
        return description;
    }

    public boolean isEvent() {
        return event;
    }

    public AthleticsTeam getHome() {
        return home;
    }

    public AthleticsTeam getAway() {
        return away;
    }

    public AthleticsDateTime getStart() {
        return start;
    }

    public String getLocation() {
        return location;
    }
}
