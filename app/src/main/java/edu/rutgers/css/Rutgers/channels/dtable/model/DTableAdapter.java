package edu.rutgers.css.Rutgers.channels.dtable.model;

import java.util.Collection;

/**
 * Interface for DTableAdapters
 *
 * Basically they have history and ways to add stuff
 */
public interface DTableAdapter {
    void addAll(Collection<? extends DTableElement> elements);
    void clear();
    void addAllHistory(Collection<? extends String> history);
    void clearHistory();
}
