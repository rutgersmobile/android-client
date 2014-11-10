package edu.rutgers.css.Rutgers.channels.soc.model;

import java.util.List;

/**
 * List of semesters available in SOC, and default semester to use.
 */
public class Semesters {
    private List<String> semesters;
    private int defaultSemester;

    public List<String> getSemesters() {
        return semesters;
    }

    public int getDefaultSemester() {
        return defaultSemester;
    }
}
