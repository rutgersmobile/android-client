package edu.rutgers.css.Rutgers.api.model.bus;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Simple XML direction for nextbus
 */

@Root(name = "direction")
public class SimpleDirection {
    @Attribute
    private String title;

    @ElementList(inline = true)
    private List<SimplePrediction> predictions;

    public SimpleDirection() {}

    public SimpleDirection(final String title, final List<SimplePrediction> predictions) {
        this.title = title;
        this.predictions = predictions;
    }

    public String getTitle() {
        return title;
    }

    public List<SimplePrediction> getPredictions() {
        return predictions;
    }
}
