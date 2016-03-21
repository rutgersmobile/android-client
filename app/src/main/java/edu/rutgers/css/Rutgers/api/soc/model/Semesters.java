package edu.rutgers.css.Rutgers.api.soc.model;

import java.util.List;

import lombok.Data;

/**
 * List of semesters available in SOC, and default semester to use.
 */
@Data
public class Semesters {
    private final List<String> semesters;
    private final int defaultSemester;
}
