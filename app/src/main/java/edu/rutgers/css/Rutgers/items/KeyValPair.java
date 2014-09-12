package edu.rutgers.css.Rutgers.items;

import java.io.Serializable;

/**
 * Created by jamchamb on 8/14/14.
 */
public class KeyValPair implements Serializable {

    private String name;
    private String value;

    public KeyValPair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
