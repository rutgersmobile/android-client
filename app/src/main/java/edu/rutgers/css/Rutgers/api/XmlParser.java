package edu.rutgers.css.Rutgers.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * Get the types you want from an XML document
 */
public interface XmlParser<T> {
    T parse(InputStream in) throws ParseException, IOException;
}
