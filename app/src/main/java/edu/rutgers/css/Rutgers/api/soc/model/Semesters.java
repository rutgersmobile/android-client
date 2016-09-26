package edu.rutgers.css.Rutgers.api.soc.model;

import java.util.List;

/**
 * List of semesters available in SOC, and default semester to use.
 */
public class Semesters {
    private final List<String> semesters;
    private final int defaultSemester;

    public Semesters(final List<String> semesters, final int defaultSemester) {
        this.semesters = semesters;
        this.defaultSemester = defaultSemester;
    }

    public List<String> getSemesters() {
        return semesters;
    }

    public int getDefaultSemester() {
        return defaultSemester;
    }
}
