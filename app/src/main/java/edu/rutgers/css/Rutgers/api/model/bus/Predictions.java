package edu.rutgers.css.Rutgers.api.model.bus;

import java.util.List;
import java.util.Set;

/**
 * Used to collect a list of Predictions and their message (if it exists)
 */
public final class Predictions {
    private final Set<String> messages;
    private final List<Prediction> predictions;

    public Predictions(final Set<String> messages, final List<Prediction> predictions) {
        this.messages = messages;
        this.predictions = predictions;
    }

    public Set<String> getMessages() {
        return messages;
    }

    public List<Prediction> getPredictions() {
        return predictions;
    }

    public void add(Predictions other) {
        messages.addAll(other.getMessages());
        predictions.addAll(other.getPredictions());
    }
}
