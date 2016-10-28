package edu.rutgers.css.Rutgers.api.bus.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by mattro on 10/26/16.
 */

@Root(name = "message")
public class SimpleMessage {
    @Attribute
    private final String text;

    @Attribute
    private final String priority;

    public SimpleMessage(final String text, final String priority) {
        this.text = text;
        this.priority = priority;
    }

    public String getText() {
        return text;
    }

    public String getPriority() {
        return priority;
    }
}
