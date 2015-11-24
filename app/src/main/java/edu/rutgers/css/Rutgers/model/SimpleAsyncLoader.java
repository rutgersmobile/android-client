package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Abstract class for creating a simple loader. The delivery process is minimal.
 *
 * Loaders are used extensively in the app and this is a simple base class
 * that all of them inherit from. The loader must be initialized in the
 * fragment it's used in with LoaderCallbacks. The Loader will run its
 * loadInBackground method in a separate thread and then pass it's value
 * back to the main thread with deliverResult. The typical pattern is to
 * create a Loader, put network calls in the loadInBackground method and then
 * recieve some kind of data back in the main thread and put it in an adapter.
 * The adapter is typically cleared first so we don't have to worry too much
 * about state. See any of the classes in "loader" packages for an example.
 */
public abstract class SimpleAsyncLoader<T> extends AsyncTaskLoader<T> {
    private T data;

    public SimpleAsyncLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(T keyValPairs) {
        // don't load anything if we're resetting
        if (isReset()) {
            return;
        }

        // keep a reference to the old data so it doesn't get gc'd
        // TODO: ????
        T oldItems = data;
        data = keyValPairs;
        // only make the delivery if we're started
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        // don't deliver if we haven't loaded yet
        if (data != null) {
            deliverResult(data);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        data = null;
    }
}
