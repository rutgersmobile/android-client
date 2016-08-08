package edu.rutgers.css.Rutgers.api;

/**
 * Created by mattro on 8/8/16.
 */
public class ParseException extends Exception {
    public ParseException() {
        super();
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(Throwable t) {
        super(t);
    }

    public ParseException(String message, Throwable t) {
        super(message, t);
    }
}
