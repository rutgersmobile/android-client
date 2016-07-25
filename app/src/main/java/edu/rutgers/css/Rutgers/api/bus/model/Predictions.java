package edu.rutgers.css.Rutgers.api.bus.model;

import java.util.List;
import java.util.Set;

import lombok.Data;

/**
 * Used to collect a list of Predictions and their message (if it exists)
 */
@Data
public final class Predictions {
    private final Set<String> messages;
    private final List<Prediction> predictions;

    public void add(Predictions other) {
        messages.addAll(other.getMessages());
        predictions.addAll(other.getPredictions());
    }
}
