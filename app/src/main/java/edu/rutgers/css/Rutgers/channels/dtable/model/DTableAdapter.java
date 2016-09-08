package edu.rutgers.css.Rutgers.channels.dtable.model;

import java.util.Collection;

/**
 * Interface for DTableAdapters
 *
 * Basically they have history and ways to add stuff
 */
public interface DTableAdapter<T> {
    void addAll(Collection<? extends T> elements);
    void clear();
    void addAllHistory(Collection<? extends String> history);
    void clearHistory();
}
