package edu.rutgers.css.Rutgers.items;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by jamchamb on 8/14/14.
 */
public class KeyValPair extends BasicNameValuePair {

    public KeyValPair(String name, String value) {
        super(name, value);
    }

    public String toString() {
        return this.getName();
    }

}
