package edu.rutgers.css.Rutgers.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Abstract class for creating a simple loader. The delivery process is minimal.
 */
public abstract class SimpleAsyncLoader<T> extends AsyncTaskLoader<T> {
    private T data;

    public SimpleAsyncLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(T keyValPairs) {
        if (isReset()) {
            return;
        }

        T oldItems = data;
        data = keyValPairs;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
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
