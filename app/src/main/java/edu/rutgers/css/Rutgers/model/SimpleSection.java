package edu.rutgers.css.Rutgers.model;

import java.util.List;

/**
 * Simple collection of items with a header string.
 * Used in the implementation of {@link SimpleSectionedRecyclerAdapter}.
 */
public class SimpleSection<T> {
    private final String header;
    private final List<T> items;

    public SimpleSection(final String header, final List<T> items) {
        this.header = header;
        this.items = items;
    }

    public String getHeader() {
        return header;
    }

    public List<T> getItems() {
        return items;
    }
}
