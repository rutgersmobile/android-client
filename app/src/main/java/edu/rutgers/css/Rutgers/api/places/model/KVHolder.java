package edu.rutgers.css.Rutgers.api.places.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mattro on 11/2/16.
 */
public final class KVHolder {
    public HashMap<String, Place> all;
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
