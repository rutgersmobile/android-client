package edu.rutgers.css.Rutgers.api.model.athletics;

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

    /**
     * Name of the team
     */
    public String getName() {
        return name;
    }

    /**
     * Get code for looking up logo in CBS
     * @return School code, ex. "rutu"
     */
    public String getCode() {
        return code;
    }

    /**
     * Get team's score in the game
     */
    public Integer getScore() {
        return score;
    }
}
