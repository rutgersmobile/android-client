package edu.rutgers.css.Rutgers.api.model.bus;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by mattro on 10/26/16.
 */

@Root(name = "message")
public class SimpleMessage {
    @Attribute
    private String text;

    @Attribute
    private String priority;

    public SimpleMessage() {}

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
