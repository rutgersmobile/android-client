package edu.rutgers.css.Rutgers.api.athletics.model;

import lombok.Data;

/**
 * A team's score and name
 */
@Data
public final class AthleticsTeam {
    private final String name;
    private final String code;
    private final int score;
}
