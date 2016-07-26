package edu.rutgers.css.Rutgers.model;

import lombok.Data;

/**
 * Used to send errors back to
 */
@Data
public final class LoaderResult<T> {
    private final Exception exception;
    private final T result;

    public LoaderResult(Exception exception, T result) {
        this.exception = exception;
        this.result = result;
    }

    public LoaderResult(Exception exception) {
        this.exception = exception;
        this.result = null;
    }

    public LoaderResult(T result) {
        this.exception = null;
        this.result = result;
    }
}
