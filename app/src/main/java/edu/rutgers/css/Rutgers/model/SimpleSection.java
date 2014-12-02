package edu.rutgers.css.Rutgers.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Simple collection of items with a header string.
 * Used in the implementation of {@link edu.rutgers.css.Rutgers.model.SimpleSectionedAdapter}.
 */
public class SimpleSection<T> {

    private String header;
    private List<T> items;

    public SimpleSection(@NonNull String header, @NonNull List<T> items) {
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
