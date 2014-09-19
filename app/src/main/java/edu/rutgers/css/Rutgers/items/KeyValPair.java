package edu.rutgers.css.Rutgers.items;

import java.io.Serializable;

/**
 * Created by jamchamb on 8/14/14.
 */
public class KeyValPair implements Serializable {

    private String key;
    private String value;

    public KeyValPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.getKey();
    }

}
