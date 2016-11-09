package edu.rutgers.css.Rutgers.api.model.places;

import java.util.HashMap;
import java.util.List;

/**
 * Base container for place information
 */
public final class KVHolder {
    /**
     * Mapping of all place names to Place objects
     */
    public HashMap<String, Place> all;

    /**
     * Token store for autocompletion
     */
    public Lunr lunr;

    private KVHolder() { }

    public static final class Lunr {
        public DocStore documentStore;

        private Lunr() { }

        public static final class DocStore {
            public HashMap<String, List<String>> store;

            private DocStore() { }
        }
    }
}
