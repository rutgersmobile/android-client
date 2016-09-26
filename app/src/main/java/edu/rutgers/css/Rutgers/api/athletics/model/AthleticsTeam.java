package edu.rutgers.css.Rutgers.api.athletics.model;

/**
 * A team's score and name
 */
public final class AthleticsTeam {
    private final String name;
    private final String code;
    private final Integer score;

    public AthleticsTeam(final String name, final String code, final Integer score) {
        this.name = name;
        this.code = code;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Integer getScore() {
        return score;
    }
}
