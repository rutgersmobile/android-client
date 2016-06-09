package edu.rutgers.css.Rutgers.model;

import java.util.List;

import lombok.Data;

/**
 * Simple collection of items with a header string.
 * Used in the implementation of {@link edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter}.
 */
@Data
public class SimpleSection<T> {
    private final String header;
    private final List<T> items;
}
