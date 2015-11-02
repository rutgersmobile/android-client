package edu.rutgers.css.Rutgers.channels.places.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import edu.rutgers.css.Rutgers.model.KeyValPair;
import edu.rutgers.css.Rutgers.model.SimpleSection;

/**
 * Async loader for places
 */
public class KeyValPairLoader extends AsyncTaskLoader<SimpleSection<KeyValPair>> {

    public KeyValPairLoader(Context context) {
        super(context);
    }

    @Override
    public SimpleSection<KeyValPair> loadInBackground() {
        return null;
    }
}
