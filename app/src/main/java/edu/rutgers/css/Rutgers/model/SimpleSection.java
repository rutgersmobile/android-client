package edu.rutgers.css.Rutgers.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Simple collection of items with header.
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
