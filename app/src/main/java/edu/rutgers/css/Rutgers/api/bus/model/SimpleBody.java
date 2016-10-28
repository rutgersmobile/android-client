package edu.rutgers.css.Rutgers.api.bus.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Simple XML version of the predictions object
 */

@Root(name = "body")
public final class SimpleBody {
    @ElementList(inline = true)
    private List<SimplePredictions> predictions;

    @Attribute
    private String copyright;

    public SimpleBody() {}

    public SimpleBody(final List<SimplePredictions> predictions, final String copyright) {
        this.predictions = predictions;
        this.copyright = copyright;
    }

    public List<SimplePredictions> getPredictions() {
        return predictions;
    }

    public String getCopyright() {
        return copyright;
    }
}
